package com.rentagreement.pdfeditor.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "document_history")
data class DocumentHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uri: String,
    val name: String,
    val importTimestamp: Long,
    val exportTimestamp: Long? = null,
    val extractedJson: String
)
