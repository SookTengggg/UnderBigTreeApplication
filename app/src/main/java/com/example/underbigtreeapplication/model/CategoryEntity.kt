package com.example.underbigtreeapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category")
data class CategoryEntity (
    @PrimaryKey val id: String,
    val name: String,
    val imageRes: String,
)