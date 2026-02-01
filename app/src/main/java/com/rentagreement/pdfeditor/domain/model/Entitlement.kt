package com.rentagreement.pdfeditor.domain.model

data class Entitlement(
    val isPro: Boolean = false,
    val freeExportsRemaining: Int = 1,
    val lastChecked: Long = System.currentTimeMillis()
)
