package com.projects.musicplayer.adapters

import android.content.Context
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.projects.musicplayer.R
import com.projects.musicplayer.database.RecentSongEntity
import com.projects.musicplayer.database.SongEntity
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class SinglePlaylistAdapter(
    context: Context
) : RecyclerView.Adapter<SinglePlaylistAdapter.SinglePlaylistViewHolder>() {

    val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var songs: List<SongEntity>? = null

    private var selectedsongId: Int = -1


    private fun setSelectedSongId(p: Int) {
        selectedsongId = p
    }

    fun getSelectedSongId():Int = selectedsongId

    //callbacks for item click listeners fro updating live data
    var favClickCallback: ((id: Int) -> Unit)? = null
    var onSongClickCallback: ((recentSong: RecentSongEntity,song :SongEntity,allSongs:List<SongEntity>) -> Unit)? = null

    class SinglePlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnCreateContextMenuListener {
        val txtSongName: TextView = view.findViewById(R.id.txtSongName)
        val txtSongArtistName: TextView = view.findViewById(R.id.txtSongArtistName)
        val btnFav: ToggleButton = view.findViewById(R.id.btnFav)
        val cardViewForSong: CardView = view.findViewById(R.id.cardViewForSong)

        init {
            view.setOnCreateContextMenuListener(this)
        }


        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu!!.add(0, R.id.ctx_remove_from_playlist, 0, "Remove from Playlist")
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SinglePlaylistViewHolder {
        val songItemView: View = mInflater.inflate(R.layout.single_song_item, parent, false)
        return SinglePlaylistViewHolder(
            songItemView
        )
    }

    override fun onBindViewHolder(holder: SinglePlaylistViewHolder, position: Int) {
        if (songs != null) {

            val currentSong: SongEntity = songs!![position]
            holder.txtSongName.text = currentSong.songName
            holder.txtSongArtistName.text = currentSong.artistName
            holder.btnFav.isChecked = songs!![position].isFav > 0

            holder.btnFav.setOnClickListener {
                favClickCallback?.invoke(currentSong.songId)
                Log.d("SINGLE PLAYLIST INFO", songs.toString())
            }

            holder.cardViewForSong.setOnLongClickListener {
                setSelectedSongId(currentSong.songId)
                Log.i("SELECTEDPLAYLIST",currentSong.toString())
                false
            }

            holder.cardViewForSong.setOnClickListener {
                val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"))
                val currentLocalTime = cal.time
                val date: DateFormat = SimpleDateFormat("yyMMddHHmmssZ")
                date.setTimeZone(TimeZone.getTimeZone("GMT+1:00"))

                val localTime: String = date.format(currentLocalTime)

                onSongClickCallback?.invoke(RecentSongEntity(currentSong.songId,currentSong.albumId,localTime),
                    currentSong,
                songs!!)
                Log.d("RECENTSONGupdated", RecentSongEntity(currentSong.songId,currentSong.albumId,localTime).toString())

            }
        } else {
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