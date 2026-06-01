package ru.urfu.githubrepositories.ui.repositories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.urfu.githubrepositories.domain.model.GitHubRepository
import ru.urfu.githubrepositories.presentation.repositories.RepositoryDetailsUiState

@Composable
fun RepositoryDetailsScreen(
    state: RepositoryDetailsUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        RepositoryDetailsUiState.Loading -> LoadingContent(modifier = modifier)
        is RepositoryDetailsUiState.Error -> DetailsErrorContent(
            message = state.message,
            onBackClick = onBackClick,
            onRetryClick = onRetryClick,
            modifier = modifier
        )

        is RepositoryDetailsUiState.Success -> RepositoryDetailsContent(
            repository = state.repository,
            isFavorite = state.isFavorite,
            onBackClick = onBackClick,
            onFavoriteClick = onFavoriteClick,
            modifier = modifier
        )
    }
}

@Composable
private fun RepositoryDetailsContent(
    repository: GitHubRepository,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onBackClick) {
                Text(text = "Назад")
            }
            Button(onClick = onFavoriteClick) {
                Text(
                    text = if (isFavorite) {
                        "Удалить из избранного"
                    } else {
                        "Добавить в избранное"
                    }
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = repository.name,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = repository.owner,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = repository.description,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow(label = "Язык", value = repository.language)
                DetailRow(label = "Лицензия", value = repository.license)
                DetailRow(label = "Основная ветка", value = repository.defaultBranch)
                DetailRow(label = "Обновлено", value = repository.updatedAt)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow(label = "Звезды", value = repository.stars.toString())
                DetailRow(label = "Форки", value = repository.forks.toString())
                DetailRow(label = "Открытые issues", value = repository.openIssues.toString())
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Topics",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = repository.topics.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium
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
private fun DetailsErrorContent(
    message: String,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
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
            Button(onClick = onBackClick) {
                Text(text = "Назад")
            }
            Button(onClick = onRetryClick) {
                Text(text = "Повторить")
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
