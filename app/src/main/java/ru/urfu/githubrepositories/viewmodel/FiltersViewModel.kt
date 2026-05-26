package ru.urfu.githubrepositories.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.urfu.githubrepositories.data.cache.FiltersBadgeCache
import ru.urfu.githubrepositories.domain.model.RepositoryFilters
import ru.urfu.githubrepositories.domain.usecase.GetRepositoryFiltersUseCase
import ru.urfu.githubrepositories.domain.usecase.SaveRepositoryFiltersUseCase

class FiltersViewModel(
    private val getRepositoryFiltersUseCase: GetRepositoryFiltersUseCase,
    private val saveRepositoryFiltersUseCase: SaveRepositoryFiltersUseCase,
    private val filtersBadgeCache: FiltersBadgeCache
) : ViewModel() {
    var queryText by mutableStateOf(RepositoryFilters.DEFAULT_QUERY_TEXT)
        private set

    var language by mutableStateOf(RepositoryFilters.DEFAULT_LANGUAGE)
        private set

    var minStars by mutableStateOf(RepositoryFilters.DEFAULT_MIN_STARS.toString())
        private set

    init {
        viewModelScope.launch {
            getRepositoryFiltersUseCase().collect { filters ->
                queryText = filters.queryText
                language = filters.language
                minStars = filters.minStars.toString()
                filtersBadgeCache.update(filters)
            }
        }
    }

    fun onQueryTextChange(value: String) {
        queryText = value
    }

    fun onLanguageChange(value: String) {
        language = value
    }

    fun onMinStarsChange(value: String) {
        minStars = value.filter { char -> char.isDigit() }
    }

    fun saveFilters(onSaved: () -> Unit) {
        val filters = RepositoryFilters(
            queryText = queryText.ifBlank { RepositoryFilters.DEFAULT_QUERY_TEXT },
            language = language.trim(),
            minStars = minStars.toIntOrNull() ?: RepositoryFilters.DEFAULT_MIN_STARS
        )

        viewModelScope.launch {
            saveRepositoryFiltersUseCase(filters)
            filtersBadgeCache.update(filters)
            onSaved()
        }
    }
}
