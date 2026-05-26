package ru.urfu.githubrepositories.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.urfu.githubrepositories.domain.model.RepositoryFilters

private val Context.repositoryFiltersDataStore by preferencesDataStore(
    name = "repository_filters"
)

class RepositoryFiltersDataStore(
    private val context: Context
) {
    val filters: Flow<RepositoryFilters> = context.repositoryFiltersDataStore.data.map { preferences ->
        RepositoryFilters(
            queryText = preferences[QUERY_TEXT_KEY] ?: RepositoryFilters.DEFAULT_QUERY_TEXT,
            language = preferences[LANGUAGE_KEY] ?: RepositoryFilters.DEFAULT_LANGUAGE,
            minStars = preferences[MIN_STARS_KEY] ?: RepositoryFilters.DEFAULT_MIN_STARS
        )
    }

    suspend fun saveFilters(filters: RepositoryFilters) {
        context.repositoryFiltersDataStore.edit { preferences ->
            preferences[QUERY_TEXT_KEY] = filters.queryText.ifBlank {
                RepositoryFilters.DEFAULT_QUERY_TEXT
            }
            preferences[LANGUAGE_KEY] = filters.language
            preferences[MIN_STARS_KEY] = filters.minStars.coerceAtLeast(0)
        }
    }

    private companion object {
        val QUERY_TEXT_KEY = stringPreferencesKey("query_text")
        val LANGUAGE_KEY = stringPreferencesKey("language")
        val MIN_STARS_KEY = intPreferencesKey("min_stars")
    }
}
