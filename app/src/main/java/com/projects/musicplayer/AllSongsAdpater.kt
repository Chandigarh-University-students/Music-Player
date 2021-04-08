package com.projects.musicplayer


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView

class AllSongsAapter(context: Context) : RecyclerView.Adapter<AllSongsAapter.AllSongsViewHolder>() {

    val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var songs: List<Song>? = null

    class AllSongsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtSongName: TextView = view.findViewById(R.id.txtSongName)
        val txtSongArtistName: TextView = view.findViewById(R.id.txtSongArtistName)
        val btnFav: ToggleButton = view.findViewById(R.id.btnFav)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllSongsViewHolder {
        val songItemView: View = mInflater.inflate(R.layout.single_song_item, parent, false)
        return AllSongsViewHolder(songItemView)
    }

    override fun onBindViewHolder(holder: AllSongsViewHolder, position: Int) {
        if (songs != null) {
            val currentSong: Song = songs!![position]
            holder.txtSongName.text = currentSong.songName
            holder.txtSongArtistName.text = currentSong.artistName
            holder.btnFav.isChecked = songs!![position].isFav

            holder.btnFav.setOnClickListener {
                songs!![position].isFav = !songs!![position].isFav
                notifyItemChanged(position)
                Log.d("ALLSONGINFO",songs.toString())
            }
        } else {
            holder.txtSongName.setText(R.string.NoSong)
        }
    }


    fun setSongs(mSongs: List<Song>) {
        songs = mSongs
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return if (songs != null)
            songs!!.size;
        else 0;
    }

}