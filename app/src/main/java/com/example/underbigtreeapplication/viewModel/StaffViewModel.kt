package com.example.underbigtreeapplication.viewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.underbigtreeapplication.model.AddOnEntity
import com.example.underbigtreeapplication.model.DrinkEntity
import com.example.underbigtreeapplication.model.MenuEntity
import com.example.underbigtreeapplication.model.OptionItem
import com.example.underbigtreeapplication.model.SauceEntity
import com.example.underbigtreeapplication.repository.MenuRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class StaffViewModel(private val repository: MenuRepository) : ViewModel() {
    val menus = repository.menus.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val sauces = repository.sauces.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val addons = repository.addons.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val db = Firebase.firestore

    private val menuCollection = db.collection("Menu")
    private val addonCollection = db.collection("AddOn")
    private val sauceCollection = db.collection("Sauce")

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

    suspend fun getMenuById(id: String): MenuEntity? {
        return menuCollection.document(id).get().await().toObject(MenuEntity::class.java)
    }

    suspend fun getAddOnById(id: String): AddOnEntity? {
        return try {
            val snapshot = addonCollection.document(id).get().await()
            snapshot.toObject(AddOnEntity::class.java)
        } catch (e: Exception) {
            Log.e("StaffVM", "Error fetching AddOn by id: $id", e)
            null
        }
    }

    suspend fun getSauceById(id: String): SauceEntity? {
        return try {
            val snapshot = sauceCollection.document(id).get().await()
            snapshot.toObject(SauceEntity::class.java)
        } catch (e: Exception) {
            Log.e("StaffVM", "Error fetching Sauce by id: $id", e)
            null
        }
    }

    fun updateMenu(menu: MenuEntity) {
        menuCollection.document(menu.id)
            .set(menu)
            .addOnSuccessListener { Log.d("StaffVM", "Menu updated successfully") }
            .addOnFailureListener { e -> Log.e("StaffVM", "Error updating menu", e) }
    }

    fun updateAddOn(addon: AddOnEntity) {
        addonCollection.document(addon.id)
            .set(addon)
            .addOnSuccessListener { Log.d("StaffVM", "AddOn updated successfully") }
            .addOnFailureListener { e -> Log.e("StaffVM", "Error updating addon", e) }
    }

    fun updateSauce(sauce: SauceEntity) {
        sauceCollection.document(sauce.id)
            .set(sauce)
            .addOnSuccessListener { Log.d("StaffVM", "Sauce updated successfully") }
            .addOnFailureListener { e -> Log.e("StaffVM", "Error updating sauce", e) }
    }

    fun addMenu(menu: MenuEntity) {
        viewModelScope.launch {
            repository.addMenu(menu)
        }
    }
    suspend fun getAllSauceNamesFromFirebase(): List<OptionItem> {
        return repository.getAllSauceNames()
    }
    suspend fun getLastSauceIdFromFirebase(): String? {
        return repository.getLastSauceId()
    }

    fun addSauce(sauce: SauceEntity) {
        viewModelScope.launch {
            repository.addSauce(sauce)
        }
    }

    fun updateSauceAvailability(sauceId: String, availability: Boolean) {
        viewModelScope.launch {
            repository.updateSauceAvailability(sauceId, availability)
        }
    }

    fun deleteSauce(sauceId: String) {
        viewModelScope.launch {
            repository.deleteSauce(sauceId)
        }
    }
    suspend fun getAllAddOnNamesFromFirebase(): List<OptionItem> {
        return repository.getAllAddOnNames()
    }

    suspend fun getLastAddOnIdFromFirebase(): String? {
        return repository.getLastAddOnId()
    }

    fun addAddOn(addon: AddOnEntity) {
        viewModelScope.launch {
            repository.addAddOn(addon)
        }
    }
    fun updateAddOnAvailability(addonId: String, available: Boolean) {
        viewModelScope.launch {
            repository.updateAddOnAvailability(addonId, available)
        }
    }
    fun deleteAddOn(addOnId: String) {
        viewModelScope.launch {
            repository.deleteAddOn(addOnId)
        }
    }
    suspend fun getLastDrinkIdFromFirebase(): String? {
        return repository.getLastDrinkId()
    }

    fun addDrink(drink: DrinkEntity) {
        viewModelScope.launch {
            repository.addDrink(drink)
        }
    }

    fun addFood(food: MenuEntity) {
        viewModelScope.launch {
            repository.addFood(food)
        }
    }

    fun deleteMenu(menuId: String) {
        viewModelScope.launch {
            repository.deleteMenu(menuId)
        }
    }

}
