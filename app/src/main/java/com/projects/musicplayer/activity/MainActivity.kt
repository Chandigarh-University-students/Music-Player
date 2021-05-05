package com.projects.musicplayer.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.projects.musicplayer.R
import com.projects.musicplayer.database.allSongs.SongEntity
import com.projects.musicplayer.database.recentSongs.RecentSongEntity
import com.projects.musicplayer.database.playlists.PlaylistConverter
import com.projects.musicplayer.fragments.*
import com.projects.musicplayer.interfaces.Playable
import com.projects.musicplayer.services.OnClearFromRecentService
import com.projects.musicplayer.uicomponents.RepeatTriStateButton
import com.projects.musicplayer.viewmodel.allSongs.AllSongsViewModel
import com.projects.musicplayer.viewmodel.allSongs.AllSongsViewModelFactory
import com.projects.musicplayer.viewmodel.mediaControl.MediaControlViewModel
import com.projects.musicplayer.viewmodel.playlists.PlaylistViewModel
import com.projects.musicplayer.viewmodel.playlists.PlaylistViewModelFactory
import com.projects.musicplayer.viewmodel.recentSongs.RecentSongsViewModel
import com.projects.musicplayer.viewmodel.recentSongs.RecentSongsViewModelFactory
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import java.lang.Exception
import java.lang.Long.parseLong
import java.lang.Runnable
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), Playable {
    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var mBottomSheetBehavior: BottomSheetBehavior<LinearLayoutCompat>
    lateinit var flFragment: FrameLayout
    lateinit var bottomSheet: LinearLayoutCompat

    lateinit var b_sheet_Collapsed: LinearLayout
    lateinit var b_sheet_Expanded: ConstraintLayout
    lateinit var sharedPreferences: SharedPreferences

    /**Now Playing Controls*/

    /**EXPANDED BOTTOM SHEET ELEMENTS*/
    //toolbar elements
    lateinit var btnMinimizeToolbar: ImageButton
    lateinit var txtCurrPlaying: TextView
    lateinit var btnSongList: ImageButton

    /**Collapsed Bottom sheet**/
    lateinit var b_sheet_CollapsedMusicCover: ImageView
    lateinit var b_sheet_Collapsed_Song: TextView
    lateinit var b_sheet_Collapsed_Artist: TextView
    lateinit var b_sheet_CollapsedMusicControl: ToggleButton

    /**Collapsed Bottom sheet**/


    //current song in now playing
    //lateinit var songNowPlaying: CardView
    lateinit var musicCoverPic: ImageView
    lateinit var txtSongName: TextView
    lateinit var txtSongArtistName: TextView
    lateinit var btnFav: ToggleButton
    //current song in now playing

    lateinit var controlSeekBar: SeekBar
    lateinit var txtCurrentDuration: TextView
    lateinit var txtTotalDuration: TextView
    lateinit var btnControlShuffle: ToggleButton
    lateinit var btnRepeatControl: RepeatTriStateButton

    lateinit var btnPrevControl: ImageButton
    lateinit var btnPlayPauseControl: ToggleButton
    lateinit var btnNextControl: ImageButton


    private val READ_STORAGE_PERMISSION_REQUEST_CODE = 1
    private val TAG = "PermissionDemo"


    //view model related
    private lateinit var mAllSongsViewModel: AllSongsViewModel
    private lateinit var mAllSongsViewModelFactory: AllSongsViewModelFactory

    private lateinit var mRecentSongsViewModel: RecentSongsViewModel
    private lateinit var mRecentSongsViewModelFactory: RecentSongsViewModelFactory

    private lateinit var mMediaControlViewModel: MediaControlViewModel

    private lateinit var mPlaylistViewModel: PlaylistViewModel
    private lateinit var mPlaylistViewModelFactory: PlaylistViewModelFactory

    lateinit var mediaPlayer: MediaPlayer
    lateinit var runnable: Runnable

    val MediaPlayer.seconds: Int
        get() = try{Log.e("Refresh","MediaPlayer.seconds is called"); this.duration / 1000}catch (e:Exception){Log.e("Refresh","Media player was null for MediaPlayer.seconds"); 0}
    val MediaPlayer.currentSeconds: Int
        get() = try{Log.e("Refresh","MediaPlayer.currentSeconds is called"); this.currentPosition / 1000}catch (e:Exception){Log.e("Refresh","Media player was null for MediaPlayer.currentSeconds"); 0}


    //get() = this.currentPosition / 1000
    var homeFragment: HomeFragment = HomeFragment()
    var handler: Handler = Handler((Looper.getMainLooper()))


    //coroutine scopes
    val uiscope = CoroutineScope(Dispatchers.Main)

    lateinit var progressLayout: RelativeLayout
    lateinit var emptyAllSongs: RelativeLayout


    //notification related
    private var mNotifyManager: NotificationManager? = null
    private val mReceiver = NotificationReceiver()

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private val globalIsPlayingSongObserver = androidx.lifecycle.Observer<Boolean> {
        if (mMediaControlViewModel.nowPlayingSong.value != null)
            sendNotification(mMediaControlViewModel.nowPlayingSong.value!!, it)

        Log.i("PLAYBACK STATUS", it.toString())
        btnPlayPauseControl.isChecked = it
        b_sheet_CollapsedMusicControl.isChecked = it
        playPauseMedia(it)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private val globalNowPlayingSongObserver = androidx.lifecycle.Observer<SongEntity> {
        Log.i("PLAYLISTSONG", "New Song Clicked ${it.songName}")
        Log.i("NEXTPREV", mMediaControlViewModel.isFirstInit.value!!.toString())
        if(setUpMediaPlayer(it, !mMediaControlViewModel.isFirstInit.value!!)){
            Log.e("Refresh","setupMediaPlayer returned with true value")
            initializeSeekbar()
            mRecentSongsViewModel.updateRecentSong(
                RecentSongEntity(
                    it.songId,
                    it.albumId,
                    getLocalTime()
                )
            )
            uiscope.launch {
                setUpCollapsedBottomSheetUI(it)
            }
            uiscope.launch {
                setUpExpandedBottomSheetUI(it)
            }
        } else{
            Log.e("Refresh","setupMediaPlayer returned with false value")
            //TODO Now refresh the whole database
            Toast.makeText(this,"Song has been deleted from storage!!",Toast.LENGTH_SHORT).show()
            runBlocking {
                progressLayout.visibility=View.VISIBLE
                Log.i("Refresh","...............................Start method............................")
                refreshDatabases()
                progressLayout.visibility=View.GONE
                Log.i("Refresh","...............................Stop method............................")
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        createNotificationChannel()

        val intentFilter = IntentFilter()
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
        intentFilter.addAction(ACTION_PREVIOUS)
        intentFilter.addAction(ACTION_PLAY_PAUSE)
        intentFilter.addAction(ACTION_NEXT)

        registerReceiver(mReceiver, intentFilter)
        startService(Intent(baseContext, OnClearFromRecentService::class.java))

        progressLayout=findViewById(R.id.progressLayout)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomSheet = findViewById(R.id.bottom_sheet)
        flFragment = findViewById(R.id.frame)
        b_sheet_Collapsed = findViewById(R.id.b_sheet_Collapsed)
        b_sheet_Expanded = findViewById(R.id.b_sheet_Expanded)


        b_sheet_CollapsedMusicCover = findViewById(R.id.b_sheet_CollapsedMusicCover)
        b_sheet_Collapsed_Song = findViewById(R.id.b_sheet_Collapsed_Song)
        b_sheet_Collapsed_Artist = findViewById(R.id.b_sheet_Collapsed_Artist)
        b_sheet_CollapsedMusicControl = findViewById(R.id.b_sheet_CollapsedMusicControl)

        btnMinimizeToolbar = findViewById(R.id.btnMinimizeToolbar)
        txtCurrPlaying = findViewById(R.id.txtCurrPlaying)
        btnSongList = findViewById(R.id.btnSongList)

        //songNowPlaying = findViewById(R.id.songNowPlaying)
        musicCoverPic = findViewById(R.id.musicCoverPic)
        txtSongName = findViewById(R.id.txtSongName)
        txtSongArtistName = findViewById(R.id.txtSongArtistName)
        btnFav = findViewById(R.id.btnFav)
        controlSeekBar = findViewById(R.id.controlSeekBar)
        txtCurrentDuration = findViewById(R.id.txtCurrentDuration)
        txtTotalDuration = findViewById(R.id.txtTotalDuration)
        btnControlShuffle = findViewById(R.id.btnControlShuffle)
        btnRepeatControl = findViewById(R.id.btnRepeatControl)

        btnPrevControl = findViewById(R.id.btnPrevControl)
        btnPlayPauseControl = findViewById(R.id.btnPlayPauseControl)
        btnNextControl = findViewById(R.id.btnNextControl)
        emptyAllSongs = findViewById(R.id.emptyAllSongs)

        sharedPreferences = getSharedPreferences(
            "Audio DB Preferences",
            Context.MODE_PRIVATE
        )

        //init View Model object
        //use of view model factory to pass parameter to view model
        /** Viewmodel for AllSongs*/
        mAllSongsViewModelFactory =
            AllSongsViewModelFactory(
                application
            )
        mAllSongsViewModel =
            ViewModelProvider(this, mAllSongsViewModelFactory).get(AllSongsViewModel::class.java)

        /** Viewmodel for MediaControl*/
        mMediaControlViewModel =
            ViewModelProvider(this).get(MediaControlViewModel::class.java)

        /** Viewmodel for RecentSongs*/
        mRecentSongsViewModelFactory =
            RecentSongsViewModelFactory(
                application
            )
        mRecentSongsViewModel =
            ViewModelProvider(
                this,
                mRecentSongsViewModelFactory
            ).get(RecentSongsViewModel::class.java)


        /** Viewmodel for playlists*/
        mPlaylistViewModelFactory =
            PlaylistViewModelFactory(
                application
            )
        mPlaylistViewModel =
            ViewModelProvider(this, mPlaylistViewModelFactory).get(PlaylistViewModel::class.java)


        mAllSongsViewModel.allSongs.observe(this, Observer {
            //TODO Aman
            if (it.isNullOrEmpty()) {
                if (isDatabaseInitialized()) {
                    Log.i("FavInQueue", "Database initialized")
                    bottomNavigationView.visibility = View.GONE
                    progressLayout.visibility = View.GONE
                    emptyAllSongs.visibility=View.VISIBLE
                    mBottomSheetBehavior.isHideable = true
                    mBottomSheetBehavior.state=BottomSheetBehavior.STATE_HIDDEN
                }
                else{
                    Log.i("FavInQueue","Database not initialized")
                }
            }
            else {
                progressLayout.visibility = View.GONE
                emptyAllSongs.visibility=View.GONE
                if(mBottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                    mBottomSheetBehavior.isHideable = false
                    when (supportFragmentManager.findFragmentById(R.id.frame)) {
                        is SinglePlaylistFragment ->  bottomNavigationView.visibility=View.VISIBLE
                        is HomeFragment ->  bottomNavigationView.visibility=View.VISIBLE
                        else ->  bottomNavigationView.visibility=View.GONE
                    }
                    mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
            Log.i("FavInQueue","Current queue is ${mMediaControlViewModel.nowPlaylist.value}")
            val currentQueue = mMediaControlViewModel.nowPlayingSongs.value
            val updatedCurrentQueue = mutableListOf<SongEntity>()
            Log.d("FavInQueue", mMediaControlViewModel.nowPlayingSongs.value.toString())
            Log.d("FavInQueue", it.toString())
            if (currentQueue != null) {
                for (song in currentQueue) {
                    val songComplement =
                        SongEntity(
                            song.songId,
                            song.songName,
                            song.artistName,
                            song.duration,
                            song.albumId,
                            song.isFav * (-1)
                        )
                    if (song in it)
                        updatedCurrentQueue.add(song)
                    else if (songComplement in it)
                        updatedCurrentQueue.add(songComplement)
                    else {
                        Log.d("FavInQueue", "Deleted song")
                        //updatedCurrentQueue.add(song)
                    }
                }
                mMediaControlViewModel.nowPlayingSongs.value = updatedCurrentQueue
                Log.d(
                    "FavInQueue",
                    "Live data for nowPlayingSongs is updated due to change in fav"
                )
            } else {
                Log.d("FavInQueue", "Doing nothing since no songs are being played")
            }
        })


        mMediaControlViewModel.isPlaying.observeForever(globalIsPlayingSongObserver)

        mMediaControlViewModel.isShuffleMode.observe(this, Observer {
            if (it) {
                val currentList = mMediaControlViewModel.nowPlayingSongs.value
                mMediaControlViewModel.nowPlayingSongs.value = currentList!!.shuffled()
            }
        })



        mMediaControlViewModel.nowPlayingSong.observeForever(globalNowPlayingSongObserver)

        mMediaControlViewModel.nowPlayingSongs.observe(this, Observer {
            Log.i("PLAYLIST", "New playlist added ${it.toString()}")
            uiscope.launch {
                /**This will set NowPlaying UI to latest nowPlayingSong with correct isFav*/
                setUpExpandedBottomSheetUI(mMediaControlViewModel.nowPlayingSong.value!!)
                setUpCollapsedBottomSheetUI(mMediaControlViewModel.nowPlayingSong.value!!)
            }
        })

        mMediaControlViewModel.nowPlaylist.observe(this, Observer {
            Log.i("PLAYLISTNAME", "New playlist added ${it}")
        })



        Log.i("Req",isDatabaseInitialized().toString())
        if (!isDatabaseInitialized()) {
            Log.i("Req","Preparing for fetching")
            prepare(ContextWrapper(applicationContext).contentResolver)}


        setUpBottomSheet()

        initUI()

        setUpBottomNav()

        setUpExpandedNowPlaying()


        b_sheet_CollapsedMusicControl.setOnCheckedChangeListener(
            object : CompoundButton.OnCheckedChangeListener {
                /**
                 * Called when the checked state of a compound button has changed.
                 *
                 * @param buttonView The compound button view whose state has changed.
                 * @param isChecked  The new checked state of buttonView.
                 */
                override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
//                    uiscope.launch {
                    mMediaControlViewModel.isPlaying.value = isChecked
//                    }
                }

            }
        )

        btnPlayPauseControl.setOnCheckedChangeListener { _, isChecked ->
            uiscope.launch {
                Log.i("PlayPause","PlayPause Button has changed its state = $isChecked")
                Log.i("PlayPause","PlayPause Button has changed its state for = ${mMediaControlViewModel.nowPlayingSong.value?.songName}")
                mMediaControlViewModel.isPlaying.value = isChecked


            }
        }

        btnControlShuffle.setOnCheckedChangeListener { _, isChecked ->
            uiscope.launch {
                mMediaControlViewModel.isShuffleMode.value = isChecked
            }
        }

        btnRepeatControl.addCheckedStateCallback(object :
            RepeatTriStateButton.CheckedStateCallback() {
            override fun onStateChanged(newState: Int) {
                uiscope.launch {
                    mMediaControlViewModel.repeatMode.value = newState
                }
            }
        })
    }

    fun getLocalTime():String
    {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"))
        val currentLocalTime = cal.time
        val date: DateFormat = SimpleDateFormat("yyMMddHHmmssZ")
        date.setTimeZone(TimeZone.getTimeZone("GMT+1:00"))
        val localTime: String = date.format(currentLocalTime)
        return localTime
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setUpMediaPlayer(songEntity: SongEntity, toPlay: Boolean = true):Boolean {
        clearMediaPlayer()


        val songUri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songEntity.songId.toLong()
        )
        Log.e("Refresh","Song uri now is $songUri")
        Log.e("Refresh","mediaPlayer intialized = ${this::mediaPlayer.isInitialized}")
        try{
            mediaPlayer = MediaPlayer.create(applicationContext, songUri)
        }catch (e:Exception){
            //TODO delete this song from everywhere,print a toast message
            Log.e("Refresh","Media player was null inside setUpMediaPlayer")
            Log.e("Refresh","mediaPlayer intialized = ${this::mediaPlayer.isInitialized}")
            return false
        }

        sendNotification(songEntity, toPlay)
        if (toPlay)
            uiscope.launch {
                mMediaControlViewModel.isPlaying.value = true
            }
        else {
            mMediaControlViewModel.isFirstInit.value = false
        }

        mediaPlayer.setOnCompletionListener {
            Log.i("COMPLETE",it.isPlaying().toString())
            val currSong=mMediaControlViewModel.nowPlayingSong.value
            val currSongQueue=mMediaControlViewModel.nowPlayingSongs.value
            val currSongPosition = currSongQueue?.indexOf(currSong)
            val maxSongPosition = (currSongQueue?.size)?.minus(1)
            val repeatState = mMediaControlViewModel.repeatMode.value
            Log.i("NEXTPREV",currSong.toString())
            Log.i("NEXTPREV",currSongPosition.toString())
            Log.i("NEXTPREV",maxSongPosition.toString())

            if(currSongPosition!=null && repeatState!=null) {
                when(repeatState){
                    RepeatTriStateButton.NO_REPEAT -> {
                        if(currSongPosition==maxSongPosition){
                            // move to the first song and pause
                            runBlocking {
                                mMediaControlViewModel.nowPlayingSong.value=currSongQueue[0]
                            }
                            uiscope.launch {
                                mMediaControlViewModel.isPlaying.value = false
                            }

                        }else{
                            // Move to next song
                            mMediaControlViewModel.nowPlayingSong.value= currSongQueue[currSongPosition+1]
                        }
                    }
                    RepeatTriStateButton.REPEAT_ONE -> {
                        // Play Again
                        mMediaControlViewModel.nowPlayingSong.value= currSongQueue[currSongPosition]
                    }
                    RepeatTriStateButton.REPEAT_ALL -> {
                        if(currSongPosition==maxSongPosition){
                            // Start first song
                            mMediaControlViewModel.nowPlayingSong.value=currSongQueue[0]
                        }else{
                            // Move to next song
                            mMediaControlViewModel.nowPlayingSong.value= currSongQueue[currSongPosition+1]
                        }
                    }
                    else -> {Log.e("NEXTPREV","repeatState invalid")}
                }
            }else{
                Log.e("NEXTPREV","currSongPosition or repeatState is null")
            }

        }
        return true
}

    fun initializeSeekbar() {

        try{
            controlSeekBar.max = mediaPlayer.seconds
            txtTotalDuration.text = getDuration(mediaPlayer.seconds.toLong())
            runnable = Runnable {
                controlSeekBar.progress = mediaPlayer.currentSeconds
                txtCurrentDuration.text = getDuration(mediaPlayer.currentSeconds.toLong())
                handler.postDelayed(runnable, 1000)
            }
            handler.postDelayed(runnable, 1000)
        }catch (e:Exception){
            Log.e("Refresh","Media player was null inside seekbar")
        }
    }

    fun getDuration(seconds: Long): String {
        val time = String.format(
            "%02d : %02d",
            TimeUnit.SECONDS.toMinutes(seconds),
            seconds - TimeUnit.MINUTES.toSeconds(
                TimeUnit.SECONDS.toMinutes(seconds)
            )

        )
        return time
    }

    private suspend fun setUpCollapsedBottomSheetUI(songEntity: SongEntity) {
        withContext(Dispatchers.Main) {
            b_sheet_Collapsed_Song.text = songEntity.songName
            b_sheet_Collapsed_Artist.text = songEntity.artistName

            try {
                val genericArtUri = Uri.parse("content://media/external/audio/albumart")
                val actualArtUri =
                    ContentUris.withAppendedId(genericArtUri, parseLong(songEntity.albumId))
                Picasso.with(this@MainActivity).load(actualArtUri).error(R.mipmap.default_cover)
                    .into(b_sheet_CollapsedMusicCover)


            } catch (e: java.lang.Exception) {
                b_sheet_CollapsedMusicCover.setImageResource(R.mipmap.default_cover)
            }
        }
    }


    private fun playPauseMedia(play: Boolean) {
        val pause = !play
        if (this::mediaPlayer.isInitialized) {
            if (pause) {
                mediaPlayer.pause()
            } else  {
                mediaPlayer.start()
            }
        }
    }


    private suspend fun setUpExpandedBottomSheetUI(songEntity: SongEntity) {
        withContext(Dispatchers.Main) {
            txtSongName.text = songEntity.songName
            txtSongArtistName.text = songEntity.artistName
            btnFav.isChecked = songEntity.isFav?.let{
                when(it){
                    -1 -> false
                    else -> true
                }
            }

            try {
                val genericArtUri = Uri.parse("content://media/external/audio/albumart")
                val actualArtUri =
                    ContentUris.withAppendedId(genericArtUri, parseLong(songEntity.albumId))
                Picasso.with(this@MainActivity).load(actualArtUri).error(R.mipmap.default_cover)
                    .into(musicCoverPic)


            } catch (e: java.lang.Exception) {
                musicCoverPic.setImageResource(R.mipmap.default_cover)
            }
        }

    }


    private fun setUpExpandedNowPlaying() {
        btnMinimizeToolbar.setOnClickListener {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            when (supportFragmentManager.findFragmentById(R.id.frame)) {
                is HomeFragment -> bottomNavigationView.selectedItemId = R.id.home_button
                is PlaylistsFragment -> bottomNavigationView.selectedItemId = R.id.tab_playlist
                else -> {}
            }
        }

        btnSongList.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.frame,
                    SongsQueueFragment()
                ).commit()

            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
           }

        btnNextControl.setOnClickListener {
            // play next song in list
            Log.i("NEXTPREV",mMediaControlViewModel.nowPlayingSong.value.toString())
                val currSong=mMediaControlViewModel.nowPlayingSong.value
                val currSongQueue=mMediaControlViewModel.nowPlayingSongs.value
                val currSongPosition = currSongQueue?.indexOf(currSong)
                val maxSongPosition = (currSongQueue?.size)?.minus(1)
                val repeatState = mMediaControlViewModel.repeatMode.value
                Log.i("NEXTPREV",currSong.toString())
                Log.i("NEXTPREV",currSongPosition.toString())
                Log.i("NEXTPREV",maxSongPosition.toString())
                if(currSongPosition!=null && repeatState!=null) {
                    when(repeatState){
                        RepeatTriStateButton.NO_REPEAT -> {
                            if(currSongPosition==maxSongPosition){
                                // move to the first song and pause
                                runBlocking {
                                    mMediaControlViewModel.isPlaying.value = true
                                }
                                runBlocking {
                                    mMediaControlViewModel.nowPlayingSong.value=currSongQueue[0]
                                }
                                uiscope.launch {
                                    mMediaControlViewModel.isPlaying.value = false
                                }
                            }else{
                                // Move to next song
                                mMediaControlViewModel.nowPlayingSong.value= currSongQueue[currSongPosition+1]
                            }
                        }
                        RepeatTriStateButton.REPEAT_ONE -> {
                                // Play Again
                            mMediaControlViewModel.nowPlayingSong.value= currSongQueue[currSongPosition]
                        }
                        RepeatTriStateButton.REPEAT_ALL -> {
                            if(currSongPosition==maxSongPosition){
                                // Start first song
                                mMediaControlViewModel.nowPlayingSong.value=currSongQueue[0]
                            }else{
                                // Move to next song
                                mMediaControlViewModel.nowPlayingSong.value= currSongQueue[currSongPosition+1]
                            }
                        }
                        else -> {Log.e("NEXTPREV","repeatState invalid")}
                    }
                }else{
                    Log.e("NEXTPREV","currSongPosition or repeatState is null")
                }
        }

        btnPrevControl.setOnClickListener {
            // play prev song
            Log.i("NEXTPREV",mMediaControlViewModel.nowPlayingSong.value.toString())
                val currSong=mMediaControlViewModel.nowPlayingSong.value
                val currSongQueue=mMediaControlViewModel.nowPlayingSongs.value
                val currSongPosition = currSongQueue?.indexOf(currSong)
                val maxSongPosition = (currSongQueue?.size)?.minus(1)
                val repeatState = mMediaControlViewModel.repeatMode.value
                Log.i("NEXTPREV",currSong.toString())
                Log.i("NEXTPREV",currSongPosition.toString())
                Log.i("NEXTPREV",maxSongPosition.toString())
                if(currSongPosition!=null && repeatState!=null) {
                    when(repeatState){
                        RepeatTriStateButton.NO_REPEAT -> {
                            if(currSongPosition==0){
                                // move to the first song again
                                runBlocking {
                                    mMediaControlViewModel.nowPlayingSong.value=currSongQueue[0]
                                }
                            }else{
                                // Move to prev song
                                mMediaControlViewModel.nowPlayingSong.value= currSongQueue[currSongPosition-1]
                            }
                        }
                        RepeatTriStateButton.REPEAT_ONE -> {
                            // Play Again
                            mMediaControlViewModel.nowPlayingSong.value= currSongQueue[currSongPosition]
                        }
                        RepeatTriStateButton.REPEAT_ALL -> {
                            if(currSongPosition==0){
                                // Start last song
                                mMediaControlViewModel.nowPlayingSong.value=currSongQueue[maxSongPosition!!]
                            }else{
                                // Move to prev song
                                mMediaControlViewModel.nowPlayingSong.value= currSongQueue[currSongPosition-1]
                            }
                        }
                        else -> {Log.e("NEXTPREV","repeatState invalid")}
                    }
                }else{
                    Log.e("NEXTPREV","currSongPosition or repeatState is null")
                }
        }

        btnFav.setOnClickListener{
            Log.i("PLAYINGFAV","btnFav is clicked and value = ${btnFav.isChecked}")
            runBlocking {
                    /**This does not call any observer*/
                    mMediaControlViewModel.nowPlayingSong.value?.isFav  = mMediaControlViewModel.nowPlayingSong.value?.isFav?.times((-1))!!
                    Log.i("PLAYINGFAV","Value of nowPlaying is fav from clickListener= ${mMediaControlViewModel.nowPlayingSong.value}")

            }
            uiscope.launch {
                mAllSongsViewModel.updateFav(mMediaControlViewModel.nowPlayingSong.value?.songId!!)
            }

        }


        controlSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar,
                    progresValue: Int, fromUser: Boolean
                ) {
                    if (fromUser) {
                        mediaPlayer.seekTo(progresValue * 1000)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {

                }
            })

        //testing callback for evey state change
        btnRepeatControl.addCheckedStateCallback(
            object :
                RepeatTriStateButton.CheckedStateCallback() {

                override fun onStateChanged(newState: Int) {
                    when (newState) {
                        RepeatTriStateButton.NO_REPEAT -> {
                            Toast.makeText(
                                this@MainActivity,
                                "NO_REPEAT",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        RepeatTriStateButton.REPEAT_ALL -> {
                            Toast.makeText(
                                this@MainActivity,
                                "REPEAT_ALL",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        RepeatTriStateButton.REPEAT_ONE -> {
                            Toast.makeText(
                                this@MainActivity,
                                "REPEAT_ONE",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> println("DEFAULT STATE")
                    }
                }

            }
        )

    }


    fun initUI() {
        //initially load home_fragment into frame layout...

        bottomNavigationView.selectedItemId =
            R.id.home_button

        supportFragmentManager.beginTransaction()
            .replace(
                R.id.frame,
                homeFragment
            ).commit()
        //set initial state of bottom sheet
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun clearMediaPlayer() {
        try{
            if (this::mediaPlayer.isInitialized) {
                mediaPlayer.stop()
                mediaPlayer.reset()
                mediaPlayer.release()
            }
        }catch (e:Exception){
            Log.e("Refresh","Media player was null inside clearMediaPlayer")
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent != null) {
            val notificationTap = intent.getBooleanExtra("fromNotification", false)
            if (notificationTap) {
                Log.i("CREATE-INTENT", "onNewIntent CALLED")
                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDestroy() {
        super.onDestroy()
        Log.i("DESTROY", "ACTIVITY DESTROYED")
        clearMediaPlayer()
        // remove all notifications with created notification id when app destroyed
        mNotifyManager!!.cancel(NOTIFICATION_ID)
        unregisterReceiver(mReceiver)

        //remove global observers
        mMediaControlViewModel.isPlaying.removeObserver(globalIsPlayingSongObserver)
        mMediaControlViewModel.nowPlayingSong.removeObserver(globalNowPlayingSongObserver)
    }

    fun setUpBottomNav() {
        //styling bottom navigation
        //get radius
        val radius = resources.getDimension(R.dimen.radius_small)
        //get bottom nav background view
        val bottomNavigationViewBackground =
            bottomNavigationView.background as MaterialShapeDrawable
        //manually change background appearance
        bottomNavigationViewBackground.shapeAppearanceModel =
            bottomNavigationViewBackground.shapeAppearanceModel.toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, radius)
                .build()

        //tab click listeners
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_button -> {
                    mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            HomeFragment()
                        ).commit()
                    true
                }
                R.id.nowPlaying -> {
                    bottomNavigationView.visibility = View.GONE
                    mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    true
                }
                R.id.tab_playlist -> {
                    mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    supportFragmentManager.beginTransaction().replace(
                        R.id.frame,
                        PlaylistsFragment()
                    ).commit()
                    true
                }
            }
            true

        }

    }

    fun setUpBottomSheet() {
        //define bottomsheet behaviour
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        mBottomSheetBehavior.isDraggable = false


        mBottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {

            /**
             * Called when the bottom sheet changes its state.
             *
             * @param bottomSheet The bottom sheet view.
             * @param newState The new state. This will be one of [.STATE_DRAGGING], [     ][.STATE_SETTLING], [.STATE_EXPANDED], [.STATE_COLLAPSED], [     ][.STATE_HIDDEN], or [.STATE_HALF_EXPANDED].
             */
            override fun onStateChanged(bottomSheet: View, newState: Int) {

                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        //make bottom nav visible when bottom sheet collapsed
                        bottomNavigationView.visibility = View.VISIBLE
                        b_sheet_Collapsed.visibility = View.VISIBLE
                        b_sheet_Expanded.visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        //make bottom nav hidden when bottom sheet expanded
                        bottomNavigationView.visibility = View.GONE
                        b_sheet_Collapsed.visibility = View.GONE
                        b_sheet_Expanded.visibility = View.VISIBLE
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }
                    else -> {
                      }
                }
            }

            /**
             * Called when the bottom sheet is being dragged.
             *
             * @param bottomSheet The bottom sheet view.
             * @param slideOffset The new offset of this bottom sheet within [-1,1] range. Offset increases
             * as this bottom sheet is moving upward. From 0 to 1 the sheet is between collapsed and
             * expanded states and from -1 to 0 it is between hidden and collapsed states.
             */
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

                val currentHeight = flFragment.height - bottomSheet.height
                val bottomShiftDown = currentHeight - bottomSheet.top
                flFragment.setPadding(
                    0, 0, 0, bottomSheet.height + bottomShiftDown
                )
            }

        })


        bottomSheet.setOnClickListener {
            if (mBottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomNavigationView.visibility = View.GONE
                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    override fun onBackPressed() {
        //if bottom sheet expanded simply collapse it to show its underlying view automatically
        if (mBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

            when (supportFragmentManager.findFragmentById(R.id.frame)) {
                is HomeFragment -> bottomNavigationView.selectedItemId = R.id.home_button
                is PlaylistsFragment -> bottomNavigationView.selectedItemId = R.id.tab_playlist
                else -> {}
            }

        } else
            when (supportFragmentManager.findFragmentById(R.id.frame)) {
                is SinglePlaylistFragment -> openPlaylistFrag()
                is FavFragment -> openPlaylistFrag()
                !is HomeFragment -> initUI()
                is HomeFragment -> moveTaskToBack(false)
                else -> super.onBackPressed()
            }
    }

    private fun openPlaylistFrag() {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.frame,
                PlaylistsFragment()
            ).commit()
    }


    private fun isDatabaseInitialized(): Boolean =
        sharedPreferences.getBoolean("songLoaded", false)

    private fun prepare(mContentResolver: ContentResolver) {

        val mSongs = mutableListOf<SongEntity>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cur: Cursor? = mContentResolver.query(
            uri,
            null,
            MediaStore.Audio.Media.IS_MUSIC + "!= 0",
            null,
            MediaStore.Audio.Media.TITLE + " ASC"
        )

        if (cur != null && cur.moveToFirst()) {
            val albumArtColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cur.getColumnIndex(MediaStore.Audio.Media.DURATION)
            val artistColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val titleColumn = cur.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val idColumn = cur.getColumnIndex(MediaStore.Audio.Media._ID)
            do {
                mSongs.add(
                    SongEntity(
                        cur.getInt(idColumn),
                        cur.getString(titleColumn),
                        cur.getString(artistColumn),
                        cur.getLong(durationColumn),
                        cur.getLong(albumArtColumn).toString(),
                        -1
                    )
                )
            } while (cur.moveToNext())
            cur.close()

            mAllSongsViewModel.insertSongs(mSongs)
            sharedPreferences.edit().putBoolean("songLoaded", true).apply()


        } else {
            return
        }
    }

    private fun refreshDatabases()
    {
        //TODO fetch again and set all databases
        var oldSongsList:MutableList<SongEntity>
        runBlocking{
            oldSongsList = mAllSongsViewModel.getAllSongs() as MutableList<SongEntity>
        }
        var newSongsList = prepareRefresh(ContextWrapper(applicationContext).contentResolver)
        var sameSongs = mutableListOf<SongEntity>()
        var newAddedSongs = mutableListOf<SongEntity>()
        var deletedSongs = mutableListOf<SongEntity>()

        Log.i("Refresh","Old song list size - ${oldSongsList.size}")
        Log.i("Refresh","New song list size - ${newSongsList.size}")

        for(song in newSongsList){
            val songComplement =
                SongEntity(
                    song.songId,
                    song.songName,
                    song.artistName,
                    song.duration,
                    song.albumId,
                    song.isFav * (-1)
                )
            if(oldSongsList.remove(song) || oldSongsList.remove(songComplement)){
                sameSongs.add(song)
            }else{
                newAddedSongs.add(song)
            }
        }
        deletedSongs = oldSongsList
        Log.i("Refresh","Common song list size - ${sameSongs.size}")
        Log.i("Refresh","New added song list  - $newAddedSongs")
        Log.i("Refresh","Deleted song list  - $deletedSongs")

        //TODO Now reset databases according to added and deleted songs
        /**Setting database for newly added songs**/
        mAllSongsViewModel.insertSongs(newAddedSongs)
        /**Both play queue and home adapter will be reloaded*/

        /**Setting database for deleted songs**/

        for(song in deletedSongs){
            Log.i("Refresh","Deleting song  - $song")

            /** Removing form AllSongs*/
            mAllSongsViewModel.removeSong(song)

            /** Removing form RecentSongs*/
            mRecentSongsViewModel.deleteRecentSong(RecentSongEntity(song.songId,song.albumId,getLocalTime()))

            /** Removing form Playlists*/
            var playlistIdList : List<Int>
            runBlocking {
                playlistIdList = mPlaylistViewModel.getAllPlaylists()
            }
            for(playlistId in playlistIdList) {
                Log.i("Refresh","Deleting song $song from playlist $playlistId")
                var songs: String? = "Sample"
                runBlocking {
                    songs = mPlaylistViewModel.getPlaylistSongsById(playlistId)
                }
                uiscope.launch {
                    val listOfSongs: List<Int>? = PlaylistConverter.toList(songs)
                    if (listOfSongs == null)
                        Log.e("Refresh", "Empty Playlist")
                    else {
                        val mutableSongs = (listOfSongs as MutableList<Int>)
                        if(mutableSongs.remove(song.songId)){
                            Log.i("Refresh", "${song.songName} found in $playlistId")
                            mPlaylistViewModel.updatePlaylist(playlistId, mutableSongs)
                        }else{
                            Log.i("Refresh", "${song.songName} not found in $playlistId")
                        }
                    }
                }
            }

            /** Removing form NowPlaying*/
            if (song.songId == mMediaControlViewModel.nowPlayingSong.value?.songId ) {
                Log.i("Refresh", "Deleted song also in Now Playing")
                var allSongs : List<SongEntity>
                runBlocking {
                    allSongs = mAllSongsViewModel.getAllSongs()
                }
                if(!allSongs.isNullOrEmpty()) {
                    Log.i("Refresh", "Setting now playing song as random song from all songs")
                    runBlocking {
                        if(mMediaControlViewModel.isPlaying.value==false){
                            Log.i("Refresh", "song was paused already")
                            mMediaControlViewModel.isFirstInit.value = true
                        }else{
                            Log.i("Refresh", "song was played already")
                        }
                    }
                    runBlocking {
                        mMediaControlViewModel.nowPlaylist.value = "All Songs"
                        mMediaControlViewModel.nowPlayingSongs.value = allSongs
                        mMediaControlViewModel.nowPlayingSong.value = allSongs.random()
                    }
                }
                else{
                    Log.i("Refresh", "All songs empty message should be on screen automatically")
                }
            }
        }
    }

    private fun prepareRefresh(mContentResolver: ContentResolver) : MutableList<SongEntity>{

        val mSongs = mutableListOf<SongEntity>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cur: Cursor? = mContentResolver.query(
            uri,
            null,
            MediaStore.Audio.Media.IS_MUSIC + "!= 0",
            null,
            MediaStore.Audio.Media.TITLE + " ASC"
        )

        if (cur != null && cur.moveToFirst()) {
            val albumArtColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cur.getColumnIndex(MediaStore.Audio.Media.DURATION)
            val artistColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val titleColumn = cur.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val idColumn = cur.getColumnIndex(MediaStore.Audio.Media._ID)
            do {
                mSongs.add(
                    SongEntity(
                        cur.getInt(idColumn),
                        cur.getString(titleColumn),
                        cur.getString(artistColumn),
                        cur.getLong(durationColumn),
                        cur.getLong(albumArtColumn).toString(),
                        -1
                    )
                )
            } while (cur.moveToNext())
            cur.close()

            //TODO mSongs has all updated songs
            return mSongs
            //mAllSongsViewModel.insertSongs(mSongs)
            //sharedPreferences.edit().putBoolean("songLoaded", true).apply()


        } else {
            return mSongs
        }
    }

    private fun createNotificationChannel() {
        mNotifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {
            // Create a NotificationChannel
            val notificationChannel = NotificationChannel(
                PRIMARY_CHANNEL_ID,
                "Mascot Notification", NotificationManager.IMPORTANCE_DEFAULT
            )


            notificationChannel.setSound(null, null)
            notificationChannel.enableVibration(false)
            notificationChannel.description = "Notification from Mascot"
            mNotifyManager?.createNotificationChannel(notificationChannel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun sendNotification(track: SongEntity, isPlaying: Boolean) {
        val intentPrevious = Intent(ACTION_PREVIOUS)
        val previousPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intentPrevious,
            0
        )
        val drawablePrevious = R.drawable.ic_previous_control

        val intentPlayPause = Intent(ACTION_PLAY_PAUSE)
        val playPausePendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            intentPlayPause,
            0
        )
        val drawablePlayPause =
            if (!isPlaying) R.drawable.ic_baseline_play_arrow_24 else R.drawable.ic_baseline_pause_24

        val intentNext = Intent(ACTION_NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(
            this,
            2,
            intentNext,
            0
        )
        val drawableNext = R.drawable.ic_next_control


        //get notification from notification builder
        val notifyBuilder = getNotificationBuilder(track)

        notifyBuilder.setOngoing(true)

        notifyBuilder.addAction(drawablePrevious, "Previous", previousPendingIntent)
        notifyBuilder.addAction(drawablePlayPause, "PlayPause", playPausePendingIntent)
        notifyBuilder.addAction(drawableNext, "Next", nextPendingIntent)

        notifyBuilder.setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2)

        )

        mNotifyManager?.notify(NOTIFICATION_ID, notifyBuilder.build())

    }

    private fun getNotificationBuilder(track: SongEntity): NotificationCompat.Builder {

        //DONE: handle open now playing expanded when notification is tapped
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        notificationIntent.putExtra("fromNotification", true)
        val notificationPendingIntent = PendingIntent.getActivity(
            this,
            NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        //build initial notification
        val notifyBuilder = NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
            .setSmallIcon(R.mipmap.default_cover_foreground)
            .setContentTitle(track.songName)
            .setContentText(track.artistName)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(notificationPendingIntent)
            .setColor(ContextCompat.getColor(this, R.color.primaryColor))

        //TEST DONE: FIX ICON PROPER NOT SHOWING BY setSmallIcon() in Android 11
//            .setSmallIcon(R.mipmap.icon)


        return notifyBuilder
    }


    companion object {
        private const val PRIMARY_CHANNEL_ID = "primary_notification_channel"
        private const val ACTION_UPDATE_NOTIFICATION =
            "com.projects.musicplayer.ACTION_MUSIC_NOTIFICATION"
        const val NOTIFICATION_ID = 0
        const val ACTION_PREVIOUS = "com.projects.musicplayer.ACTION_PREVIOUS"
        const val ACTION_PLAY_PAUSE = "com.projects.musicplayer.ACTION_PLAY"
        const val ACTION_NEXT = "com.projects.musicplayer.ACTION_NEXT"
    }


    //playable interface methods implementations
    override fun onTrackPrevious() {

        Log.i("RECEIVER-callback", "onTrackPrevious() called")


        val currSong = mMediaControlViewModel.nowPlayingSong.value
        val currSongQueue = mMediaControlViewModel.nowPlayingSongs.value
        val currSongPosition = currSongQueue?.indexOf(currSong)
        val maxSongPosition = (currSongQueue?.size)?.minus(1)
        val repeatState = mMediaControlViewModel.repeatMode.value
        if (currSongPosition != null && repeatState != null) {
            when (repeatState) {
                RepeatTriStateButton.NO_REPEAT -> {
                    if (currSongPosition == 0) {
                        // move to the first song again
                        runBlocking {
                            mMediaControlViewModel.nowPlayingSong.value = currSongQueue[0]
                        }
                    } else {
                        // Move to prev song
                        mMediaControlViewModel.nowPlayingSong.value =
                            currSongQueue[currSongPosition - 1]
                    }
                }
                RepeatTriStateButton.REPEAT_ONE -> {
                    // Play Again
                    mMediaControlViewModel.nowPlayingSong.value =
                        currSongQueue[currSongPosition]
                }
                RepeatTriStateButton.REPEAT_ALL -> {
                    if (currSongPosition == 0) {
                        // Start last song
                        mMediaControlViewModel.nowPlayingSong.value =
                            currSongQueue[maxSongPosition!!]
                    } else {
                        // Move to prev song
                        mMediaControlViewModel.nowPlayingSong.value =
                            currSongQueue[currSongPosition - 1]
                    }
                }
                else -> {
                    Log.e("NEXTPREV", "repeatState invalid")
                }
            }
        } else {
            Log.e("NEXTPREV", "currSongPosition or repeatState is null")
        }
    }

    override fun onTrackPlayPause() {
        Log.i("RECEIVER-callback", "onTrackPlayPause() called")
        uiscope.launch {
            mMediaControlViewModel.isPlaying.value = !mMediaControlViewModel.isPlaying.value!!
        }
    }


    override fun onTrackNext() {
        Log.i("RECEIVER-callback", "onTrackNext() called")
        // play next song in list
        val currSong = mMediaControlViewModel.nowPlayingSong.value
        val currSongQueue = mMediaControlViewModel.nowPlayingSongs.value
        val currSongPosition = currSongQueue?.indexOf(currSong)
        val maxSongPosition = (currSongQueue?.size)?.minus(1)
        val repeatState = mMediaControlViewModel.repeatMode.value
        if (currSongPosition != null && repeatState != null) {
            when (repeatState) {
                RepeatTriStateButton.NO_REPEAT -> {
                    if (currSongPosition == maxSongPosition) {
                        // move to the first song and pause
                        runBlocking {
                            mMediaControlViewModel.isPlaying.value = true
                        }
                        runBlocking {
                            mMediaControlViewModel.nowPlayingSong.value = currSongQueue[0]
                        }
                        uiscope.launch {
                            mMediaControlViewModel.isPlaying.value = false
                        }
                    } else {
                        // Move to next song
                        mMediaControlViewModel.nowPlayingSong.value =
                            currSongQueue[currSongPosition + 1]
                    }
                }
                RepeatTriStateButton.REPEAT_ONE -> {
                    // Play Again
                    mMediaControlViewModel.nowPlayingSong.value =
                        currSongQueue[currSongPosition]
                }
                RepeatTriStateButton.REPEAT_ALL -> {
                    if (currSongPosition == maxSongPosition) {
                        // Start first song
                        mMediaControlViewModel.nowPlayingSong.value = currSongQueue[0]
                    } else {
                        // Move to next song
                        mMediaControlViewModel.nowPlayingSong.value =
                            currSongQueue[currSongPosition + 1]
                    }
                }
                else -> {
                    Log.e("NEXTPREV", "repeatState invalid")
                }
            }
        } else {
            Log.e("NEXTPREV", "currSongPosition or repeatState is null")
        }
    }

    inner class NotificationReceiver() : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            // Update the notification
            val action = intent.action
            when (action) {
                ACTION_PREVIOUS -> {
                    onTrackPrevious()
                    Log.i("RECEIVER", "ACTION_PREVIOUS RECEIVED")
                }
                ACTION_PLAY_PAUSE -> {
                    onTrackPlayPause()
                    Log.i("RECEIVER", "ACTION_PLAY_PAUSE RECEIVED")
                }
                ACTION_NEXT -> {
                    onTrackNext()
                    Log.i("RECEIVER", "ACTION_NEXT RECEIVED")
                }
            }
        }
    }

}