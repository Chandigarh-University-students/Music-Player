package com.projects.musicplayer.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.projects.musicplayer.R
import com.projects.musicplayer.database.PlaylistEntity

class PlaylistDialogAdapter(context: Context) :
    RecyclerView.Adapter<PlaylistDialogAdapter.AllPlaylistViewHolder>() {

    val mInflater: LayoutInflater = LayoutInflater.from(context)

    private var playlists: List<PlaylistEntity>? = null

    var playlistClickCallback: ((id: Int) -> Unit)? = null

    class AllPlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val playlistName: TextView = view.findViewById(R.id.playlistName)
        val playlistItem: CardView = view.findViewById(R.id.FavoritesCardView)
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
        holder.playlistItem.setOnClickListener {
            playlistClickCallback?.invoke(currentPlaylist.id)
        }
    }


    fun setPlayLists(mPlaylists: List<PlaylistEntity>) {
        playlists = mPlaylists
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return if (playlists != null)
            playlists!!.size;
        else 0;
    }

}
