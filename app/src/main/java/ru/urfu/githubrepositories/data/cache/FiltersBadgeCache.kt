package ru.urfu.githubrepositories.data.cache

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.urfu.githubrepositories.domain.model.RepositoryFilters

class FiltersBadgeCache {
    private val _hasActiveFilters = MutableStateFlow(false)
    val hasActiveFilters: StateFlow<Boolean> = _hasActiveFilters

    fun update(filters: RepositoryFilters) {
        _hasActiveFilters.value = !filters.isDefault()
    }
}
