package com.rentagreement.pdfeditor.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rentagreement.pdfeditor.data.repository.DocumentRepository
import com.rentagreement.pdfeditor.domain.model.DocumentHistory
import com.rentagreement.pdfeditor.domain.model.ExtractedFields
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExtractedFieldsViewModel(application: Application) : AndroidViewModel(application) {
    private val documentRepository = DocumentRepository(application)
    
    private val _document = MutableStateFlow<DocumentHistory?>(null)
    val document: StateFlow<DocumentHistory?> = _document
    
    private val _fields = MutableStateFlow(ExtractedFields())
    val fields: StateFlow<ExtractedFields> = _fields
    
    fun loadDocument(documentId: Long) {
        viewModelScope.launch {
            val doc = documentRepository.getDocumentById(documentId)
            _document.value = doc
            _fields.value = doc?.extractedFields ?: ExtractedFields()
        }
    }
    
    fun updateFields(fields: ExtractedFields) {
        _fields.value = fields
    }
    
    fun saveAndGeneratePdf(onSuccess: (Long) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val doc = _document.value
                if (doc != null) {
                    val updated = doc.copy(extractedFields = _fields.value)
                    documentRepository.updateDocument(updated)
                    onSuccess(doc.id)
                } else {
                    onError("Document not found")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            }
        }
    }
}
