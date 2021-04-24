package com.projects.musicplayer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.projects.musicplayer.rest.PlaylistModel
import com.projects.musicplayer.R
import com.projects.musicplayer.database.PlaylistEntity
import com.projects.musicplayer.database.RecentSongEntity
import com.projects.musicplayer.database.SongEntity

//class PlaylistAdapter (context: Context,var playlists: List<PlaylistModel>) : RecyclerView.Adapter<PlaylistAdapter.AllPlaylistViewHolder>() {
class PlaylistAdapter(context: Context) :
    RecyclerView.Adapter<PlaylistAdapter.AllPlaylistViewHolder>() {

    val mInflater: LayoutInflater = LayoutInflater.from(context)

    private var playlists: List<PlaylistEntity>? = null
    var onPlaylistClickCallback: ((playlist: PlaylistEntity) -> Unit)? = null

    class AllPlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val playlistName: TextView = view.findViewById(R.id.playlistName)
        val playlistCardView: CardView = view.findViewById(R.id.PlaylistsCardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllPlaylistViewHolder {
        val playlistItemView: View = mInflater.inflate(R.layout.single_playlist_item, parent, false)
        return AllPlaylistViewHolder(
            playlistItemView
        )
    }

    override fun onBindViewHolder(holder: AllPlaylistViewHolder, position: Int) {
        val currentPlaylist: PlaylistEntity = playlists!![position]
        holder.playlistName.text = currentPlaylist.name

        holder.playlistCardView.setOnClickListener {
            //TODO open singlePlaylist for this particular playlist
            onPlaylistClickCallback?.invoke(currentPlaylist)
        }


    }


    //    fun setPlayLists(mplaylists: List<PlaylistModel>) {
//        playlists = mplaylists
//        notifyDataSetChanged()
//    }
    fun setPlayLists(mplaylists: List<PlaylistEntity>) {
        playlists = mplaylists
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return if (playlists != null)
            playlists!!.size;
        else 0;
    }

}
