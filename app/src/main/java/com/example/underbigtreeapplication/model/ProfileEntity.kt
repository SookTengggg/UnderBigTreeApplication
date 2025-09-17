package com.example.underbigtreeapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Profiles")
data class ProfileEntity(
    @PrimaryKey val uid: String,
    val customerId: String?,
    val name: String,
    val phone: String,
    val gender: String,
    val email: String,
    val photoUrl: String?,
    val role: String,
    val points: Int = 0
)