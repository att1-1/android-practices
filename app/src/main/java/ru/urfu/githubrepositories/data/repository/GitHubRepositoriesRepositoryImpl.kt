package ru.urfu.githubrepositories.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.urfu.githubrepositories.data.mapper.toDomain
import ru.urfu.githubrepositories.data.mapper.toFavoriteEntity
import ru.urfu.githubrepositories.data.local.favorite.FavoriteRepositoryDao
import ru.urfu.githubrepositories.data.remote.GitHubApi
import ru.urfu.githubrepositories.domain.model.GitHubRepository
import ru.urfu.githubrepositories.domain.repository.GitHubRepositoriesRepository

class GitHubRepositoriesRepositoryImpl(
    private val api: GitHubApi,
    private val favoriteRepositoryDao: FavoriteRepositoryDao
) : GitHubRepositoriesRepository {
    override suspend fun searchRepositories(query: String): List<GitHubRepository> {
        return withContext(Dispatchers.IO) {
            api.searchRepositories(query).items.orEmpty().map { repositoryDto ->
                repositoryDto.toDomain()
            }
        }
    }

    override suspend fun getRepositoryDetails(owner: String, repo: String): GitHubRepository {
        return withContext(Dispatchers.IO) {
            api.getRepositoryDetails(owner, repo).toDomain()
        }
    }

    override fun observeFavoriteRepositories(): Flow<List<GitHubRepository>> {
        return favoriteRepositoryDao.observeFavorites().map { favorites ->
            favorites.map { favorite -> favorite.toDomain() }
        }
    }

    override fun observeIsFavorite(id: Long): Flow<Boolean> {
        return favoriteRepositoryDao.observeIsFavorite(id)
    }

    override suspend fun addFavoriteRepository(repository: GitHubRepository) {
        withContext(Dispatchers.IO) {
            favoriteRepositoryDao.insertFavorite(repository.toFavoriteEntity())
        }
    }

    override suspend fun removeFavoriteRepository(id: Long) {
        withContext(Dispatchers.IO) {
            favoriteRepositoryDao.deleteFavoriteById(id)
        }
    }
}
