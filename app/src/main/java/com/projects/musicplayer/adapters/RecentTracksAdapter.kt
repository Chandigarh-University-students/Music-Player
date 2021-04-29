package com.projects.musicplayer.adapters

import android.content.ContentUris
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.projects.musicplayer.R
import com.projects.musicplayer.database.RecentSongEntity
import com.projects.musicplayer.utils.Utility
import com.squareup.picasso.Picasso
import java.lang.Long
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class RecentTracksAdapter(val context: Context) :
    RecyclerView.Adapter<RecentTracksAdapter.RecentTrackViewHolder>() {

    val mInflater: LayoutInflater = LayoutInflater.from(context)

    private var songs: List<RecentSongEntity>? = null

    var onSongClickCallback: ((song: RecentSongEntity) -> Unit)? = null

    class RecentTrackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgSingleRecentTrack: ImageView = view.findViewById(R.id.imgSingleRecentTrack)
        val cardViewForRecentTrack:CardView = view.findViewById(R.id.cardViewForRecentTrack)
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecentTrackViewHolder {
        val songTrackView: View = mInflater.inflate(R.layout.single_recent_track, parent, false)
        return RecentTrackViewHolder(
            songTrackView
        )
    }


    override fun onBindViewHolder(holder: RecentTrackViewHolder, position: Int) {
        if (songs != null) {
            try {
                val genericArtUri = Uri.parse("content://media/external/audio/albumart")
                val actualArtUri =
                    ContentUris.withAppendedId(genericArtUri,
                        Long.parseLong(songs!![position].albumId)
                    )
                Picasso.with(context).load(actualArtUri).error(R.mipmap.default_cover)
                    .into(holder.imgSingleRecentTrack)


            } catch (e: java.lang.Exception) {
                holder.imgSingleRecentTrack.setImageResource(R.mipmap.default_cover)
            }



            holder.cardViewForRecentTrack.setOnClickListener{
                val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"))
                val currentLocalTime = cal.time
                val date: DateFormat = SimpleDateFormat("yyMMddHHmmssZ")
                date.setTimeZone(TimeZone.getTimeZone("GMT+1:00"))

                val localTime: String = date.format(currentLocalTime)
                onSongClickCallback?.invoke(RecentSongEntity(songs!![position].songId,songs!![position].albumId,localTime))
                Log.d("RECENTSONGupdated", RecentSongEntity(songs!![position].songId,songs!![position].albumId,localTime).toString())

            }
        }
    }

    fun addTracks(mSongs: List<RecentSongEntity>) {
        songs=mSongs
        notifyDataSetChanged()
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int {
        return if (songs != null)
            songs!!.size;
        else 0;
    }
}