package com.projects.musicplayer.rest

import androidx.room.Database
import androidx.room.Room
import android.content.Context
import androidx.room.RoomDatabase


@Database(entities = [FavEntity::class], version = 1, exportSchema = false)
abstract class FavRoomDatabase : RoomDatabase() {
    abstract fun favDao(): FavDao

    companion object {
        @Volatile
        lateinit var FAV_INSTANCE: FavRoomDatabase

        //To return Singleton INSTANCE of Database
        fun getFavDatabase(context: Context): FavRoomDatabase {
            synchronized(FavRoomDatabase::class.java) {
                if (!Companion::FAV_INSTANCE.isInitialized) {

                    FAV_INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        FavRoomDatabase::class.java,
                        "fav_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return FAV_INSTANCE
        }
    }
}