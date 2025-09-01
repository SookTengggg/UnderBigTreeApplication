package com.example.underbigtreeapplication.repository

import android.util.Log
import com.example.underbigtreeapplication.data.local.AppDatabase
import com.example.underbigtreeapplication.model.CategoryEntity
import com.example.underbigtreeapplication.model.MenuEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

class MenuRepository (private val db: AppDatabase) {
    private val firestore = FirebaseFirestore.getInstance()
    //room flows
    val menus: Flow<List<MenuEntity>> = db.menuDao().getAllMenus()
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
        } catch (e: Exception) {
            Log.e("MenuRepository", "Error fetching data from firebase", e)
        }
    }
}