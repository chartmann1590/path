package com.biblereadingpath.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblereadingpath.app.data.local.entity.AchievementEntity
import com.biblereadingpath.app.data.repository.AchievementCategory
import com.biblereadingpath.app.data.repository.AchievementDefinition
import com.biblereadingpath.app.data.repository.Achievements
import com.biblereadingpath.app.data.repository.AchievementRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AchievementUiItem(
    val definition: AchievementDefinition,
    val isUnlocked: Boolean,
    val unlockedAt: Long?
)

class AchievementsViewModel(
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    val achievements: StateFlow<List<AchievementUiItem>> = achievementRepository.allAchievements
        .map { entities ->
            val unlockedMap = entities.associateBy { it.achievementId }
            Achievements.ALL.map { def ->
                val entity = unlockedMap[def.id]
                AchievementUiItem(
                    definition = def,
                    isUnlocked = entity != null,
                    unlockedAt = entity?.unlockedAt
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalUnlocked: StateFlow<Int> = achievements.map { list ->
        list.count { it.isUnlocked }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun getAchievementsByCategory(category: AchievementCategory): StateFlow<List<AchievementUiItem>> {
        return achievements.map { list ->
            list.filter { it.definition.category == category }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
}
