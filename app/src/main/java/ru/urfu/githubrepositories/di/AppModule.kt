package ru.urfu.githubrepositories.di

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.urfu.githubrepositories.data.cache.FiltersBadgeCache
import ru.urfu.githubrepositories.data.local.database.AppDatabase
import ru.urfu.githubrepositories.data.local.preferences.RepositoryFiltersDataStore
import ru.urfu.githubrepositories.data.remote.RetrofitClient
import ru.urfu.githubrepositories.data.repository.GitHubRepositoriesRepositoryImpl
import ru.urfu.githubrepositories.data.repository.RepositoryFiltersRepositoryImpl
import ru.urfu.githubrepositories.domain.repository.GitHubRepositoriesRepository
import ru.urfu.githubrepositories.domain.repository.RepositoryFiltersRepository
import ru.urfu.githubrepositories.domain.usecase.AddFavoriteRepositoryUseCase
import ru.urfu.githubrepositories.domain.usecase.GetRepositoryDetailsUseCase
import ru.urfu.githubrepositories.domain.usecase.GetRepositoryFiltersUseCase
import ru.urfu.githubrepositories.domain.usecase.ObserveFavoriteRepositoriesUseCase
import ru.urfu.githubrepositories.domain.usecase.ObserveIsFavoriteUseCase
import ru.urfu.githubrepositories.domain.usecase.RemoveFavoriteRepositoryUseCase
import ru.urfu.githubrepositories.domain.usecase.SaveRepositoryFiltersUseCase
import ru.urfu.githubrepositories.domain.usecase.SearchRepositoriesUseCase
import ru.urfu.githubrepositories.viewmodel.FiltersViewModel
import ru.urfu.githubrepositories.viewmodel.RepositoriesViewModel
import ru.urfu.githubrepositories.viewmodel.SavedRepositoriesViewModel

val appModule = module {
    single { RetrofitClient.gitHubApi }

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "github_repositories.db"
        ).build()
    }
    single { get<AppDatabase>().favoriteRepositoryDao() }

    single { RepositoryFiltersDataStore(androidContext()) }
    single { FiltersBadgeCache() }

    single<GitHubRepositoriesRepository> {
        GitHubRepositoriesRepositoryImpl(
            api = get(),
            favoriteRepositoryDao = get()
        )
    }
    single<RepositoryFiltersRepository> {
        RepositoryFiltersRepositoryImpl(filtersDataStore = get())
    }

    factory { SearchRepositoriesUseCase(repository = get()) }
    factory { GetRepositoryDetailsUseCase(repository = get()) }
    factory { GetRepositoryFiltersUseCase(repository = get()) }
    factory { SaveRepositoryFiltersUseCase(repository = get()) }
    factory { ObserveFavoriteRepositoriesUseCase(repository = get()) }
    factory { ObserveIsFavoriteUseCase(repository = get()) }
    factory { AddFavoriteRepositoryUseCase(repository = get()) }
    factory { RemoveFavoriteRepositoryUseCase(repository = get()) }

    viewModel {
        RepositoriesViewModel(
            searchRepositoriesUseCase = get(),
            getRepositoryDetailsUseCase = get(),
            getRepositoryFiltersUseCase = get(),
            observeIsFavoriteUseCase = get(),
            addFavoriteRepositoryUseCase = get(),
            removeFavoriteRepositoryUseCase = get(),
            filtersBadgeCache = get()
        )
    }
    viewModel {
        FiltersViewModel(
            getRepositoryFiltersUseCase = get(),
            saveRepositoryFiltersUseCase = get(),
            filtersBadgeCache = get()
        )
    }
    viewModel {
        SavedRepositoriesViewModel(
            observeFavoriteRepositoriesUseCase = get()
        )
    }
}
