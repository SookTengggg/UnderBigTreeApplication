package com.example.underbigtreeapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Profiles")
class ProfileEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val phone: String,
    val gender: String,
    val email: String,
    val photoUrl: String?,
    val role: String
)