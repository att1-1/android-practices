package ru.urfu.githubrepositories.presentation.repositories

import ru.urfu.githubrepositories.domain.model.GitHubRepository

sealed interface RepositoryDetailsUiState {
    data object Loading : RepositoryDetailsUiState

    data class Success(
        val repository: GitHubRepository,
        val isFavorite: Boolean
    ) : RepositoryDetailsUiState

    data class Error(
        val message: String
    ) : RepositoryDetailsUiState
}
