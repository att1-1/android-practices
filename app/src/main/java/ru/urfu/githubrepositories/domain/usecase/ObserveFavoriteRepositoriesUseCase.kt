package ru.urfu.githubrepositories.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.urfu.githubrepositories.domain.model.GitHubRepository
import ru.urfu.githubrepositories.domain.repository.GitHubRepositoriesRepository

class ObserveFavoriteRepositoriesUseCase(
    private val repository: GitHubRepositoriesRepository
) {
    operator fun invoke(): Flow<List<GitHubRepository>> {
        return repository.observeFavoriteRepositories()
    }
}
