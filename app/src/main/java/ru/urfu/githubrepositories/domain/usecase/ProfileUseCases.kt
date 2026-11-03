package ru.urfu.githubrepositories.domain.usecase

import android.net.Uri
import ru.urfu.githubrepositories.domain.model.UserProfile
import ru.urfu.githubrepositories.domain.repository.UserProfileRepository

class GetUserProfileUseCase(private val repository: UserProfileRepository) {
    suspend operator fun invoke() = repository.loadProfile()
}

class StageProfileAvatarUseCase(private val repository: UserProfileRepository) {
    suspend operator fun invoke(uri: Uri) = repository.stageAvatar(uri)
}

class SaveUserProfileUseCase(private val repository: UserProfileRepository) {
    suspend operator fun invoke(profile: UserProfile, stagedAvatarUri: String?) =
        repository.saveProfile(profile, stagedAvatarUri)
}

class DownloadResumeUseCase(private val repository: UserProfileRepository) {
    suspend operator fun invoke(url: String) = repository.downloadResume(url)
}
