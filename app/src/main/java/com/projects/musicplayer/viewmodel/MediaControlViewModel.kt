package com.projects.musicplayer.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.projects.musicplayer.database.SongEntity
import com.projects.musicplayer.uicomponents.RepeatTriStateButton
import kotlinx.coroutines.runBlocking

class MediaControlViewModel() : ViewModel() {

    var nowPlayingSongs: MutableLiveData<List<SongEntity>> = MutableLiveData()

    var nowPlayingSong: MutableLiveData<SongEntity> = MutableLiveData()

    var isShuffleMode: MutableLiveData<Boolean> = MutableLiveData(false)

    var repeatMode: MutableLiveData<Int> = MutableLiveData(RepeatTriStateButton.NO_REPEAT)

    var currentDuration: MutableLiveData<Long> = MutableLiveData(0)

    var isPlaying: MutableLiveData<Boolean> = MutableLiveData(false)
        private set

    fun togglePlayPause(): Unit {
        isPlaying.value = isPlaying.value == true
    }


    fun shuffleList(): Unit = runBlocking {
        if (nowPlayingSongs != null)
            nowPlayingSongs!!.value = nowPlayingSongs!!.value!!.shuffled()
    }


}