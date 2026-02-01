package com.rentagreement.pdfeditor.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rentagreement.pdfeditor.data.repository.DocumentRepository
import com.rentagreement.pdfeditor.data.repository.EntitlementRepository
import com.rentagreement.pdfeditor.domain.model.DocumentHistory
import com.rentagreement.pdfeditor.domain.model.Entitlement
import com.rentagreement.pdfeditor.util.PdfProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class PreviewViewModel(application: Application) : AndroidViewModel(application) {
    private val documentRepository = DocumentRepository(application)
    private val entitlementRepository = EntitlementRepository(application)
    private val pdfProcessor = PdfProcessor(application)
    
    private val _document = MutableStateFlow<DocumentHistory?>(null)
    val document: StateFlow<DocumentHistory?> = _document
    
    private val _entitlement = MutableStateFlow(Entitlement())
    val entitlement: StateFlow<Entitlement> = _entitlement
    
    private val _pdfFile = MutableStateFlow<File?>(null)
    val pdfFile: StateFlow<File?> = _pdfFile
    
    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState
    
    sealed class ExportState {
        object Idle : ExportState()
        object Generating : ExportState()
        data class Success(val file: File) : ExportState()
        data class Error(val message: String) : ExportState()
    }
    
    fun loadDocument(documentId: Long) {
        viewModelScope.launch {
            _document.value = documentRepository.getDocumentById(documentId)
            _entitlement.value = entitlementRepository.getEntitlementSync()
        }
    }
    
    fun exportPdf() {
        viewModelScope.launch {
            try {
                _exportState.value = ExportState.Generating
                
                val doc = _document.value
                val ent = _entitlement.value
                
                if (doc == null) {
                    _exportState.value = ExportState.Error("Document not found")
                    return@launch
                }
                
                if (!ent.isPro && ent.freeExportsRemaining <= 0) {
                    _exportState.value = ExportState.Error("Free export limit reached")
                    return@launch
                }
                
                val outputFile = File(getApplication<Application>().cacheDir, "rent_agreement_${System.currentTimeMillis()}.txt")
                val success = pdfProcessor.generateSummaryPdf(doc.extractedFields, outputFile)
                
                if (success) {
                    if (!ent.isPro) {
                        entitlementRepository.decrementFreeExports()
                    }
                    
                    val updated = doc.copy(exportTimestamp = System.currentTimeMillis())
                    documentRepository.updateDocument(updated)
                    
                    _pdfFile.value = outputFile
                    _exportState.value = ExportState.Success(outputFile)
                } else {
                    _exportState.value = ExportState.Error("Failed to generate PDF")
                }
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }
}
