package com.projects.musicplayer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


//@TypeConverters(PlaylistConverter::class)


@Database(entities = [PlaylistEntity::class], version = 1, exportSchema = false)
abstract class PlaylistRoomDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        lateinit var PLAYLIST_INSTANCE: FavRoomDatabase

        //To return Singleton INSTANCE of Database
        fun getPlaylistDatabase(context: Context): FavRoomDatabase {
            synchronized(FavRoomDatabase::class.java) {
                if (!::PLAYLIST_INSTANCE.isInitialized) {

                    PLAYLIST_INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        FavRoomDatabase::class.java,
                        "playlist_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return PLAYLIST_INSTANCE
        }
    }
}