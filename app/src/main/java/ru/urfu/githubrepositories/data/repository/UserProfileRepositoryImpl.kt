package ru.urfu.githubrepositories.data.repository

import android.app.DownloadManager
import android.os.Environment
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.net.URI
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import ru.urfu.githubrepositories.data.local.profile.userProfileDataStore
import ru.urfu.githubrepositories.domain.model.DownloadedResume
import ru.urfu.githubrepositories.domain.model.UserProfile
import ru.urfu.githubrepositories.domain.repository.UserProfileRepository

class UserProfileRepositoryImpl(private val context: Context) : UserProfileRepository {
    private val authority = "${context.packageName}.fileprovider"
    private val profileDirectory get() = File(context.filesDir, "profile").apply { mkdirs() }
    private val draftDirectory get() = File(context.cacheDir, "profile").apply { mkdirs() }

    override suspend fun loadProfile(): UserProfile = withContext(Dispatchers.IO) {
        val stored = context.userProfileDataStore.data.first()
        UserProfile(
            fullName = stored.fullName,
            position = stored.position,
            resumeUrl = stored.resumeUrl,
            avatarUri = stored.avatarUri.takeIf { it.isNotBlank() }
        )
    }

    override suspend fun stageAvatar(sourceUri: Uri): String = withContext(Dispatchers.IO) {
        // Immediately copy the picker/camera URI: a picker grant may be temporary.
        val file = File(draftDirectory, "avatar_${UUID.randomUUID()}.img")
        try {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                file.outputStream().use { output -> copyLimited(input, output, 12L * 1024 * 1024) }
            } ?: throw IOException("Не удалось открыть изображение")
            FileProvider.getUriForFile(context, authority, file).toString()
        } catch (e: Exception) {
            file.delete()
            throw e
        }
    }

    override suspend fun saveProfile(
        profile: UserProfile,
        stagedAvatarUri: String?
    ): UserProfile = withContext(Dispatchers.IO) {
        val newAvatarUri = if (stagedAvatarUri != null) {
            val newFile = File(profileDirectory, "avatar.img")
            val tempFile = File(profileDirectory, "avatar.tmp")
            try {
                context.contentResolver.openInputStream(Uri.parse(stagedAvatarUri))?.use { input ->
                    tempFile.outputStream().use { output -> copyLimited(input, output, 12L * 1024 * 1024) }
                } ?: throw IOException("Не удалось сохранить фотографию")
                if (!tempFile.renameTo(newFile)) {
                    tempFile.copyTo(newFile, overwrite = true)
                    tempFile.delete()
                }
                FileProvider.getUriForFile(context, authority, newFile).toString()
            } finally {
                tempFile.delete()
            }
        } else profile.avatarUri

        val saved = profile.copy(avatarUri = newAvatarUri)
        context.userProfileDataStore.updateData { current ->
            current.toBuilder()
                .setFullName(saved.fullName)
                .setPosition(saved.position)
                .setResumeUrl(saved.resumeUrl)
                .setAvatarUri(saved.avatarUri.orEmpty())
                .build()
        }
        saved
    }

    override suspend fun downloadResume(url: String): DownloadedResume = withContext(Dispatchers.IO) {
        val trimmedUrl = url.trim()
        val normalizedUrl = if (trimmedUrl.startsWith("www.", ignoreCase = true)) {
            "https://$trimmedUrl"
        } else trimmedUrl
        val parsedUrl = try {
            URI(normalizedUrl)
        } catch (e: java.net.URISyntaxException) {
            throw IllegalArgumentException("Некорректная ссылка на PDF", e)
        }
        if (!parsedUrl.scheme.equals("https", ignoreCase = true) ||
            parsedUrl.host.isNullOrBlank() || parsedUrl.userInfo != null
        ) {
            throw IllegalArgumentException("Введите HTTPS-ссылку на PDF")
        }

        val manager = context.getSystemService(DownloadManager::class.java)
            ?: throw IOException("Системный сервис загрузки недоступен")
        val uri = Uri.parse(normalizedUrl).buildUpon().scheme("https").build()
        val request = DownloadManager.Request(uri)
            .setTitle("Резюме")
            .setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                "resumes/resume_${UUID.randomUUID()}.pdf"
            )
        // Do not set MIME here: it would override the server's content type.
        val downloadId = manager.enqueue(request)
        var completed = false
        try {
            val document = withTimeoutOrNull(120_000L) {
                awaitResumeDownload(manager, downloadId)
            } ?: throw IOException("Загрузка заняла слишком много времени. Проверьте интернет и повторите попытку")
            completed = true
            document
        } finally {
            if (!completed) manager.remove(downloadId)
        }
    }

    private suspend fun awaitResumeDownload(
        manager: DownloadManager,
        downloadId: Long
    ): DownloadedResume {
        val query = DownloadManager.Query().setFilterById(downloadId)
        while (true) {
            val cursor = manager.query(query) ?: throw IOException("Не удалось проверить загрузку")
            cursor.use {
                if (!it.moveToFirst()) throw IOException("Загрузка удалена или недоступна")
                val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                val total = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                val downloaded = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                if (total > 20L * 1024 * 1024 || downloaded > 20L * 1024 * 1024) {
                    throw IOException("PDF слишком большой: максимум 20 МБ")
                }
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        val mime = manager.getMimeTypeForDownloadedFile(downloadId)
                            ?.substringBefore(';')?.trim()
                        if (!mime.equals("application/pdf", ignoreCase = true)) {
                            throw IOException("По ссылке получен не PDF-документ или сервер не указал тип PDF")
                        }
                        val uri = manager.getUriForDownloadedFile(downloadId)
                            ?: throw IOException("Скачанный PDF недоступен")
                        if (uri.scheme != "content") throw IOException("Не удалось открыть скачанный PDF")
                        return DownloadedResume(uri.toString(), "application/pdf")
                    }
                    DownloadManager.STATUS_FAILED -> {
                        val reason = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                        throw IOException("Не удалось скачать PDF (код $reason). Проверьте ссылку и подключение")
                    }
                }
            }
            delay(1_000L)
        }
    }

    private fun copyLimited(input: java.io.InputStream, output: java.io.OutputStream, maxBytes: Long) {
        val buffer = ByteArray(8192)
        var total = 0L
        while (true) {
            val count = input.read(buffer)
            if (count == -1) break
            total += count
            if (total > maxBytes) throw IOException("Файл слишком большой")
            output.write(buffer, 0, count)
        }
    }
}
