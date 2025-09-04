package com.example.underbigtreeapplication.viewModel

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.underbigtreeapplication.model.AddOnEntity
import com.example.underbigtreeapplication.model.DrinkEntity
import com.example.underbigtreeapplication.model.MenuEntity
import com.example.underbigtreeapplication.model.SauceEntity
import com.example.underbigtreeapplication.repository.MenuRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class StaffViewModel(private val repository: MenuRepository) : ViewModel() {
    val menus = repository.menus.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            repository.refreshFromFirebase()
        }
    }

    fun updateMenuAvailability(menuId: String, available: Boolean) {
        viewModelScope.launch {
            repository.updateMenuAvailability(menuId, available)
        }
    }

    fun addMenu(menu: MenuEntity) {
        viewModelScope.launch {
            repository.addMenu(menu)
        }
    }
    suspend fun getLastSauceIdFromFirebase(): String? {
        return repository.getLastSauceId()
    }

    fun addSauce(sauce: SauceEntity) {
        viewModelScope.launch {
            repository.addSauce(sauce)
        }
    }

    suspend fun getLastAddOnIdFromFirebase(): String? {
        return repository.getLastAddOnId()
    }

    fun addAddOn(addon: AddOnEntity) {
        viewModelScope.launch {
            repository.addAddOn(addon)
        }
    }
    suspend fun getLastDrinkIdFromFirebase(): String? {
        return repository.getLastDrinkId()
    }
    suspend fun uploadDrinkImageToFirebase(uri: Uri, drinkId: String): String? {
        return repository.uploadDrinkImage(uri, drinkId)
    }

    fun addDrink(drink: DrinkEntity) {
        viewModelScope.launch {
            repository.addDrink(drink)
        }
    }
    fun deleteMenu(menuId: String) {
        viewModelScope.launch {
            repository.deleteMenu(menuId)
        }
    }

}
