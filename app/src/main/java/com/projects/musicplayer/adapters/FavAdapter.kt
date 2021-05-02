package com.projects.musicplayer.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.projects.musicplayer.R
import com.projects.musicplayer.database.recentSongs.RecentSongEntity
import com.projects.musicplayer.database.allSongs.SongEntity
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class FavAdapter (context: Context
) : RecyclerView.Adapter<FavAdapter.FavViewHolder>() {

    val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var songs: List<SongEntity>? = null

    //callbacks for item click listeners fro updating live data
    var favClickCallback: ((id: Int) -> Unit)? = null
    var onSongClickCallback: (( song : SongEntity, allFavSongs : List<SongEntity>) -> Unit)? = null

    class FavViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtSongName: TextView = view.findViewById(R.id.txtSongName)
        val txtSongArtistName: TextView = view.findViewById(R.id.txtSongArtistName)
        val btnFav: ToggleButton = view.findViewById(R.id.btnFav)
        val cardViewForSong: CardView = view.findViewById(R.id.cardViewForSong)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavViewHolder {
        val songItemView: View = mInflater.inflate(R.layout.single_song_item, parent, false)
        return FavViewHolder(
            songItemView
        )
    }

    override fun onBindViewHolder(holder: FavViewHolder, position: Int) {
        if (songs != null) {

            val currentSong: SongEntity = songs!![position]
            holder.txtSongName.text = currentSong.songName
            holder.txtSongArtistName.text = currentSong.artistName
            holder.btnFav.isChecked = songs!![position].isFav > 0

            holder.btnFav.setOnClickListener {
                favClickCallback?.invoke(currentSong.songId)
                Log.d("SINGLE PLAYLIST INFO", songs.toString())
            }

            holder.cardViewForSong.setOnClickListener {
                onSongClickCallback?.invoke(
                    currentSong,
                    songs!!
                )
            }
        } else {
            //TODO error toast required
            holder.txtSongName.setText(R.string.NoSong)
        }
    }


    fun setSongs(mSongs: List<SongEntity>) {
        songs = mSongs
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return if (songs != null)
            songs!!.size;
        else 0;
    }

}