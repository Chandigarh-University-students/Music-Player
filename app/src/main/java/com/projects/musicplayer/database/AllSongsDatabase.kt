package com.projects.musicplayer.database

import androidx.room.Database
import androidx.room.Room
import android.content.Context
import androidx.room.RoomDatabase


@Database(entities = [SongEntity::class], version = 1, exportSchema = false)
abstract class AllSongsDatabase : RoomDatabase() {
    abstract fun allSongsDao(): AllSongsDao

    companion object {
        @Volatile
        lateinit var ALL_SONGS_INSTANCE: AllSongsDatabase

        //To return Singleton INSTANCE of Database
        fun getAllSongsDatabase(context: Context): AllSongsDatabase {
            synchronized(AllSongsDatabase::class.java) {
                if (!::ALL_SONGS_INSTANCE.isInitialized) {

                    ALL_SONGS_INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AllSongsDatabase::class.java,
                        "all_songs_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return ALL_SONGS_INSTANCE
        }
    }
}