package com.biblereadingpath.app.data.local.dao

import androidx.room.*
import com.biblereadingpath.app.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY savedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE verseId = :verseId")
    suspend fun getFavorite(verseId: String): FavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE verseId = :verseId")
    suspend fun deleteFavoriteById(verseId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE verseId = :verseId)")
    suspend fun isFavorite(verseId: String): Boolean
}
