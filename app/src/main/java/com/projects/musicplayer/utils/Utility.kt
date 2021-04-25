package com.projects.musicplayer.utils

import android.media.MediaMetadataRetriever
import android.util.Log

class Utility {
    companion object {
        fun getAlbumCover(url:String?): ByteArray?  {
            if(url==null)
                return null
            val mmr = MediaMetadataRetriever()

            try {
                mmr.setDataSource(url);
                Log.e("IMAGE","path OBTAINED for this song")
                return mmr.embeddedPicture;
            }
            catch(e:Exception) {


                Log.e("IMAGE", e.message+e.stackTrace.toString()+" for path "+url)
                return null
            }
        }
    }
}