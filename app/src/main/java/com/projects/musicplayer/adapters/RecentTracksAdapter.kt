package com.projects.musicplayer.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.projects.musicplayer.R
import com.projects.musicplayer.database.RecentSongEntity
import com.projects.musicplayer.database.SongEntity
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class RecentTracksAdapter(context: Context) :
    RecyclerView.Adapter<RecentTracksAdapter.RecentTrackViewHolder>() {

    val mInflater: LayoutInflater = LayoutInflater.from(context)

    private var songs: List<RecentSongEntity>? = null
   // private var totalTracks: Int? = null

    var onSongClickCallback: ((song: RecentSongEntity) -> Unit)? = null

    class RecentTrackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgSingleRecentTrack: ImageView = view.findViewById(R.id.imgSingleRecentTrack)
        val cardViewForRecentTrack:CardView = view.findViewById(R.id.cardViewForRecentTrack)
    }

    /**
     * Called when RecyclerView needs a new [ViewHolder] of the given type to represent
     * an item.
     *
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     *
     *
     * The new ViewHolder will be used to display items of the adapter using
     * [.onBindViewHolder]. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary [View.findViewById] calls.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new ViewHolder that holds a View of the given view type.
     * @see .getItemViewType
     * @see .onBindViewHolder
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecentTrackViewHolder {
        val songTrackView: View = mInflater.inflate(R.layout.single_recent_track, parent, false)
        return RecentTrackViewHolder(
            songTrackView
        )
    }


    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the [ViewHolder.itemView] to reflect the item at the given
     * position.
     *
     *
     * Note that unlike [android.widget.ListView], RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the `position` parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use [ViewHolder.getAdapterPosition] which will
     * have the updated adapter position.
     *
     * Override [.onBindViewHolder] instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     * item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: RecentTrackViewHolder, position: Int) {
        if (songs != null) {
            holder.imgSingleRecentTrack.setImageResource(R.drawable.drawable_cover) //TODO using songs!![position]

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
}