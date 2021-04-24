package com.projects.musicplayer.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.projects.musicplayer.database.*

class FavSongsRepository(application: Application) {
    private var mFavSongsDao: FavDao

    val db: FavRoomDatabase

    init {
        db = FavRoomDatabase.getFavDatabase(application)
        mFavSongsDao = db.favDao()
        //db.close()
    }

   suspend fun insertSong(favSongEntity: FavEntity) = mFavSongsDao.addToFav(favSongEntity)

    suspend fun removeSong(favSongEntity: FavEntity) = mFavSongsDao.removeFromFav(favSongEntity)

    fun checkFav(id: Int): LiveData<Int> = mFavSongsDao.checkFav(id)

    val mAllFavSongs: LiveData<List<FavEntity>>
        get() {
            return mFavSongsDao.allFav
        }
}