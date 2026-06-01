package ru.urfu.githubrepositories.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.urfu.githubrepositories.domain.model.GitHubRepository

interface GitHubRepositoriesRepository {
    suspend fun searchRepositories(query: String): List<GitHubRepository>

    suspend fun getRepositoryDetails(owner: String, repo: String): GitHubRepository

    fun observeFavoriteRepositories(): Flow<List<GitHubRepository>>

    fun observeIsFavorite(id: Long): Flow<Boolean>

    suspend fun addFavoriteRepository(repository: GitHubRepository)

    suspend fun removeFavoriteRepository(id: Long)
}
