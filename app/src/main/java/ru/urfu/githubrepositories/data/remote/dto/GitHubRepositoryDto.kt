package ru.urfu.githubrepositories.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GitHubRepositoryDto(
    @SerializedName("id")
    val id: Long?,
    @SerializedName("owner")
    val owner: OwnerDto?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("language")
    val language: String?,
    @SerializedName("stargazers_count")
    val stars: Int?,
    @SerializedName("forks_count")
    val forks: Int?,
    @SerializedName("open_issues_count")
    val openIssues: Int?,
    @SerializedName("license")
    val license: LicenseDto?,
    @SerializedName("default_branch")
    val defaultBranch: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("topics")
    val topics: List<String>?
)

data class OwnerDto(
    @SerializedName("login")
    val login: String?
)

data class LicenseDto(
    @SerializedName("name")
    val name: String?
)
