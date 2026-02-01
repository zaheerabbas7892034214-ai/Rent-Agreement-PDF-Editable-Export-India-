package com.rentagreement.pdfeditor.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entitlement")
data class EntitlementEntity(
    @PrimaryKey
    val id: Int = 1,
    val isPro: Boolean = false,
    val freeExportsRemaining: Int = 1,
    val lastChecked: Long = System.currentTimeMillis()
)
