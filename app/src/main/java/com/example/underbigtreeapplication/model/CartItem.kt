package com.example.underbigtreeapplication.model

data class CartItem(
    val orderId: String = "",
    val food: Food = Food(),
    val selectedSauces: List<Option> = emptyList(),
    val selectedAddOns: List<Option> = emptyList(),
    val quantity: Int = 1,
    val takeAway: Boolean = false,
    val remarks: String = "",
    val totalPrice: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = "",
    val status: String = "pending",
    val paymentId: String? = null
)

