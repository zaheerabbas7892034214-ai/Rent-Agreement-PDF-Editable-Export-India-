package com.rentagreement.pdfeditor.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rentagreement.pdfeditor.data.repository.DocumentRepository
import com.rentagreement.pdfeditor.data.repository.EntitlementRepository
import com.rentagreement.pdfeditor.domain.model.DocumentHistory
import com.rentagreement.pdfeditor.domain.model.Entitlement
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val documentRepository = DocumentRepository(application)
    private val entitlementRepository = EntitlementRepository(application)
    
    val documents: StateFlow<List<DocumentHistory>> = documentRepository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val entitlement: StateFlow<Entitlement> = entitlementRepository.entitlement
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Entitlement())
    
    init {
        viewModelScope.launch {
            entitlementRepository.initializeEntitlement()
        }
    }
}
