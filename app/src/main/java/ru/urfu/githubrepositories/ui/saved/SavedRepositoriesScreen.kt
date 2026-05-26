package ru.urfu.githubrepositories.ui.saved

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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.urfu.githubrepositories.domain.model.GitHubRepository
import ru.urfu.githubrepositories.presentation.repositories.SavedRepositoriesUiState

@Composable
fun SavedRepositoriesScreen(
    state: SavedRepositoriesUiState,
    modifier: Modifier = Modifier
) {
    when (state) {
        SavedRepositoriesUiState.Loading -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        is SavedRepositoriesUiState.Success -> SavedRepositoriesList(
            repositories = state.repositories,
            modifier = modifier
        )
    }
}

@Composable
private fun SavedRepositoriesList(
    repositories: List<GitHubRepository>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Сохраненные",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        if (repositories.isEmpty()) {
            item {
                Text(
                    text = "Нет сохраненных репозиториев",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        items(
            items = repositories,
            key = { repository -> repository.id }
        ) { repository ->
            SavedRepositoryCard(repository = repository)
        }
    }
}

@Composable
private fun SavedRepositoryCard(
    repository: GitHubRepository,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
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
