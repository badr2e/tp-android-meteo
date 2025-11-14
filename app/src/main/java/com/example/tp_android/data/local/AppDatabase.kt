package com.example.tp_android.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tp_android.data.local.dao.FavoriteCityDao
import com.example.tp_android.data.local.dao.WeatherDao
import com.example.tp_android.data.local.entity.FavoriteCityEntity
import com.example.tp_android.data.local.entity.WeatherEntity

/**
 * Base de données Room pour l'application météo
 */
@Database(
    entities = [
        WeatherEntity::class,
        FavoriteCityEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun weatherDao(): WeatherDao
    abstract fun favoriteCityDao(): FavoriteCityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private const val DATABASE_NAME = "weather_database"

        /**
         * Récupère l'instance unique de la base de données (Singleton)
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // En développement uniquement
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
