package ru.urfu.githubrepositories.domain.model

data class GitHubRepository(
    val id: Long,
    val owner: String,
    val name: String,
    val description: String,
    val language: String,
    val stars: Int,
    val forks: Int,
    val openIssues: Int,
    val license: String,
    val defaultBranch: String,
    val updatedAt: String,
    val topics: List<String>
)
