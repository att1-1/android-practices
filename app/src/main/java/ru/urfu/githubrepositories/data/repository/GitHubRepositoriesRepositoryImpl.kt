package ru.urfu.githubrepositories.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.urfu.githubrepositories.data.mapper.toDomain
import ru.urfu.githubrepositories.data.remote.GitHubApi
import ru.urfu.githubrepositories.domain.model.GitHubRepository
import ru.urfu.githubrepositories.domain.repository.GitHubRepositoriesRepository

class GitHubRepositoriesRepositoryImpl(
    private val api: GitHubApi
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
}
