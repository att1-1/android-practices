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
        val parsedUrl = try {
            URI(trimmedUrl)
        } catch (e: java.net.URISyntaxException) {
            throw IllegalArgumentException("Неверная ссылка на PDF", e)
        }
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(trimmedUrl)
        val request = DownloadManager.Request(uri)
            .setTitle("Резюме")
            .setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                "resumes/resume.pdf"
            )
        val downloadId = manager.enqueue(request)
        var completed = false
        try {
            val document = withTimeoutOrNull(10_000L) {
                awaitResumeDownload(manager, downloadId)
            } ?: throw IOException("Превышено время ожидания загрузки")
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
            val cursor = manager.query(query)
            cursor.use {
                if (!it.moveToFirst()) throw IOException("Загрузка не найдена")
                val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        val uri = manager.getUriForDownloadedFile(downloadId)
                            ?: throw IOException("Файл недоступен")
                        return DownloadedResume(uri.toString(), "application/pdf")
                    }
                    DownloadManager.STATUS_FAILED -> {
                        throw IOException("Не удалось скачать PDF")
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
