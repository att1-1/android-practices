package ru.urfu.githubrepositories

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.urfu.githubrepositories.di.appModule

class GithubRepositoriesApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@GithubRepositoriesApplication)
            modules(appModule)
        }
    }
}
