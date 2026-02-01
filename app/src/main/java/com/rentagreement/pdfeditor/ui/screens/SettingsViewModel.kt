package com.rentagreement.pdfeditor.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rentagreement.pdfeditor.data.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val documentRepository = DocumentRepository(application)
    
    private val _clearHistoryState = MutableStateFlow<ClearHistoryState>(ClearHistoryState.Idle)
    val clearHistoryState: StateFlow<ClearHistoryState> = _clearHistoryState
    
    sealed class ClearHistoryState {
        object Idle : ClearHistoryState()
        object Success : ClearHistoryState()
        data class Error(val message: String) : ClearHistoryState()
    }
    
    fun clearHistory() {
        viewModelScope.launch {
            try {
                documentRepository.clearHistory()
                _clearHistoryState.value = ClearHistoryState.Success
            } catch (e: Exception) {
                _clearHistoryState.value = ClearHistoryState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun resetState() {
        _clearHistoryState.value = ClearHistoryState.Idle
    }
}
