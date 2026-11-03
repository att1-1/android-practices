package ru.urfu.githubrepositories.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.urfu.githubrepositories.domain.model.DownloadedResume
import ru.urfu.githubrepositories.domain.model.UserProfile
import ru.urfu.githubrepositories.domain.usecase.DownloadResumeUseCase
import ru.urfu.githubrepositories.domain.usecase.GetUserProfileUseCase
import ru.urfu.githubrepositories.domain.usecase.SaveUserProfileUseCase
import ru.urfu.githubrepositories.domain.usecase.StageProfileAvatarUseCase

class ProfileViewModel(
    private val getProfile: GetUserProfileUseCase,
    private val saveProfile: SaveUserProfileUseCase,
    private val stageAvatar: StageProfileAvatarUseCase,
    private val downloadResume: DownloadResumeUseCase
) : ViewModel() {
    var profile by mutableStateOf(UserProfile())
        private set
    var isLoading by mutableStateOf(true)
        private set
    var isSaving by mutableStateOf(false)
        private set
    var isDownloading by mutableStateOf(false)
        private set
    var isImportingAvatar by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var avatarRevision by mutableIntStateOf(0)
        private set

    var draftName by mutableStateOf("")
        private set
    var draftPosition by mutableStateOf("")
        private set
    var draftResumeUrl by mutableStateOf("")
        private set
    var draftAvatarUri by mutableStateOf<String?>(null)
        private set
    private var newAvatarUri: String? = null

    init {
        viewModelScope.launch {
            try { profile = getProfile() }
            catch (e: Exception) { errorMessage = "Не удалось прочитать профиль: ${e.message}" }
            finally { isLoading = false }
        }
    }

    fun beginEdit() {
        draftName = profile.fullName
        draftPosition = profile.position
        draftResumeUrl = profile.resumeUrl
        draftAvatarUri = profile.avatarUri
        newAvatarUri = null
        errorMessage = null
    }

    fun onNameChange(value: String) { draftName = value }
    fun onPositionChange(value: String) { draftPosition = value }
    fun onResumeUrlChange(value: String) { draftResumeUrl = value }
    fun clearError() { errorMessage = null }

    fun onImageSelected(uri: Uri) {
        isImportingAvatar = true
        viewModelScope.launch {
            try {
                val stagedUri = stageAvatar(uri)
                draftAvatarUri = stagedUri
                newAvatarUri = stagedUri
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = "Не удалось прочитать фотографию: ${e.message}"
            } finally {
                isImportingAvatar = false
            }
        }
    }

    fun save(onSaved: () -> Unit) {
        if (isSaving || isImportingAvatar) return
        if (draftResumeUrl.isNotBlank() &&
            !(draftResumeUrl.startsWith("https://", ignoreCase = true) ||
                draftResumeUrl.startsWith("www.", ignoreCase = true))
        ) {
            errorMessage = "Для резюме укажите HTTPS-ссылку на файл"
            return
        }
        isSaving = true
        viewModelScope.launch {
            try {
                val updated = UserProfile(
                    fullName = draftName.trim(),
                    position = draftPosition.trim(),
                    resumeUrl = draftResumeUrl.trim(),
                    avatarUri = profile.avatarUri
                )
                profile = saveProfile(updated, newAvatarUri)
                avatarRevision++
                errorMessage = null
                onSaved()
            } catch (e: Exception) {
                errorMessage = "Не удалось сохранить профиль: ${e.message}"
            } finally {
                isSaving = false
            }
        }
    }

    fun openResume(onDownloaded: (DownloadedResume) -> Unit) {
        if (isDownloading || profile.resumeUrl.isBlank()) return
        isDownloading = true
        viewModelScope.launch {
            try {
                val document = downloadResume(profile.resumeUrl)
                errorMessage = null
                onDownloaded(document)
            } catch (e: Exception) {
                errorMessage = "Не удалось открыть резюме: ${e.message}"
            } finally {
                isDownloading = false
            }
        }
    }
}
