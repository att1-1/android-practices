package ru.urfu.githubrepositories.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.urfu.githubrepositories.data.local.favorite.FavoriteRepositoryDao
import ru.urfu.githubrepositories.data.local.favorite.FavoriteRepositoryEntity

@Database(
    entities = [FavoriteRepositoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteRepositoryDao(): FavoriteRepositoryDao
}
