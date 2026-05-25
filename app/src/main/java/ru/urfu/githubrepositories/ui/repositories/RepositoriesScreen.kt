package ru.urfu.githubrepositories.ui.repositories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.urfu.githubrepositories.data.GitHubRepository

@Composable
fun RepositoriesScreen(
    repositories: List<GitHubRepository>,
    onRepositoryClick: (GitHubRepository) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Репозитории",
                style = MaterialTheme.typography.headlineMedium
            )
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
