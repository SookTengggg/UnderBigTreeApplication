package com.example.underbigtreeapplication.model

data class Payment(
    val paymentId: String = "",
    val orderIds: List<String> = emptyList(),
    val totalPrice: Double = 0.0,
    val paymentMethod: String = "",
    val transactionDate: String = "",
    val phone: String = "",
    val userId: String = ""
)

