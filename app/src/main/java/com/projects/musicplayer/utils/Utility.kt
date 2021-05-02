package com.projects.musicplayer.utils

import com.projects.musicplayer.database.allSongs.SongEntity

class Utility {
    companion object {
        var songComparator = Comparator<SongEntity> { song1, song2 ->

            val song1Name = song1.songName
            val song2Name = song2.songName
            if(song1Name[0].isLetter() && song2Name[0].isLetter()){
                song1Name.compareTo(song2Name,true)
            }else if(song1Name[0].isLetter()){
                -1
            }
            else if(song2Name[0].isLetter()){
                1
            }
            else
            song1Name.compareTo(song2Name,true)
        }
    }
}