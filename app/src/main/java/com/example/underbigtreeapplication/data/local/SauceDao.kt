package com.example.underbigtreeapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.OnConflictStrategy
import com.example.underbigtreeapplication.model.SauceEntity

@Dao
interface SauceDao {

    @Query("SELECT * FROM sauce")
    fun getAllSauces(): Flow<List<SauceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSauce(sauce: SauceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSauces(sauces: List<SauceEntity>)

    @Query("UPDATE sauce SET availability = :available WHERE id = :sauceId")
    suspend fun updateAvailability(sauceId: String, available: Boolean)

    @Query("DELETE FROM sauce WHERE id = :sauceId")
    suspend fun deleteSauce(sauceId: String)
}
