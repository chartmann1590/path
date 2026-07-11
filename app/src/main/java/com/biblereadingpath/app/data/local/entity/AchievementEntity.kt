package com.biblereadingpath.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val achievementId: String,
    val unlockedAt: Long = System.currentTimeMillis(),
    val progress: Int = 0,
    val target: Int = 1
)
