package ru.urfu.githubrepositories.domain.repository

import ru.urfu.githubrepositories.domain.model.GitHubRepository

interface GitHubRepositoriesRepository {
    suspend fun searchRepositories(query: String): List<GitHubRepository>

    suspend fun getRepositoryDetails(owner: String, repo: String): GitHubRepository
}
