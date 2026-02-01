package com.rentagreement.pdfeditor.domain.model

data class DocumentHistory(
    val id: Long = 0,
    val uri: String,
    val name: String,
    val importTimestamp: Long,
    val exportTimestamp: Long? = null,
    val extractedFields: ExtractedFields
)
