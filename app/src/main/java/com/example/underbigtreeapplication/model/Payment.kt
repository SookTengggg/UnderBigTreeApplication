package com.example.underbigtreeapplication.model

data class Payment(
    val paymentId: String = "",
    val orderId: String = "",
    val totalPrice: Double = 0.0,
    val paymentMethod: String = "",
    val transactionDate: String = "",
    val phone: String = ""
)
