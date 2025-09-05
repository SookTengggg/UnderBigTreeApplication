package com.example.underbigtreeapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.underbigtreeapplication.data.local.Converters

@Entity(tableName = "drink")
@TypeConverters(Converters::class)
data class DrinkEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double,
    val imageRes: String,
    val category: List<String> = emptyList(),
    val desc: String = "",
    val availability: Boolean = true,
    val addOn: List<String> = emptyList(),
    val sauce: List<String> = emptyList()
)
