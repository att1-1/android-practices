package ru.urfu.githubrepositories.ui.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun FiltersScreen(
    queryText: String,
    language: String,
    minStars: String,
    onQueryTextChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onMinStarsChange: (String) -> Unit,
    onDoneClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(onClick = onBackClick) {
            Text(text = "Назад")
        }

        Text(
            text = "Фильтры",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = queryText,
            onValueChange = onQueryTextChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Query text") },
            singleLine = true
        )

        OutlinedTextField(
            value = language,
            onValueChange = onLanguageChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Language") },
            singleLine = true
        )

        OutlinedTextField(
            value = minStars,
            onValueChange = onMinStarsChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Min stars") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Button(
            onClick = onDoneClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Готово")
        }
    }
}
