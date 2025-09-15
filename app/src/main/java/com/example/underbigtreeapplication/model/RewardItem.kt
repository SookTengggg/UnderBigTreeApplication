package com.example.underbigtreeapplication.model

data class RewardItem(
    val id: String = "",
    val name: String = "",
    val condition: String = "Can only redeem one time",
    val pointsRequired: Int = 50,
    var isRedeemed: Boolean = false,
    var isPaid: Boolean = false
)

