package com.rentagreement.pdfeditor.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentHistoryDao {
    @Query("SELECT * FROM document_history ORDER BY importTimestamp DESC")
    fun getAllDocuments(): Flow<List<DocumentHistoryEntity>>
    
    @Query("SELECT * FROM document_history WHERE id = :id")
    suspend fun getDocumentById(id: Long): DocumentHistoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentHistoryEntity): Long
    
    @Update
    suspend fun updateDocument(document: DocumentHistoryEntity)
    
    @Query("DELETE FROM document_history")
    suspend fun clearAll()
    
    @Delete
    suspend fun deleteDocument(document: DocumentHistoryEntity)
}
