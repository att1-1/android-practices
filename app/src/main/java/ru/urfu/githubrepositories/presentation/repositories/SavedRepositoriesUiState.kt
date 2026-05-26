package ru.urfu.githubrepositories.presentation.repositories

import ru.urfu.githubrepositories.domain.model.GitHubRepository

sealed interface SavedRepositoriesUiState {
    data object Loading : SavedRepositoriesUiState

    data class Success(
        val repositories: List<GitHubRepository>
    ) : SavedRepositoriesUiState
}
