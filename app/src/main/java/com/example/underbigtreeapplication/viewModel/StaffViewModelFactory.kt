package com.example.underbigtreeapplication.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.underbigtreeapplication.repository.MenuRepository

class StaffViewModelFactory(private val repository: MenuRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StaffViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StaffViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
