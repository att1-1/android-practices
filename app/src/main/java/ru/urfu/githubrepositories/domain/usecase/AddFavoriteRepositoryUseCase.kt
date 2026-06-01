package ru.urfu.githubrepositories.domain.usecase

import ru.urfu.githubrepositories.domain.model.GitHubRepository
import ru.urfu.githubrepositories.domain.repository.GitHubRepositoriesRepository

class AddFavoriteRepositoryUseCase(
    private val repository: GitHubRepositoriesRepository
) {
    suspend operator fun invoke(gitHubRepository: GitHubRepository) {
        repository.addFavoriteRepository(gitHubRepository)
    }
}
