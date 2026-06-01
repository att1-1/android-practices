package ru.urfu.githubrepositories.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.urfu.githubrepositories.domain.model.RepositoryFilters

interface RepositoryFiltersRepository {
    fun observeFilters(): Flow<RepositoryFilters>

    suspend fun saveFilters(filters: RepositoryFilters)
}
