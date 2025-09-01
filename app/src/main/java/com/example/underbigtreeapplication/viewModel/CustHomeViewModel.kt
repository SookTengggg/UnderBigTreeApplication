package com.example.underbigtreeapplication.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.underbigtreeapplication.repository.MenuRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustHomeViewModel (private val repository: MenuRepository) : ViewModel() {
    val menus = repository.menus.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val categories = repository.categories.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedCategory = mutableStateOf("All")
    val selectedCategory: State<String> = _selectedCategory

    fun selectCategory(category: String){
        _selectedCategory.value = category
    }

    init {
        viewModelScope.launch {
            repository.refreshFromFirebase()
        }
    }

//    fun loadData(){
//        viewModelScope.launch{
//            repository.refreshFromFirebase()
//        }
//    }
}