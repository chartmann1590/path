package com.path.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.path.app.data.local.dao.BibleDao
import com.path.app.data.local.dao.FavoriteDao
import com.path.app.data.local.dao.NoteDao
import com.path.app.data.local.dao.ProgressDao
import com.path.app.data.local.entity.BibleVerseEntity
import com.path.app.data.local.entity.FavoriteEntity
import com.path.app.data.local.entity.NoteEntity
import com.path.app.data.local.entity.ProgressEntity

@Database(
    entities = [NoteEntity::class, ProgressEntity::class, BibleVerseEntity::class, FavoriteEntity::class],
    version = 3,
    exportSchema = false
)
abstract class PathDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun progressDao(): ProgressDao
    abstract fun bibleDao(): BibleDao
    abstract fun favoriteDao(): FavoriteDao
}
