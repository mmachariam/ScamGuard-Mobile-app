package com.example.scamguard.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CheckEntity::class],
    version = 1,
    exportSchema = true
)
abstract class ScamGuardDatabase : RoomDatabase() {

    abstract fun checkDao(): CheckDao

    companion object {

        @Volatile
        private var instance: ScamGuardDatabase? = null

        fun getInstance(context: Context): ScamGuardDatabase {
            // Reuse one database instance across the application.
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ScamGuardDatabase::class.java,
                    "scamguard.db"
                )
                    // No destructive fallback: future schema changes need migrations.
                    .build()
                    .also { database ->
                        instance = database
                    }
            }
        }
    }
}