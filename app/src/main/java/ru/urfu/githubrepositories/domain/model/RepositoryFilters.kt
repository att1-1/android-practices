package ru.urfu.githubrepositories.domain.model

data class RepositoryFilters(
    val queryText: String = DEFAULT_QUERY_TEXT,
    val language: String = DEFAULT_LANGUAGE,
    val minStars: Int = DEFAULT_MIN_STARS
) {
    fun isDefault(): Boolean {
        return queryText == DEFAULT_QUERY_TEXT &&
            language == DEFAULT_LANGUAGE &&
            minStars == DEFAULT_MIN_STARS
    }

    fun toGitHubQuery(): String {
        val parts = mutableListOf<String>()
        val safeQuery = queryText.ifBlank { DEFAULT_QUERY_TEXT }

        parts += safeQuery

        if (language.isNotBlank()) {
            parts += "language:${language.lowercase()}"
        }

        if (minStars > 0) {
            parts += "stars:>$minStars"
        }

        return parts.joinToString(separator = " ")
    }

    companion object {
        const val DEFAULT_QUERY_TEXT = "android"
        const val DEFAULT_LANGUAGE = "Kotlin"
        const val DEFAULT_MIN_STARS = 0
    }
}
