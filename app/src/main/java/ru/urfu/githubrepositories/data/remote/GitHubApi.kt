package ru.urfu.githubrepositories.data.remote

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query
import ru.urfu.githubrepositories.data.remote.dto.GitHubRepositoryDto
import ru.urfu.githubrepositories.data.remote.dto.SearchRepositoriesResponseDto

interface GitHubApi {
    @Headers(
        "Accept: application/vnd.github+json",
        "X-GitHub-Api-Version: 2022-11-28"
    )
    @GET("search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String,
        @Query("per_page") perPage: Int = 20
    ): SearchRepositoriesResponseDto

    @Headers(
        "Accept: application/vnd.github+json",
        "X-GitHub-Api-Version: 2022-11-28"
    )
    @GET("repos/{owner}/{repo}")
    suspend fun getRepositoryDetails(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GitHubRepositoryDto
}
