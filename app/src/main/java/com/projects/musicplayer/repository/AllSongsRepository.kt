package com.projects.musicplayer.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.projects.musicplayer.database.AllSongsDao
import com.projects.musicplayer.database.AllSongsDatabase
import com.projects.musicplayer.database.SongEntity

class AllSongsRepository(application: Application) {
    private var mAllSongsDao: AllSongsDao

    val db: AllSongsDatabase

    init {
        db = AllSongsDatabase.getAllSongsDatabase(application)
        mAllSongsDao = db.allSongsDao()
        //db.close()
    }

    suspend fun insertSongs(songsList: List<SongEntity>) =
        mAllSongsDao.insertAfterFirstFetch(songsList)


    suspend fun insertSong(songEntity: SongEntity) = mAllSongsDao.insert(songEntity)

    suspend fun removeSong(songEntity: SongEntity) = mAllSongsDao.removeSong(songEntity)

    suspend fun getSongById(id: Int): SongEntity = mAllSongsDao.getSongById(id)

    suspend fun checkFav(id: Int): Int = mAllSongsDao.checkFav(id)

    suspend fun updateFav(id: Int) = mAllSongsDao.updateFav(id)

    suspend fun getAllSongs() = mAllSongsDao.getAllSongs()


    val mAllSongs: LiveData<List<SongEntity>>
        get() {
            return mAllSongsDao.allSongs
        }

    val mFavSongs: LiveData<List<SongEntity>>
        get() {
            return mAllSongsDao.favSongs
        }

}