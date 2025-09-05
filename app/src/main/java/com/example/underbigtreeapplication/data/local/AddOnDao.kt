package com.example.underbigtreeapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.OnConflictStrategy
import com.example.underbigtreeapplication.model.AddOnEntity

@Dao
interface AddOnDao {

    @Query("SELECT * FROM addon")
    fun getAllAddOn(): Flow<List<AddOnEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddOn(addon: AddOnEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddOn(addon: List<AddOnEntity>)

    @Query("UPDATE addon SET availability = :available WHERE id = :addonId")
    suspend fun updateAvailability(addonId: String, available: Boolean)

    @Query("DELETE FROM addon WHERE id = :addonId")
    suspend fun deleteSauce(addonId: String)
}
