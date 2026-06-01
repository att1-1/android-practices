package ru.urfu.githubrepositories.domain.usecase

import ru.urfu.githubrepositories.domain.model.GitHubRepository
import ru.urfu.githubrepositories.domain.model.RepositoryFilters
import ru.urfu.githubrepositories.domain.repository.GitHubRepositoriesRepository

class SearchRepositoriesUseCase(
    private val repository: GitHubRepositoriesRepository
) {
    suspend operator fun invoke(filters: RepositoryFilters): List<GitHubRepository> {
        return repository.searchRepositories(filters.toGitHubQuery())
    }
}
