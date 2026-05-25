package ru.urfu.githubrepositories.domain.usecase

import ru.urfu.githubrepositories.domain.model.GitHubRepository
import ru.urfu.githubrepositories.domain.repository.GitHubRepositoriesRepository

class GetRepositoryDetailsUseCase(
    private val repository: GitHubRepositoriesRepository
) {
    suspend operator fun invoke(owner: String, repo: String): GitHubRepository {
        return repository.getRepositoryDetails(owner, repo)
    }
}
