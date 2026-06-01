package ru.urfu.githubrepositories.domain.usecase

import ru.urfu.githubrepositories.domain.repository.GitHubRepositoriesRepository

class RemoveFavoriteRepositoryUseCase(
    private val repository: GitHubRepositoriesRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.removeFavoriteRepository(id)
    }
}
