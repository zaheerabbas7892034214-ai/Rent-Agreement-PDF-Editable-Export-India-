package com.rentagreement.pdfeditor.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.rentagreement.pdfeditor.data.database.AppDatabase
import com.rentagreement.pdfeditor.data.database.EntitlementEntity
import com.rentagreement.pdfeditor.domain.model.Entitlement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EntitlementRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val entitlementDao = database.entitlementDao()
    private val prefs: SharedPreferences = context.getSharedPreferences("entitlement_prefs", Context.MODE_PRIVATE)
    
    val entitlement: Flow<Entitlement> = entitlementDao.getEntitlement().map { entity ->
        entity?.toDomain() ?: getDefaultEntitlement()
    }
    
    suspend fun getEntitlementSync(): Entitlement {
        val entity = entitlementDao.getEntitlementSync()
        return entity?.toDomain() ?: getDefaultEntitlement()
    }
    
    suspend fun updateProStatus(isPro: Boolean) {
        val current = entitlementDao.getEntitlementSync() ?: EntitlementEntity()
        entitlementDao.insertEntitlement(
            current.copy(
                isPro = isPro,
                lastChecked = System.currentTimeMillis()
            )
        )
        prefs.edit().putBoolean("is_pro", isPro).apply()
    }
    
    suspend fun decrementFreeExports() {
        val current = entitlementDao.getEntitlementSync() ?: EntitlementEntity()
        if (current.freeExportsRemaining > 0) {
            entitlementDao.insertEntitlement(
                current.copy(freeExportsRemaining = current.freeExportsRemaining - 1)
            )
            prefs.edit().putInt("free_exports", current.freeExportsRemaining - 1).apply()
        }
    }
    
    suspend fun initializeEntitlement() {
        if (entitlementDao.getEntitlementSync() == null) {
            val isPro = prefs.getBoolean("is_pro", false)
            val freeExports = prefs.getInt("free_exports", 1)
            entitlementDao.insertEntitlement(
                EntitlementEntity(
                    isPro = isPro,
                    freeExportsRemaining = freeExports
                )
            )
        }
    }
    
    private fun getDefaultEntitlement() = Entitlement(
        isPro = prefs.getBoolean("is_pro", false),
        freeExportsRemaining = prefs.getInt("free_exports", 1)
    )
    
    private fun EntitlementEntity.toDomain() = Entitlement(
        isPro = isPro,
        freeExportsRemaining = freeExportsRemaining,
        lastChecked = lastChecked
    )
}
