package ru.urfu.githubrepositories.domain.usecase

import ru.urfu.githubrepositories.domain.model.RepositoryFilters
import ru.urfu.githubrepositories.domain.repository.RepositoryFiltersRepository

class SaveRepositoryFiltersUseCase(
    private val repository: RepositoryFiltersRepository
) {
    suspend operator fun invoke(filters: RepositoryFilters) {
        repository.saveFilters(filters)
    }
}
