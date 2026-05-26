package ru.urfu.githubrepositories.data.repository

import kotlinx.coroutines.flow.Flow
import ru.urfu.githubrepositories.data.local.preferences.RepositoryFiltersDataStore
import ru.urfu.githubrepositories.domain.model.RepositoryFilters
import ru.urfu.githubrepositories.domain.repository.RepositoryFiltersRepository

class RepositoryFiltersRepositoryImpl(
    private val filtersDataStore: RepositoryFiltersDataStore
) : RepositoryFiltersRepository {
    override fun observeFilters(): Flow<RepositoryFilters> {
        return filtersDataStore.filters
    }

    override suspend fun saveFilters(filters: RepositoryFilters) {
        filtersDataStore.saveFilters(filters)
    }
}
