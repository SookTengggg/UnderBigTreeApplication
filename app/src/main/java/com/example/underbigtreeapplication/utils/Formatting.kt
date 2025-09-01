package com.example.underbigtreeapplication.utils

fun maskPhoneNumber(phone: String): String {
    val prefix = phone.substring(0, 3)
    val last4 = phone.takeLast(4)
    val masked = "*".repeat(phone.length - prefix.length - last4.length)
    return prefix + masked + last4
}

fun formatAmount(amount: Double): String {
    return String.format("RM %.2f", amount)
}