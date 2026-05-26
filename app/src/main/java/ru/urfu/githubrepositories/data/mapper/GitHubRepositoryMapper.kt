package ru.urfu.githubrepositories.data.mapper

import ru.urfu.githubrepositories.data.remote.dto.GitHubRepositoryDto
import ru.urfu.githubrepositories.domain.model.GitHubRepository

fun GitHubRepositoryDto.toDomain(): GitHubRepository {
    return GitHubRepository(
        id = id ?: 0L,
        owner = owner?.login.orEmpty(),
        name = name.orEmpty(),
        description = description ?: "Описание не указано",
        language = language ?: "Не указан",
        stars = stars ?: 0,
        forks = forks ?: 0,
        openIssues = openIssues ?: 0,
        license = license?.name ?: "No license",
        defaultBranch = defaultBranch ?: "main",
        updatedAt = updatedAt?.take(10) ?: "Не указано",
        topics = topics.orEmpty()
    )
}
