package com.projects.musicplayer.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.ToggleButton
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.projects.musicplayer.R
import com.projects.musicplayer.database.RecentSongEntity
import com.projects.musicplayer.database.SongEntity
import com.projects.musicplayer.rest.Song
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class SongQueueAdapter (context: Context
) : RecyclerView.Adapter<SongQueueAdapter.SongQueueViewHolder>() {

    val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var songs: List<SongEntity>? = null

    //callbacks for item click listeners fro updating live data
    var favClickCallback: ((id: Int) -> Unit)? = null
    var onSongClickCallback: ((recentSong: RecentSongEntity,song: SongEntity, allFavSongs: List<SongEntity>) -> Unit)? =
        null    //private var onSongClickCallback: ((id: Int) -> Unit)? = null
    var currentPlayingSetSelected: ((currentSOng:SongEntity,cardViewOfSong:RelativeLayout) -> Unit)? = null

    class SongQueueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtSongName: TextView = view.findViewById(R.id.txtSongName)
        val txtSongArtistName: TextView = view.findViewById(R.id.txtSongArtistName)
        val btnFav: ToggleButton = view.findViewById(R.id.btnFav)
        var cardViewForSong: CardView = view.findViewById(R.id.cardViewForSong)
        var relativeLayoutCard:RelativeLayout = view.findViewById(R.id.relativeLayoutCard)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongQueueViewHolder {
        val songItemView: View = mInflater.inflate(R.layout.single_song_item, parent, false)
        return SongQueueViewHolder(
            songItemView
        )
    }

    override fun onBindViewHolder(holder: SongQueueViewHolder, position: Int) {
        if (songs != null) {

            val currentSong: SongEntity = songs!![position]
            holder.txtSongName.text = currentSong.songName
            holder.txtSongArtistName.text = currentSong.artistName
            holder.btnFav.isChecked = songs!![position].isFav > 0

            currentPlayingSetSelected?.invoke(currentSong,holder.relativeLayoutCard)
//            holder.btnFav.isChecked = songs!![position].isFav

            holder.btnFav.setOnClickListener {
//                songs!![position].isFav = !songs!![position].isFav
//                notifyItemChanged(position)
                favClickCallback?.invoke(currentSong.songId)
//                notifyDataSetChanged()
                Log.d("SINGLE PLAYLIST INFO", songs.toString())
            }

            holder.cardViewForSong.setOnClickListener {
                //TODO add to recent, maybe using a callback
                val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"))
                val currentLocalTime = cal.time
                val date: DateFormat =
                    SimpleDateFormat("yyMMddHHmmssZ")                // you can get seconds by adding  "...:ss" to it
                // you can get seconds by adding  "...:ss" to it
                date.setTimeZone(TimeZone.getTimeZone("GMT+1:00"))

                val localTime: String = date.format(currentLocalTime)

                onSongClickCallback?.invoke(RecentSongEntity(currentSong.songId,currentSong.albumCover,localTime),
                    currentSong,
                    songs!!
                )
                Log.d(
                    "RECENTSONGupdated",
                    RecentSongEntity(
                        currentSong.songId,
                        currentSong.albumCover,
                        localTime
                    ).toString()
                )

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
        //TODO add songs taking care of list and String
        songs = mSongs
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return if (songs != null)
            songs!!.size;
        else 0;
    }
}