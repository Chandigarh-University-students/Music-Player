package com.projects.musicplayer.database

import androidx.room.Database
import androidx.room.Room
import android.content.Context
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(entities = [RecentSongEntity::class], version = 1, exportSchema = false)
@TypeConverters(RecentSongConverter::class)
abstract class RecentSongsDatabase : RoomDatabase() {
    abstract fun recentSongsDao(): RecentSongsDao

    companion object {
        @Volatile
        lateinit var RECENT_SONGS_INSTANCE: RecentSongsDatabase

        //To return Singleton INSTANCE of Database
        fun getRecentSongsDatabase(context: Context): RecentSongsDatabase {
            synchronized(RecentSongsDatabase::class.java) {
                if (!::RECENT_SONGS_INSTANCE.isInitialized) {

                    RECENT_SONGS_INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        RecentSongsDatabase::class.java,
                        "recent_songs_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return RECENT_SONGS_INSTANCE
        }
    }
}


