package com.gaelle.royalflushcasino.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PlayerEntity::class, HandEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun handDao(): HandDao

    companion object {
        // Une seule base pour toute l'app, créée au premier besoin
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "casino-database"
                )
                    .fallbackToDestructiveMigration() // comme dans le cours
                    .build()
                    .also { instance = it }
            }
        }
    }
}