package ru.urfu.githubrepositories.data.mapper

import ru.urfu.githubrepositories.data.local.favorite.FavoriteRepositoryEntity
import ru.urfu.githubrepositories.domain.model.GitHubRepository

fun GitHubRepository.toFavoriteEntity(): FavoriteRepositoryEntity {
    return FavoriteRepositoryEntity(
        id = id,
        owner = owner,
        name = name,
        description = description,
        language = language,
        stars = stars,
        forks = forks,
        openIssues = openIssues,
        license = license,
        defaultBranch = defaultBranch,
        updatedAt = updatedAt,
        topics = topics.joinToString(separator = ",")
    )
}

fun FavoriteRepositoryEntity.toDomain(): GitHubRepository {
    return GitHubRepository(
        id = id,
        owner = owner,
        name = name,
        description = description,
        language = language,
        stars = stars,
        forks = forks,
        openIssues = openIssues,
        license = license,
        defaultBranch = defaultBranch,
        updatedAt = updatedAt,
        topics = topics.split(",").filter { topic -> topic.isNotBlank() }
    )
}
