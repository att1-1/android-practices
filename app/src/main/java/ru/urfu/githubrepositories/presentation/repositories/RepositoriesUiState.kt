package ru.urfu.githubrepositories.presentation.repositories

import ru.urfu.githubrepositories.domain.model.GitHubRepository

sealed interface RepositoriesUiState {
    data object Loading : RepositoriesUiState

    data class Success(
        val repositories: List<GitHubRepository>
    ) : RepositoriesUiState

    data class Error(
        val message: String
    ) : RepositoriesUiState
}
