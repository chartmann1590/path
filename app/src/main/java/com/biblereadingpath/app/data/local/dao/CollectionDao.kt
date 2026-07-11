package com.biblereadingpath.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.biblereadingpath.app.data.local.entity.CollectionEntity
import com.biblereadingpath.app.data.local.entity.CollectionMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collections ORDER BY createdAt DESC")
    fun getAllCollections(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collections WHERE id = :id")
    suspend fun getCollection(id: Long): CollectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity): Long

    @Delete
    suspend fun deleteCollection(collection: CollectionEntity)

    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun deleteCollectionById(id: Long)

    @Query("SELECT * FROM collection_members WHERE collectionId = :collectionId")
    fun getMembersForCollection(collectionId: Long): Flow<List<CollectionMemberEntity>>

    @Query("SELECT * FROM collection_members WHERE verseId = :verseId")
    fun getCollectionsForVerse(verseId: String): Flow<List<CollectionMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addVerseToCollection(member: CollectionMemberEntity)

    @Query("DELETE FROM collection_members WHERE collectionId = :collectionId AND verseId = :verseId")
    suspend fun removeVerseFromCollection(collectionId: Long, verseId: String)
}
