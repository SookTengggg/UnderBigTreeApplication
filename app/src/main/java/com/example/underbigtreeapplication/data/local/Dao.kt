package com.example.underbigtreeapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val email: String?,
    val phone: String = "",
    val pointsBalance: Long = 0L
)
