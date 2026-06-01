package ru.urfu.githubrepositories.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import retrofit2.HttpException
import ru.urfu.githubrepositories.data.cache.FiltersBadgeCache
import ru.urfu.githubrepositories.domain.model.GitHubRepository
import ru.urfu.githubrepositories.domain.model.RepositoryFilters
import ru.urfu.githubrepositories.domain.usecase.AddFavoriteRepositoryUseCase
import ru.urfu.githubrepositories.domain.usecase.GetRepositoryDetailsUseCase
import ru.urfu.githubrepositories.domain.usecase.GetRepositoryFiltersUseCase
import ru.urfu.githubrepositories.domain.usecase.ObserveIsFavoriteUseCase
import ru.urfu.githubrepositories.domain.usecase.RemoveFavoriteRepositoryUseCase
import ru.urfu.githubrepositories.domain.usecase.SearchRepositoriesUseCase
import ru.urfu.githubrepositories.presentation.repositories.RepositoriesUiState
import ru.urfu.githubrepositories.presentation.repositories.RepositoryDetailsUiState
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class RepositoriesViewModel(
    private val searchRepositoriesUseCase: SearchRepositoriesUseCase,
    private val getRepositoryDetailsUseCase: GetRepositoryDetailsUseCase,
    private val getRepositoryFiltersUseCase: GetRepositoryFiltersUseCase,
    private val observeIsFavoriteUseCase: ObserveIsFavoriteUseCase,
    private val addFavoriteRepositoryUseCase: AddFavoriteRepositoryUseCase,
    private val removeFavoriteRepositoryUseCase: RemoveFavoriteRepositoryUseCase,
    private val filtersBadgeCache: FiltersBadgeCache
) : ViewModel() {
    var repositoriesState by mutableStateOf<RepositoriesUiState>(RepositoriesUiState.Loading)
        private set

    var repositoryDetailsState by mutableStateOf<RepositoryDetailsUiState>(
        RepositoryDetailsUiState.Loading
    )
        private set

    var hasActiveFilters by mutableStateOf(false)
        private set

    private var currentFilters = RepositoryFilters()
    private var favoriteJob: Job? = null

    init {
        observeFilters()
        observeFiltersBadge()
    }

    fun loadRepositories() {
        loadRepositories(currentFilters)
    }

    private fun observeFilters() {
        viewModelScope.launch {
            getRepositoryFiltersUseCase()
                .distinctUntilChanged()
                .collect { filters ->
                    currentFilters = filters
                    filtersBadgeCache.update(filters)
                    loadRepositories(filters)
                }
        }
    }

    private fun observeFiltersBadge() {
        viewModelScope.launch {
            filtersBadgeCache.hasActiveFilters.collect { hasFilters ->
                hasActiveFilters = hasFilters
            }
        }
    }

    private fun loadRepositories(filters: RepositoryFilters) {
        repositoriesState = RepositoriesUiState.Loading

        viewModelScope.launch {
            repositoriesState = try {
                RepositoriesUiState.Success(searchRepositoriesUseCase(filters))
            } catch (throwable: Throwable) {
                RepositoriesUiState.Error(throwable.toUserMessage())
            }
        }
    }

    fun loadRepositoryDetails(owner: String, repo: String) {
        repositoryDetailsState = RepositoryDetailsUiState.Loading
        favoriteJob?.cancel()

        viewModelScope.launch {
            try {
                val repository = getRepositoryDetailsUseCase(owner, repo)
                repositoryDetailsState = RepositoryDetailsUiState.Success(
                    repository = repository,
                    isFavorite = false
                )
                observeFavoriteState(repository)
            } catch (throwable: Throwable) {
                repositoryDetailsState = RepositoryDetailsUiState.Error(throwable.toUserMessage())
            }
        }
    }

    fun toggleFavorite() {
        val state = repositoryDetailsState as? RepositoryDetailsUiState.Success ?: return

        viewModelScope.launch {
            if (state.isFavorite) {
                removeFavoriteRepositoryUseCase(state.repository.id)
            } else {
                addFavoriteRepositoryUseCase(state.repository)
            }
        }
    }

    private fun observeFavoriteState(repository: GitHubRepository) {
        favoriteJob = viewModelScope.launch {
            observeIsFavoriteUseCase(repository.id).collect { isFavorite ->
                repositoryDetailsState = RepositoryDetailsUiState.Success(
                    repository = repository,
                    isFavorite = isFavorite
                )
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
}
