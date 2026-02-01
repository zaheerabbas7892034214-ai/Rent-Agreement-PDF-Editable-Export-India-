package com.rentagreement.pdfeditor.data.repository

import android.content.Context
import com.google.gson.Gson
import com.rentagreement.pdfeditor.data.database.AppDatabase
import com.rentagreement.pdfeditor.data.database.DocumentHistoryEntity
import com.rentagreement.pdfeditor.domain.model.DocumentHistory
import com.rentagreement.pdfeditor.domain.model.ExtractedFields
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DocumentRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val documentDao = database.documentHistoryDao()
    private val gson = Gson()
    
    val allDocuments: Flow<List<DocumentHistory>> = documentDao.getAllDocuments().map { entities ->
        entities.map { it.toDomain() }
    }
    
    suspend fun insertDocument(document: DocumentHistory): Long {
        return documentDao.insertDocument(document.toEntity())
    }
    
    suspend fun updateDocument(document: DocumentHistory) {
        documentDao.updateDocument(document.toEntity())
    }
    
    suspend fun getDocumentById(id: Long): DocumentHistory? {
        return documentDao.getDocumentById(id)?.toDomain()
    }
    
    suspend fun clearHistory() {
        documentDao.clearAll()
    }
    
    private fun DocumentHistoryEntity.toDomain(): DocumentHistory {
        val fields = try {
            gson.fromJson(extractedJson, ExtractedFields::class.java)
        } catch (e: Exception) {
            ExtractedFields()
        }
        return DocumentHistory(
            id = id,
            uri = uri,
            name = name,
            importTimestamp = importTimestamp,
            exportTimestamp = exportTimestamp,
            extractedFields = fields
        )
    }
    
    private fun DocumentHistory.toEntity() = DocumentHistoryEntity(
        id = if (id == 0L) 0 else id,
        uri = uri,
        name = name,
        importTimestamp = importTimestamp,
        exportTimestamp = exportTimestamp,
        extractedJson = gson.toJson(extractedFields)
    )
}
