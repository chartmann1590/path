package com.path.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.path.app.data.repository.PathRepository
import com.path.app.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map

class NotesViewModel(
    private val pathRepository: PathRepository
) : ViewModel() {
    val notes: StateFlow<List<NoteEntity>> = pathRepository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
