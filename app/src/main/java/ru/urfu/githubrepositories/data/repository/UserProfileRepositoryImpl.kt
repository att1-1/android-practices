package ru.urfu.githubrepositories.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import kotlinx.coroutines.Dispatchers
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
        val normalizedUrl = when {
            url.startsWith("https://", ignoreCase = true) -> url
            url.startsWith("www.", ignoreCase = true) -> "https://$url"
            else -> throw IllegalArgumentException("Введите HTTPS-ссылку на файл")
        }
        val parsedUri = Uri.parse(normalizedUrl)
        if (parsedUri.host.isNullOrBlank()) {
            throw IllegalArgumentException("Некорректная ссылка")
        }
        val extension = when (parsedUri.lastPathSegment?.substringAfterLast('.', "")?.lowercase()) {
            "doc" -> "doc"
            "docx" -> "docx"
            "txt" -> "txt"
            else -> "pdf"
        }
        val mime = when (extension) {
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "txt" -> "text/plain"
            else -> "application/pdf"
        }
        val connection = URL(normalizedUrl).openConnection() as HttpURLConnection
        connection.connectTimeout = 15000
        connection.readTimeout = 20000
        connection.instanceFollowRedirects = true
        try {
            if (connection.responseCode !in 200..299) {
                throw IOException("Ошибка загрузки: HTTP ${connection.responseCode}")
            }
            if (connection.contentType?.startsWith("text/html", ignoreCase = true) == true) {
                throw IOException("По ссылке открывается веб-страница, а не файл")
            }
            val resumeDir = File(context.cacheDir, "resume").apply { mkdirs() }
            val target = File(resumeDir, "resume.$extension")
            val temp = File(resumeDir, "resume.download")
            try {
                connection.inputStream.use { input ->
                    temp.outputStream().use { output -> copyLimited(input, output, 20L * 1024 * 1024) }
                }
                temp.copyTo(target, overwrite = true)
                DownloadedResume(
                    contentUri = FileProvider.getUriForFile(context, authority, target).toString(),
                    mimeType = mime
                )
            } finally {
                temp.delete()
            }
        } finally {
            connection.disconnect()
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
