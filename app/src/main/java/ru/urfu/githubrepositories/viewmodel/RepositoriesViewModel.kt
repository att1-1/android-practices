package ru.urfu.githubrepositories.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import ru.urfu.githubrepositories.data.remote.RetrofitClient
import ru.urfu.githubrepositories.data.repository.GitHubRepositoriesRepositoryImpl
import ru.urfu.githubrepositories.domain.usecase.GetRepositoryDetailsUseCase
import ru.urfu.githubrepositories.domain.usecase.SearchRepositoriesUseCase
import ru.urfu.githubrepositories.presentation.repositories.RepositoriesUiState
import ru.urfu.githubrepositories.presentation.repositories.RepositoryDetailsUiState
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class RepositoriesViewModel(
    private val searchRepositoriesUseCase: SearchRepositoriesUseCase,
    private val getRepositoryDetailsUseCase: GetRepositoryDetailsUseCase
) : ViewModel() {
    var repositoriesState by mutableStateOf<RepositoriesUiState>(RepositoriesUiState.Loading)
        private set

    var repositoryDetailsState by mutableStateOf<RepositoryDetailsUiState>(
        RepositoryDetailsUiState.Loading
    )
        private set

    init {
        loadRepositories()
    }

    fun loadRepositories(query: String = SearchRepositoriesUseCase.DEFAULT_QUERY) {
        repositoriesState = RepositoriesUiState.Loading

        viewModelScope.launch {
            repositoriesState = try {
                RepositoriesUiState.Success(searchRepositoriesUseCase(query))
            } catch (throwable: Throwable) {
                RepositoriesUiState.Error(throwable.toUserMessage())
            }
        }
    }

    fun loadRepositoryDetails(owner: String, repo: String) {
        repositoryDetailsState = RepositoryDetailsUiState.Loading

        viewModelScope.launch {
            repositoryDetailsState = try {
                RepositoryDetailsUiState.Success(getRepositoryDetailsUseCase(owner, repo))
            } catch (throwable: Throwable) {
                RepositoryDetailsUiState.Error(throwable.toUserMessage())
            }
        }
    }

    private fun Throwable.toUserMessage(): String {
        return when (this) {
            is UnknownHostException -> "Нет подключения к интернету"
            is SocketTimeoutException -> "Превышено время ожидания"
            is HttpException -> "Ошибка сервера: ${code()}"
            else -> message ?: "Не удалось загрузить данные"
        }
    }

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = GitHubRepositoriesRepositoryImpl(RetrofitClient.gitHubApi)

                return RepositoriesViewModel(
                    searchRepositoriesUseCase = SearchRepositoriesUseCase(repository),
                    getRepositoryDetailsUseCase = GetRepositoryDetailsUseCase(repository)
                ) as T
            }
        }
    }
}
