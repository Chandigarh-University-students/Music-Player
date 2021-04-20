package com.projects.musicplayer

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView

class PlaylistAdapter (context: Context,var playlists: List<PlaylistModel>) : RecyclerView.Adapter<PlaylistAdapter.AllPlaylistViewHolder>() {

    val mInflater: LayoutInflater = LayoutInflater.from(context)

    class AllPlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val playlistName: TextView = view.findViewById(R.id.playlistName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllPlaylistViewHolder {
        val playlistItemView: View = mInflater.inflate(R.layout.single_playlist_item, parent, false)
        return AllPlaylistViewHolder(playlistItemView)
    }

    override fun onBindViewHolder(holder: AllPlaylistViewHolder, position: Int) {
        val currentPlaylist: PlaylistModel = playlists[position]
        holder.playlistName.text = currentPlaylist.playlistName

    }


    fun setSongs(mplaylists: List<PlaylistModel>) {
        playlists = mplaylists
        notifyDataSetChanged()
    }

     override fun getItemCount(): Int {
        return playlists.size;
    }

}
