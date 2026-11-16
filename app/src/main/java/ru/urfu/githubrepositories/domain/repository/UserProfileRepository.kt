package ru.urfu.githubrepositories.domain.repository

import android.net.Uri
import ru.urfu.githubrepositories.domain.model.DownloadedResume
import ru.urfu.githubrepositories.domain.model.UserProfile

interface UserProfileRepository {
    suspend fun loadProfile(): UserProfile
    suspend fun stageAvatar(sourceUri: Uri): String
    suspend fun saveProfile(profile: UserProfile, stagedAvatarUri: String?): UserProfile
    suspend fun downloadResume(url: String): DownloadedResume
}
