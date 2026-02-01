package com.rentagreement.pdfeditor.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EntitlementDao {
    @Query("SELECT * FROM entitlement WHERE id = 1")
    fun getEntitlement(): Flow<EntitlementEntity?>
    
    @Query("SELECT * FROM entitlement WHERE id = 1")
    suspend fun getEntitlementSync(): EntitlementEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntitlement(entitlement: EntitlementEntity)
    
    @Update
    suspend fun updateEntitlement(entitlement: EntitlementEntity)
}
