package com.example.underbigtreeapplication.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.underbigtreeapplication.data.local.AppDatabase
import com.example.underbigtreeapplication.model.AddOnEntity
import com.example.underbigtreeapplication.model.CategoryEntity
import com.example.underbigtreeapplication.model.DrinkEntity
import com.example.underbigtreeapplication.model.MenuEntity
import com.example.underbigtreeapplication.model.OptionItem
import com.example.underbigtreeapplication.model.SauceEntity
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

class MenuRepository (private val db: AppDatabase) {
    private val firestore = FirebaseFirestore.getInstance()
    val firebaseStorage = Firebase.storage
    //room flows
    val menus: Flow<List<MenuEntity>> = db.menuDao().getAllMenus()
    val sauces: Flow<List<SauceEntity>> = db.sauceDao().getAllSauces()
    val addons: Flow<List<AddOnEntity>> = db.AddOnDao().getAllAddOn()

    val categories: Flow<List<CategoryEntity>> = db.categoryDao().getAllCategories()
    //refresh from firebase
    suspend fun refreshFromFirebase() = withContext(Dispatchers.IO){
        try{
            val menuSnapshot = firestore.collection("Menu").get().await()
            val menuList = menuSnapshot.map { doc ->
                MenuEntity(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    price = doc.getDouble("price") ?: 0.0,
                    imageRes = doc.getString("imageRes") ?: "",
                    category = doc.get("category") as? List<String> ?: emptyList(),
                    desc = doc.getString("desc") ?: "",
                    availability = doc.getBoolean("availability") ?: false,
                    addOn = doc.get("addOn") as? List<String> ?: emptyList(),
                    sauce = doc.get("sauce") as? List<String> ?: emptyList()
                )
            }

            db.menuDao().insertMenus(menuList)

            val categorySnapshot = firestore.collection("Category").get().await()
            val categoryList = categorySnapshot.map { doc ->
                CategoryEntity(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    imageRes = doc.getString("imageRes") ?: ""
                )
            }

            db.categoryDao().insertCategories(categoryList)

            val sauceSnapshot = firestore.collection("Sauce").get().await()
            val sauceList = sauceSnapshot.map { doc ->
                SauceEntity(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    price = doc.getDouble("price") ?: 0.0,
                    availability = doc.getBoolean("availability") ?: false
                )
            }
            db.sauceDao().insertSauces(sauceList)

            val addonSnapshot = firestore.collection("AddOn").get().await()
            val addonList = addonSnapshot.map { doc ->
                AddOnEntity(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    price = doc.getDouble("price") ?: 0.0,
                    availability = doc.getBoolean("availability") ?: false
                )
            }
            db.AddOnDao().insertAddOn(addonList)

        } catch (e: Exception) {
            Log.e("MenuRepository", "Error fetching data from firebase", e)
        }
    }
    suspend fun updateMenuAvailability(menuId: String, available: Boolean) = withContext(Dispatchers.IO) {
        try {
            firestore.collection("Menu")
                .document(menuId)
                .update("availability", available)
                .await()

            db.menuDao().updateAvailability(menuId, available)

        } catch (e: Exception) {
            Log.e("MenuRepository", "Error updating availability", e)
        }
    }

    suspend fun addMenu(menu: MenuEntity) = withContext(Dispatchers.IO) {
        try {
            firestore.collection("Menu")
                .document(menu.id)
                .set(menu)
                .await()

            db.menuDao().insertMenu(menu)

        } catch (e: Exception) {
            Log.e("MenuRepository", "Error adding menu", e)
        }
    }

