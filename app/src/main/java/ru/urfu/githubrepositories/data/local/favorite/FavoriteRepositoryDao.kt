package ru.urfu.githubrepositories.data.local.favorite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteRepositoryDao {
    @Query("SELECT * FROM favorite_repositories ORDER BY stars DESC")
    fun observeFavorites(): Flow<List<FavoriteRepositoryEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_repositories WHERE id = :id)")
    fun observeIsFavorite(id: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(repository: FavoriteRepositoryEntity)

    @Query("DELETE FROM favorite_repositories WHERE id = :id")
    suspend fun deleteFavoriteById(id: Long)
}
