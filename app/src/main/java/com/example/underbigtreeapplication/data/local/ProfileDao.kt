package com.example.underbigtreeapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.underbigtreeapplication.model.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM Profiles WHERE uid = :uid LIMIT 1")
    fun observe(uid: String): Flow<ProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ProfileEntity)

    @Query("SELECT COUNT(*) FROM Profiles WHERE role = :role")
    suspend fun countByRole(role: String): Int

    @Query("SELECT * FROM Profiles WHERE email = :email LIMIT 1")
    fun getByEmail(email: String): Flow<ProfileEntity?>

    @Query("UPDATE Profiles SET photoUrl = :photoUrl WHERE uid = :id")
    suspend fun updatePhotoUrl(id: String, photoUrl: String)
}