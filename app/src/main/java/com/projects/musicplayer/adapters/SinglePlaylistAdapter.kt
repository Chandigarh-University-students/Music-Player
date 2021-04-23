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
import com.projects.musicplayer.database.RecentSongEntity
import com.projects.musicplayer.database.SongEntity
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class SinglePlaylistAdapter(context: Context) : RecyclerView.Adapter<SinglePlaylistAdapter.SinglePlaylistViewHolder>() {

    val mInflater: LayoutInflater = LayoutInflater.from(context)

    //    private var songs: List<Song>? = null
    private var songs: List<SongEntity>? = null

    //callbacks for item click listeners fro updating live data
    var favClickCallback: ((id: Int) -> Unit)? = null
    var onSongClickCallback: ((song: RecentSongEntity) -> Unit)? = null    //private var onSongClickCallback: ((id: Int) -> Unit)? = null

    class SinglePlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtSongName: TextView = view.findViewById(R.id.txtSongName)
        val txtSongArtistName: TextView = view.findViewById(R.id.txtSongArtistName)
        val btnFav: ToggleButton = view.findViewById(R.id.btnFav)
        val cardViewForSong: CardView = view.findViewById(R.id.cardViewForSong)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SinglePlaylistViewHolder {
        val songItemView: View = mInflater.inflate(R.layout.single_song_item, parent, false)
        return SinglePlaylistViewHolder(
            songItemView
        )
    }

    //    override fun onBindViewHolder(holder: AllSongsViewHolder, position: Int) {
//        if (songs != null) {
//            val currentSong: Song = songs!![position]
//            holder.txtSongName.text = currentSong.songName
//            holder.txtSongArtistName.text = currentSong.artistName
//            holder.btnFav.isChecked = songs!![position].isFav
//
//            holder.btnFav.setOnClickListener {
//                songs!![position].isFav = !songs!![position].isFav
//                notifyItemChanged(position)
//                Log.d("ALLSONGINFO",songs.toString())
//            }
//        } else {
//            holder.txtSongName.setText(R.string.NoSong)
//        }
//    }
    override fun onBindViewHolder(holder: SinglePlaylistViewHolder, position: Int) {
        if (songs != null) {
            val currentSong: SongEntity = songs!![position]
//            val currentSong: Song = songs!![position]
            holder.txtSongName.text = currentSong.songName
            holder.txtSongArtistName.text = currentSong.artistName
            holder.btnFav.isChecked = songs!![position].isFav > 0
//            holder.btnFav.isChecked = songs!![position].isFav

            holder.btnFav.setOnClickListener {
//                songs!![position].isFav = !songs!![position].isFav
//                notifyItemChanged(position)
                favClickCallback?.invoke(currentSong.songId)
//                notifyDataSetChanged()
                Log.d("SINGLE PLAYLIST INFO", songs.toString())
            }

            holder.cardViewForSong.setOnClickListener {
                //TODO play the song


                //TODO add to recent, maybe using a callback
                val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"))
                val currentLocalTime = cal.time
                val date: DateFormat = SimpleDateFormat("HH:mm:ss a")
                // you can get seconds by adding  "...:ss" to it
                // you can get seconds by adding  "...:ss" to it
                date.setTimeZone(TimeZone.getTimeZone("GMT+1:00"))

                val localTime: String = date.format(currentLocalTime)

                onSongClickCallback?.invoke(RecentSongEntity(currentSong.songId,currentSong.albumCover,localTime))
                Log.d("RECENTSONGupdated", RecentSongEntity(currentSong.songId,currentSong.albumCover,localTime).toString())

            }
        } else {
            holder.txtSongName.setText(R.string.NoSong)
        }
    }


    //    fun setSongs(mSongs: List<Song>) {
//        songs = mSongs
//        notifyDataSetChanged()
//    }
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