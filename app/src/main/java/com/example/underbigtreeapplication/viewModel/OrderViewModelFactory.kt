package com.example.underbigtreeapplication.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class OrderViewModelFactory(
    private val foodId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrderViewModel(foodId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}