package ru.urfu.githubrepositories.viewmodel

import androidx.lifecycle.ViewModel
import ru.urfu.githubrepositories.data.GitHubRepository
import ru.urfu.githubrepositories.data.MockRepositories

class RepositoriesViewModel : ViewModel() {
    val repositories: List<GitHubRepository> = MockRepositories.repositories

    fun getRepositoryById(id: Long): GitHubRepository? {
        return repositories.firstOrNull { repository -> repository.id == id }
    }
}
