package com.biblereadingpath.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.biblereadingpath.app.data.local.dao.BibleDao
import com.biblereadingpath.app.data.local.dao.FavoriteDao
import com.biblereadingpath.app.data.local.dao.HighlightDao
import com.biblereadingpath.app.data.local.dao.CollectionDao
import com.biblereadingpath.app.data.local.dao.NoteDao
import com.biblereadingpath.app.data.local.dao.ProgressDao
import com.biblereadingpath.app.data.local.dao.AchievementDao
import com.biblereadingpath.app.data.local.dao.QuizDao
import com.biblereadingpath.app.data.local.entity.BibleVerseEntity
import com.biblereadingpath.app.data.local.entity.FavoriteEntity
import com.biblereadingpath.app.data.local.entity.HighlightEntity
import com.biblereadingpath.app.data.local.entity.CollectionEntity
import com.biblereadingpath.app.data.local.entity.CollectionMemberEntity
import com.biblereadingpath.app.data.local.entity.NoteEntity
import com.biblereadingpath.app.data.local.entity.ProgressEntity
import com.biblereadingpath.app.data.local.entity.AchievementEntity
import com.biblereadingpath.app.data.local.entity.QuizEntity

@Database(
    entities = [NoteEntity::class, ProgressEntity::class, BibleVerseEntity::class, FavoriteEntity::class, QuizEntity::class, AchievementEntity::class, HighlightEntity::class, CollectionEntity::class, CollectionMemberEntity::class],
    version = 6,
    exportSchema = false
)
abstract class PathDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun progressDao(): ProgressDao
    abstract fun bibleDao(): BibleDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun quizDao(): QuizDao
    abstract fun achievementDao(): AchievementDao
    abstract fun highlightDao(): HighlightDao
    abstract fun collectionDao(): CollectionDao
}
