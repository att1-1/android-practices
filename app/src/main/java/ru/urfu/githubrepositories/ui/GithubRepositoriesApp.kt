package ru.urfu.githubrepositories.ui

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.urfu.githubrepositories.ui.filters.FiltersRoute
import ru.urfu.githubrepositories.ui.repositories.RepositoriesRoute
import ru.urfu.githubrepositories.ui.repositories.RepositoryDetailsRoute
import ru.urfu.githubrepositories.ui.saved.SavedRepositoriesRoute
import ru.urfu.githubrepositories.ui.profile.ProfileScreen
import ru.urfu.githubrepositories.ui.profile.ProfileEditScreen

private object Routes {
    const val Repositories = "repositories"
    const val Saved = "saved"
    const val Profile = "profile"
    const val ProfileEdit = "profile/edit"
    const val Filters = "filters"
    const val Owner = "owner"
    const val Repo = "repo"
    const val RepositoryDetailsRoute = "repositoryDetails/{owner}/{repo}"

    fun repositoryDetails(owner: String, repo: String): String {
        return "repositoryDetails/${Uri.encode(owner)}/${Uri.encode(repo)}"
    }
}

private data class BottomTab(
    val route: String,
    val label: String,
    val iconText: String
)

private val bottomTabs = listOf(
    BottomTab(Routes.Repositories, "Репозитории", "Р"),
    BottomTab(Routes.Saved, "Сохраненные", "С"),
    BottomTab(Routes.Profile, "Профиль", "П")
)

@Composable
fun GithubRepositoriesApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val selectedTabRoute = when (currentRoute) {
        Routes.RepositoryDetailsRoute, Routes.Filters -> Routes.Repositories
        Routes.ProfileEdit -> Routes.Profile
        else -> currentRoute
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                bottomTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTabRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(tab.iconText) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Repositories,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.Repositories) {
                RepositoriesRoute(
                    onRepositoryClick = { repository ->
                        navController.navigate(
                            Routes.repositoryDetails(
                                owner = repository.owner,
                                repo = repository.name
                            )
                        )
                    },
                    onFiltersClick = { navController.navigate(Routes.Filters) }
                )
            }
            composable(Routes.Saved) {
                SavedRepositoriesRoute()
            }
            composable(Routes.Profile) {
                ProfileScreen(
                    onEdit = { navController.navigate(Routes.ProfileEdit) }
                )
            }
            composable(Routes.ProfileEdit) {
                ProfileEditScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable(Routes.Filters) {
                FiltersRoute(
                    onSaved = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = Routes.RepositoryDetailsRoute,
                arguments = listOf(
                    navArgument(Routes.Owner) {
                        type = NavType.StringType
                    },
                    navArgument(Routes.Repo) {
                        type = NavType.StringType
                    }
                )
            ) { entry ->
                val owner = entry.arguments?.getString(Routes.Owner).orEmpty()
                val repo = entry.arguments?.getString(Routes.Repo).orEmpty()

                RepositoryDetailsRoute(
                    owner = owner,
                    repo = repo,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
