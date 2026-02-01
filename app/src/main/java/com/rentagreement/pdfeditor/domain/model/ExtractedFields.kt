package com.rentagreement.pdfeditor.domain.model

data class ExtractedFields(
    val ownerName: String = "",
    val tenantName: String = "",
    val propertyAddress: String = "",
    val rentAmount: String = "",
    val depositAmount: String = "",
    val agreementStartDate: String = "",
    val durationMonths: String = "",
    val noticePeriod: String = "",
    val maintenanceAmount: String = ""
)
