package com.biblereadingpath.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.biblereadingpath.app.data.local.dao.BibleDao
import com.biblereadingpath.app.data.local.dao.FavoriteDao
import com.biblereadingpath.app.data.local.dao.NoteDao
import com.biblereadingpath.app.data.local.dao.ProgressDao
import com.biblereadingpath.app.data.local.dao.QuizDao
import com.biblereadingpath.app.data.local.entity.BibleVerseEntity
import com.biblereadingpath.app.data.local.entity.FavoriteEntity
import com.biblereadingpath.app.data.local.entity.NoteEntity
import com.biblereadingpath.app.data.local.entity.ProgressEntity
import com.biblereadingpath.app.data.local.entity.QuizEntity

@Database(
    entities = [NoteEntity::class, ProgressEntity::class, BibleVerseEntity::class, FavoriteEntity::class, QuizEntity::class],
    version = 4,
    exportSchema = false
)
abstract class PathDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun progressDao(): ProgressDao
    abstract fun bibleDao(): BibleDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun quizDao(): QuizDao
}
