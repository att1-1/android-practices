package ru.urfu.githubrepositories.domain.usecase

import ru.urfu.githubrepositories.domain.model.GitHubRepository
import ru.urfu.githubrepositories.domain.repository.GitHubRepositoriesRepository

class SearchRepositoriesUseCase(
    private val repository: GitHubRepositoriesRepository
) {
    suspend operator fun invoke(query: String = DEFAULT_QUERY): List<GitHubRepository> {
        return repository.searchRepositories(query)
    }

    companion object {
        const val DEFAULT_QUERY = "android language:kotlin"
    }
}
