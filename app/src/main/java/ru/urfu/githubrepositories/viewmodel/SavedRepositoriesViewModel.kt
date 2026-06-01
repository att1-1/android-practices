package ru.urfu.githubrepositories.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.urfu.githubrepositories.domain.usecase.ObserveFavoriteRepositoriesUseCase
import ru.urfu.githubrepositories.presentation.repositories.SavedRepositoriesUiState

class SavedRepositoriesViewModel(
    private val observeFavoriteRepositoriesUseCase: ObserveFavoriteRepositoriesUseCase
) : ViewModel() {
    var savedRepositoriesState by mutableStateOf<SavedRepositoriesUiState>(
        SavedRepositoriesUiState.Loading
    )
        private set

    init {
        viewModelScope.launch {
            observeFavoriteRepositoriesUseCase().collect { repositories ->
                savedRepositoriesState = SavedRepositoriesUiState.Success(repositories)
            }
        }
    }
}
