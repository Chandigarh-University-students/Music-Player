package com.projects.musicplayer.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.text.TextUtils.replace
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.projects.musicplayer.R
import com.projects.musicplayer.database.RecentSongEntity
import com.squareup.picasso.Picasso
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class RecentTracksAdapter(val context: Context) :
    RecyclerView.Adapter<RecentTracksAdapter.RecentTrackViewHolder>() {

    val mInflater: LayoutInflater = LayoutInflater.from(context)

    private var songs: List<RecentSongEntity>? = null
   // private var totalTracks: Int? = null

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
            val image=getAlbumCover(songs!![position].albumCover)
            if (image != null) {
                holder.imgSingleRecentTrack.setImageBitmap(BitmapFactory.decodeByteArray(image,0,image.size))
            }
            else{
                holder.imgSingleRecentTrack.setImageResource(R.drawable.drawable_cover)
            }


            holder.cardViewForRecentTrack.setOnClickListener{
                //TODO play song

                //TODO add to recent again, maybe using a callback
                val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"))
                val currentLocalTime = cal.time
                val date: DateFormat = SimpleDateFormat("yyMMddHHmmssZ")
                // you can get seconds by adding  "...:ss" to it
                // you can get seconds by adding  "...:ss" to it
                date.setTimeZone(TimeZone.getTimeZone("GMT+1:00"))

                val localTime: String = date.format(currentLocalTime)
                onSongClickCallback?.invoke(RecentSongEntity(songs!![position].songId,songs!![position].albumCover,localTime))
                Log.d("RECENTSONGupdated", RecentSongEntity(songs!![position].songId,songs!![position].albumCover,localTime).toString())

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

    private fun getAlbumCover(url:String?): ByteArray?  {
        if(url==null)
            return null
        val mmr = MediaMetadataRetriever()

        try {
            mmr.setDataSource(url);
            Log.e("IMAGE","path OBTAINED for this song")
            return mmr.getEmbeddedPicture();
        }
        catch(e:Exception) {


            Log.e("IMAGE", e.message+e.stackTrace.toString()+" for path "+url)
            return null
        }
    }
}