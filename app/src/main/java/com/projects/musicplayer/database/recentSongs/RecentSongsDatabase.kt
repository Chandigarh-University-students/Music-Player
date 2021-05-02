package com.projects.musicplayer.database.recentSongs

import androidx.room.Database
import androidx.room.Room
import android.content.Context
import androidx.room.RoomDatabase


@Database(entities = [RecentSongEntity::class], version = 1, exportSchema = false)
abstract class RecentSongsDatabase : RoomDatabase() {
    abstract fun recentSongsDao(): RecentSongsDao

    companion object {
        @Volatile
        lateinit var RECENT_SONGS_INSTANCE: RecentSongsDatabase

        //To return Singleton INSTANCE of Database
        fun getRecentSongsDatabase(context: Context): RecentSongsDatabase {
            synchronized(RecentSongsDatabase::class.java) {
                if (!Companion::RECENT_SONGS_INSTANCE.isInitialized) {

                    RECENT_SONGS_INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        RecentSongsDatabase::class.java,
                        "recent_songs_database"
                    )/**.addTypeConverter(RecentSongConverter())*/
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return RECENT_SONGS_INSTANCE
        }
    }
}


