package ru.urfu.githubrepositories

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ru.urfu.githubrepositories.ui.GithubRepositoriesApp
import ru.urfu.githubrepositories.ui.theme.GithubRepositoriesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GithubRepositoriesTheme {
                GithubRepositoriesApp()
            }
        }
    }
}
