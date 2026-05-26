package ru.urfu.githubrepositories.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.urfu.githubrepositories.domain.model.RepositoryFilters
import ru.urfu.githubrepositories.domain.repository.RepositoryFiltersRepository

class GetRepositoryFiltersUseCase(
    private val repository: RepositoryFiltersRepository
) {
    operator fun invoke(): Flow<RepositoryFilters> {
        return repository.observeFilters()
    }
}
