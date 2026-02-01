package com.rentagreement.pdfeditor.ui.screens

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rentagreement.pdfeditor.data.repository.DocumentRepository
import com.rentagreement.pdfeditor.domain.model.DocumentHistory
import com.rentagreement.pdfeditor.domain.model.ExtractedFields
import com.rentagreement.pdfeditor.util.PdfProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ImportViewModel(application: Application) : AndroidViewModel(application) {
    private val documentRepository = DocumentRepository(application)
    private val pdfProcessor = PdfProcessor(application)
    
    private val _state = MutableStateFlow<ImportState>(ImportState.Idle)
    val state: StateFlow<ImportState> = _state
    
    sealed class ImportState {
        object Idle : ImportState()
        data class Processing(val step: String) : ImportState()
        data class Success(val documentId: Long) : ImportState()
        data class Error(val message: String) : ImportState()
    }
    
    fun processDocument(uri: Uri, fileName: String) {
        viewModelScope.launch {
            try {
                _state.value = ImportState.Processing("Rendering PDF")
                
                _state.value = ImportState.Processing("OCR Parsing")
                
                _state.value = ImportState.Processing("Field Extraction")
                val fields = pdfProcessor.extractFieldsFromPdf(uri)
                
                val document = DocumentHistory(
                    uri = uri.toString(),
                    name = fileName,
                    importTimestamp = System.currentTimeMillis(),
                    extractedFields = fields
                )
                
                val documentId = documentRepository.insertDocument(document)
                _state.value = ImportState.Success(documentId)
            } catch (e: Exception) {
                _state.value = ImportState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun resetState() {
        _state.value = ImportState.Idle
    }
}
