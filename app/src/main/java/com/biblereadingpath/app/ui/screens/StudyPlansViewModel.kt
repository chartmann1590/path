package com.biblereadingpath.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblereadingpath.app.data.studyplan.PlanDefinition
import com.biblereadingpath.app.data.studyplan.StudyPlan
import com.biblereadingpath.app.data.studyplan.TopicPlans
import com.biblereadingpath.app.data.studyplan.DevotionalPlans
import com.biblereadingpath.app.data.repository.StudyPlanRepository
import com.biblereadingpath.app.data.repository.PathRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudyPlansViewModel(
    private val studyPlanRepository: StudyPlanRepository,
    private val pathRepository: PathRepository
) : ViewModel() {

    val topicPlans = TopicPlans.ALL

    val devotionalPlans = DevotionalPlans.ALL

    val activePlanType: StateFlow<String> = studyPlanRepository.getActivePlanFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "sequential")

    val activePlanId: StateFlow<String?> = studyPlanRepository.userPreferences.studyPlanId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activePlanName: StateFlow<String> = combine(activePlanType, activePlanId) { type, id ->
        val plan = StudyPlan.deserialize(type, id)
        studyPlanRepository.getPlanDisplayName(plan)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Sequential")

    val progressMap: StateFlow<Set<String>> = pathRepository.getAllProgress()
        .map { list ->
            list.filter { it.isCompleted }.map { it.chapterId }.toSet()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun selectPlan(plan: PlanDefinition) {
        viewModelScope.launch {
            val studyPlan = when (plan.typeKey) {
                "topic" -> StudyPlan.TopicBased(plan.id)
                "devotional" -> StudyPlan.Devotional(plan.id)
                else -> StudyPlan.Sequential
            }
            studyPlanRepository.setActivePlan(studyPlan)
        }
    }

    fun selectSequential() {
        viewModelScope.launch {
            studyPlanRepository.setActivePlan(StudyPlan.Sequential)
        }
    }
}
