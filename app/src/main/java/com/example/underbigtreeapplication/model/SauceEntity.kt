package com.example.underbigtreeapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.underbigtreeapplication.data.local.Converters

@Entity(tableName = "sauce")
@TypeConverters(Converters::class)
data class SauceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double,
    val availability: Boolean = true,
)