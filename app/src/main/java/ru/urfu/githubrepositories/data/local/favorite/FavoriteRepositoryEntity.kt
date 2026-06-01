package ru.urfu.githubrepositories.data.local.favorite

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_repositories")
data class FavoriteRepositoryEntity(
    @PrimaryKey
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
    val topics: String
)
