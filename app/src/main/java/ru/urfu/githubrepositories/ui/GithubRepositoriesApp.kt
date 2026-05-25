package ru.urfu.githubrepositories.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.urfu.githubrepositories.ui.repositories.RepositoriesScreen
import ru.urfu.githubrepositories.ui.repositories.RepositoryDetailsScreen
import ru.urfu.githubrepositories.viewmodel.RepositoriesViewModel

private object Routes {
    const val Repositories = "repositories"
    const val Saved = "saved"
    const val Profile = "profile"
    const val RepositoryId = "repositoryId"
    const val RepositoryDetailsRoute = "repositoryDetails/{repositoryId}"

    fun repositoryDetails(repositoryId: Long): String {
        return "repositoryDetails/$repositoryId"
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
    val repositoriesViewModel: RepositoriesViewModel = viewModel()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val selectedTabRoute = when (currentRoute) {
        Routes.RepositoryDetailsRoute -> Routes.Repositories
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
                RepositoriesScreen(
                    repositories = repositoriesViewModel.repositories,
                    onRepositoryClick = { repository ->
                        navController.navigate(Routes.repositoryDetails(repository.id))
                    }
                )
            }
            composable(Routes.Saved) {
                PlaceholderScreen(title = "Сохраненные")
            }
            composable(Routes.Profile) {
                PlaceholderScreen(title = "Профиль")
            }
            composable(
                route = Routes.RepositoryDetailsRoute,
                arguments = listOf(
                    navArgument(Routes.RepositoryId) {
                        type = NavType.LongType
                    }
                )
            ) { entry ->
                val repositoryId = entry.arguments?.getLong(Routes.RepositoryId) ?: -1L

                RepositoryDetailsScreen(
                    repository = repositoriesViewModel.getRepositoryById(repositoryId),
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
