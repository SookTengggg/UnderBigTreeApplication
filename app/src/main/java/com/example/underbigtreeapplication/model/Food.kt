package com.example.underbigtreeapplication.model

data class Food(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val imageRes: String = "",
    val desc: String = "",
    val availability: Boolean = true,
    val category: List<String> = emptyList(),

    val addOn: List<String> = emptyList(),
    val sauce: List<String> = emptyList(),

    // Resolved from IDs
    val addOnIds: List<Option> = emptyList(),
    val sauceIds: List<Option> = emptyList()
)