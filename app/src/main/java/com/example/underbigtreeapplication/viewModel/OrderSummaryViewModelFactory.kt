package com.example.underbigtreeapplication.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class OrderSummaryViewModelFactory(private val cartViewModel: CartViewModel): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderSummaryViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return OrderSummaryViewModel(cartViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}