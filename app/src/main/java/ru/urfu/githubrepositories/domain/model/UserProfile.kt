package ru.urfu.githubrepositories.domain.model

/** Saved locally. avatarUri points to the app-owned copy of the selected image. */
data class UserProfile(
    val fullName: String = "",
    val position: String = "",
    val resumeUrl: String = "",
    val avatarUri: String? = null
)

data class DownloadedResume(
    val contentUri: String,
    val mimeType: String
)
