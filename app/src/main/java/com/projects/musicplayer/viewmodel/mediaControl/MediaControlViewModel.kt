package com.projects.musicplayer.viewmodel.mediaControl

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.projects.musicplayer.database.allSongs.SongEntity
import com.projects.musicplayer.uicomponents.RepeatTriStateButton

class MediaControlViewModel() : ViewModel() {

    var isFirstInit:MutableLiveData<Boolean> = MutableLiveData(true)

    var nowPlayingSongs: MutableLiveData<List<SongEntity>> = MutableLiveData()

    var nowPlaylist: MutableLiveData<String> = MutableLiveData()

    var nowPlayingSong: MutableLiveData<SongEntity> = MutableLiveData()

    var isShuffleMode: MutableLiveData<Boolean> = MutableLiveData(false)

    var repeatMode: MutableLiveData<Int> = MutableLiveData(RepeatTriStateButton.NO_REPEAT)


    var isPlaying: MutableLiveData<Boolean> = MutableLiveData(false)
        private set
}