package ru.urfu.githubrepositories.ui.repositories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.urfu.githubrepositories.domain.model.GitHubRepository
import ru.urfu.githubrepositories.presentation.repositories.RepositoriesUiState

@Composable
fun RepositoriesScreen(
    state: RepositoriesUiState,
    hasActiveFilters: Boolean,
    onRepositoryClick: (GitHubRepository) -> Unit,
    onRetryClick: () -> Unit,
    onFiltersClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        RepositoriesUiState.Loading -> LoadingContent(modifier = modifier)
        is RepositoriesUiState.Error -> ErrorContent(
            message = state.message,
            onRetryClick = onRetryClick,
            onFiltersClick = onFiltersClick,
            hasActiveFilters = hasActiveFilters,
            modifier = modifier
        )

        is RepositoriesUiState.Success -> RepositoriesList(
            repositories = state.repositories,
            hasActiveFilters = hasActiveFilters,
            onRepositoryClick = onRepositoryClick,
            onFiltersClick = onFiltersClick,
            modifier = modifier
        )
    }
}

@Composable
private fun RepositoriesList(
    repositories: List<GitHubRepository>,
    hasActiveFilters: Boolean,
    onRepositoryClick: (GitHubRepository) -> Unit,
    onFiltersClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            RepositoriesHeader(
                hasActiveFilters = hasActiveFilters,
                onFiltersClick = onFiltersClick
            )
        }

        if (repositories.isEmpty()) {
            item {
                Text(
                    text = "Репозитории не найдены",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        items(
            items = repositories,
            key = { repository -> repository.id }
        ) { repository ->
            RepositoryCard(
                repository = repository,
                onClick = { onRepositoryClick(repository) }
            )
        }
    }
}

@Composable
private fun RepositoriesHeader(
    hasActiveFilters: Boolean,
    onFiltersClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Репозитории",
            style = MaterialTheme.typography.headlineMedium
        )
        Button(onClick = onFiltersClick) {
            Text(text = if (hasActiveFilters) "Фильтры •" else "Фильтры")
        }
    }
}

@Composable
private fun RepositoryCard(
    repository: GitHubRepository,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = repository.name,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = repository.owner,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = repository.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Row {
                Text(
                    text = repository.language,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Звезды: ${repository.stars}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Форки: ${repository.forks}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetryClick: () -> Unit,
    onFiltersClick: () -> Unit,
    hasActiveFilters: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )
        Row(
            modifier = Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = onRetryClick) {
                Text(text = "Повторить")
            }
            Button(onClick = onFiltersClick) {
                Text(text = if (hasActiveFilters) "Фильтры •" else "Фильтры")
            }
        }
    }
}
