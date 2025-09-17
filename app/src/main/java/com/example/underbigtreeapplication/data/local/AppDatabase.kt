package com.example.underbigtreeapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.underbigtreeapplication.model.AddOnEntity
import com.example.underbigtreeapplication.model.CategoryEntity
import com.example.underbigtreeapplication.model.DrinkEntity
import com.example.underbigtreeapplication.model.MenuEntity
import com.example.underbigtreeapplication.model.ProfileEntity
import com.example.underbigtreeapplication.model.SauceEntity

@Database(
    entities = [MenuEntity::class, CategoryEntity::class, ProfileEntity::class, SauceEntity::class, AddOnEntity::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun menuDao(): MenuDao
    abstract fun categoryDao(): CategoryDao
    abstract fun profileDao(): ProfileDao
    abstract fun sauceDao(): SauceDao
    abstract fun AddOnDao(): AddOnDao
    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}