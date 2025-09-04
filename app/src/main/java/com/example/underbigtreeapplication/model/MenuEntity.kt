package com.example.underbigtreeapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu")
data class MenuEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double,
    val imageRes: String,
    val category: List<String>,
    val desc: String,
    val availability: Boolean,
    val addOn: List<String>,
    val sauce: List<String>
)