package ru.urfu.githubrepositories.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.urfu.githubrepositories.domain.repository.GitHubRepositoriesRepository

class ObserveIsFavoriteUseCase(
    private val repository: GitHubRepositoriesRepository
) {
    operator fun invoke(id: Long): Flow<Boolean> {
        return repository.observeIsFavorite(id)
    }
}
