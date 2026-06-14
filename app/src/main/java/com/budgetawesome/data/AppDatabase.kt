package com.budgetawesome.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.budgetawesome.data.dao.*
import com.budgetawesome.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * AppDatabase - The main RoomDB database class for Budget Awesome.
 * Version 2 adds the badges table for gamification.
 * Author: Rofhiwa Siaga
 * References: Room Database - https://developer.android.com/training/data-storage/room
 */
@Database(
    entities = [User::class, Category::class, Expense::class, Goal::class, Badge::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun goalDao(): GoalDao
    abstract fun badgeDao(): BadgeDao  // ← THIS was missing

    companion object {
        private const val TAG = "AppDatabase"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Log.d(TAG, "getDatabase: Creating database instance")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_awesome_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d(TAG, "onCreate: Seeding default admin user")
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.userDao()?.insert(
                                    User(username = "admin", password = "admin123")
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}