    suspend fun getAllSauceNames(): List<OptionItem> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("Sauce").get().await()
            snapshot.documents.mapNotNull { doc ->
                val id = doc.id
                val name = doc.getString("name")
                if (name != null) OptionItem(id, name) else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getLastSauceId(): String? = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("Sauce").get().await()
            val maxNumber = snapshot.mapNotNull { doc ->
                doc.id.removePrefix("SM").toIntOrNull()
            }.maxOrNull() ?: 0
            "SM%04d".format(maxNumber)
        } catch (e: Exception) {
            Log.e("MenuRepository", "Error fetching last sauce id", e)
            null
        }
    }

    suspend fun addSauce(sauce: SauceEntity) = withContext(Dispatchers.IO) {
        try {
            firestore.collection("Sauce")
                .document(sauce.id)
                .set(sauce.copy())
                .await()

            db.sauceDao().insertSauce(sauce)

        } catch (e: Exception) {
            Log.e("MenuRepository", "Error adding sauce", e)
        }
    }
    suspend fun updateSauceAvailability(sauceId: String, availability: Boolean) = withContext(Dispatchers.IO) {
        try {
            firestore.collection("Sauce")
                .document(sauceId)
                .update("availability", availability)
                .await()

            db.sauceDao().updateAvailability(sauceId, availability)

        } catch (e: Exception) {
            Log.e("MenuRepository", "Error updating availability", e)
        }
    }
    suspend fun getAllAddOnNames(): List<OptionItem> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("AddOn").get().await()
            snapshot.documents.mapNotNull { doc ->
                val id = doc.id
                val name = doc.getString("name")
                if (name != null) OptionItem(id, name) else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    suspend fun getLastAddOnId(): String? = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("AddOn").get().await()
            val maxNumber = snapshot.mapNotNull { doc ->
                doc.id.removePrefix("AM").toIntOrNull()
            }.maxOrNull() ?: 0
            "AM%04d".format(maxNumber)
        } catch (e: Exception) {
            Log.e("MenuRepository", "Error fetching last sauce id", e)
            null
        }
    }
    suspend fun addAddOn(addon: AddOnEntity) = withContext(Dispatchers.IO) {
        try {
            firestore.collection("AddOn")
                .document(addon.id)
                .set(addon.copy())
                .await()

            db.AddOnDao().insertAddOn(addon)

        } catch (e: Exception) {
            Log.e("MenuRepository", "Error adding add-on", e)
        }
    }

    suspend fun updateAddOnAvailability(addonId: String, available: Boolean) = withContext(Dispatchers.IO) {
        try {
            firestore.collection("AddOn")
                .document(addonId)
                .update("availability", available)
                .await()

            db.AddOnDao().updateAvailability(addonId, available)
        } catch (e: Exception) {
            Log.e("MenuRepository", "Error updating add-on availability", e)
        }
    }

    suspend fun getLastDrinkId(): String? = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("Menu").get().await()
            val maxNumber = snapshot.mapNotNull { doc ->
                doc.id.removePrefix("M").toIntOrNull()
            }.maxOrNull() ?: 0
            "M%04d".format(maxNumber)
        } catch (e: Exception) {
            Log.e("MenuRepository", "Error fetching last drink id", e)
            null
        }
    }

    suspend fun uploadDrinkImageFromUri(imageUri: Uri, id: String): String? {
        val storageReference: StorageReference = FirebaseStorage.getInstance().getReference("$id.jpg")
        return try {
            // Upload the file
            val uploadTask = storageReference.putFile(imageUri)
            // Wait for the upload to complete
            uploadTask.await() // Make sure to use Kotlin Coroutines or a similar approach to wait for the task to finish
            // Get the download URL
            storageReference.downloadUrl.await().toString()
        } catch (e: Exception) {
            null  // Handle the error appropriately
        }
    }

    suspend fun addDrink(drink: DrinkEntity) = withContext(Dispatchers.IO) {
        try {
            firestore.collection("Menu")
                .document(drink.id)
                .set(drink.copy())
                .await()

        } catch (e: Exception) {
            Log.e("MenuRepository", "Error adding drink", e)
        }
    }

    suspend fun addFood(food: MenuEntity) = withContext(Dispatchers.IO) {
        try {
            firestore.collection("Menu")
                .document(food.id)
                .set(food.copy())
                .await()

        } catch (e: Exception) {
            Log.e("MenuRepository", "Error adding food", e)
        }
    }
    suspend fun deleteMenu(menuId: String) = withContext(Dispatchers.IO) {
        try {
            firestore.collection("Menu")
                .document(menuId)
                .delete()
                .await()

            db.menuDao().deleteMenu(menuId)

        } catch (e: Exception) {
            Log.e("MenuRepository", "Error deleting menu", e)
        }
    }

}