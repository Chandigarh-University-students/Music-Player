package com.projects.musicplayer.activity

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.projects.musicplayer.R
import com.projects.musicplayer.database.SongEntity
import com.projects.musicplayer.fragments.*
import com.projects.musicplayer.rest.Song
import com.projects.musicplayer.fragments.FavFragment
import com.projects.musicplayer.fragments.SinglePlaylistFragment
import com.projects.musicplayer.uicomponents.RepeatTriStateButton
import com.projects.musicplayer.utils.Utility
import com.projects.musicplayer.viewmodel.AllSongsViewModel
import com.projects.musicplayer.viewmodel.AllSongsViewModelFactory
import com.projects.musicplayer.viewmodel.MediaControlViewModel
import com.projects.musicplayer.viewmodel.MediaControlViewModelFactory
import kotlinx.coroutines.*
import java.lang.Long.parseLong
import java.lang.Runnable
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var mBottomSheetBehavior: BottomSheetBehavior<LinearLayoutCompat>
    lateinit var flFragment: FrameLayout
    lateinit var bottomSheet: LinearLayoutCompat

    lateinit var b_sheet_Collapsed: LinearLayout
    lateinit var b_sheet_Expanded: RelativeLayout
    lateinit var sharedPreferences: SharedPreferences


    //testing
//    lateinit var testState: TextView
//    lateinit var musicCoverPic:ImageView
    /**Now Playing Controls*/

    /*EXPANDED BOTTOM SHEET ELEMENTS*/
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
    lateinit var songNowPlaying: CardView
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

    private lateinit var mMediaControlViewModel: MediaControlViewModel
    private lateinit var mMediaControlViewModelFactory: MediaControlViewModelFactory

    lateinit var mediaPlayer: MediaPlayer
    lateinit var runnable: Runnable

    val MediaPlayer.seconds: Int
        get() = this.duration / 1000
    val MediaPlayer.currentSeconds: Int
        get() = this.currentPosition / 1000
    var homeFragment: HomeFragment = HomeFragment()
    var handler: Handler = Handler()


    //coroutine scopes
    val uiscope = CoroutineScope(Dispatchers.Main)
    val dbScope = CoroutineScope(Dispatchers.IO)

    /*EXPANDED BOTTOM SHEET ELEMENTS*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        songNowPlaying = findViewById(R.id.songNowPlaying)
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


        sharedPreferences = getSharedPreferences(
            "Audio DB Preferences",
            Context.MODE_PRIVATE
        )

        //init View Model obejct
        //use of view model factory to pass parameter to view model
        mAllSongsViewModelFactory = AllSongsViewModelFactory(application)
        mAllSongsViewModel =
            ViewModelProvider(this, mAllSongsViewModelFactory).get(AllSongsViewModel::class.java)
        mMediaControlViewModel =
            ViewModelProvider(this).get(MediaControlViewModel::class.java)

        mAllSongsViewModel.allSongs.observe(this, Observer {
            var currentQueue = mMediaControlViewModel.nowPlayingSongs.value
            var updatedCurrentQueue = mutableListOf<SongEntity>()
            Log.d("FavInQueue", mMediaControlViewModel.nowPlayingSongs.value.toString())
            Log.d("FavInQueue", it.toString())
            if (currentQueue != null) {
                for (song in currentQueue) {
                    val songComplement = SongEntity(
                        song.songId,
                        song.songName,
                        song.artistName,
                        song.duration,
                        song.albumCover,
                        song.isFav * (-1)
                    )
                    if (song in it)
                        updatedCurrentQueue.add(song)
                    else if (songComplement in it)
                        updatedCurrentQueue.add(songComplement)
                    else {
                        Log.d("FavInQueue", "Weird behaviour")
                        updatedCurrentQueue.add(song)
                    }
                }
                mMediaControlViewModel.nowPlayingSongs.value = updatedCurrentQueue
                Log.d("FavInQueue", "Live data for nowPlayingSongs is updated due to change in fav")
            } else {
                Log.d("FavInQueue", "Doing nothing since no songs are being played")
            }
        })

        mMediaControlViewModel.isPlaying.observe(this, Observer {
            Log.i("PLAYBACK STATUS", it.toString())
            btnPlayPauseControl.isChecked = it
            b_sheet_CollapsedMusicControl.isChecked = it
//            }
            playPauseMedia(it)
        })

        mMediaControlViewModel.isShuffleMode.observe(this, Observer {
            if (it) {
                val currentList = mMediaControlViewModel.nowPlayingSongs.value
                mMediaControlViewModel.nowPlayingSongs.value = currentList!!.shuffled()
            }
        })

        mMediaControlViewModel.nowPlayingSong.observe(this, Observer {
            Log.i("PLAYLISTSONG", "New Song Clicked ${it.songName}")
            setUpMediaPlayer(it, !mMediaControlViewModel.isFirstInit.value!!)
            initializeSeekbar()
            uiscope.launch {
                setUpCollapsedBottomSheetUI(it)
            }
            uiscope.launch {
                setUpExpandedBottomSheetUI(it)
            }
        })

        mMediaControlViewModel.nowPlayingSongs.observe(this, Observer {
            Log.i("PLAYLIST", "New playlist added ${it.toString()}")
            //TODO to use this playlist to switch to next songs
        })

        mMediaControlViewModel.nowPlaylist.observe(this, Observer {
            Log.i("PLAYLISTNAME", "New playlist added ${it}")
            //TODO to use this playlist name to display
        })


        setUpBottomSheet()


        //TODO: CHECK SYNC AUDIO FETCH AND LOADING OF HOME_FRAGMENT


        if (!isDatabaseInitialized() || !permissionGranted()) {
            Toast.makeText(
                this,
                "Fetching Songs from MediaStore for first time",
                Toast.LENGTH_SHORT
            ).show()
            if (!permissionGranted())
                setupPermissions()
            getAudioFiles()
        }
        initUI()
//        homeFragment.onPlaySongClickCallback = fun(songEntity: SongEntity) {
//            if (this::mediaPlayer.isInitialized) {
//                mediaPlayer.stop()
//                mediaPlayer.reset()
//                mediaPlayer.release()
//            }
//            val songUri = Uri.parse(songEntity.albumCover)
//            mediaPlayer = MediaPlayer.create(applicationContext, songUri)
//            mediaPlayer.start()

//        }

        setUpBottomNav()

        setUpExpandedNowPlaying()

        //initially load  testing playlist fragment
        /*supportFragmentManager.beginTransaction()
                         .replace(
                            R.id.frame, PlaylistsFragment()
                         ).commit()*/


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

    fun setUpMediaPlayer(songEntity: SongEntity, toPlay: Boolean = true) {
        clearMediaPlayer()
        val songUri = Uri.parse(songEntity.albumCover)
        mediaPlayer = MediaPlayer.create(applicationContext, songUri)
//        mediaPlayer.start()
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
                            //TODO move to the first song and pause
                            runBlocking {
                                mMediaControlViewModel.nowPlayingSong.value=currSongQueue[0]
                            }
                            uiscope.launch {
                                mMediaControlViewModel.isPlaying.value = false
                            }

                        }else{
                            //TODO Move to next song
                            mMediaControlViewModel.nowPlayingSong.value= currSongQueue[currSongPosition+1]
                        }
                    }
                    RepeatTriStateButton.REPEAT_ONE -> {
                        //TODO Play Again
                        mMediaControlViewModel.nowPlayingSong.value= currSongQueue[currSongPosition]
                    }
                    RepeatTriStateButton.REPEAT_ALL -> {
                        if(currSongPosition==maxSongPosition){
                            //TODO Start first song
                            mMediaControlViewModel.nowPlayingSong.value=currSongQueue[0]
                        }else{
                            //TODO Move to next song
                            mMediaControlViewModel.nowPlayingSong.value= currSongQueue[currSongPosition+1]
                        }
                    }
                    else -> {Log.e("NEXTPREV","repeatState invalid")}
                }
            }else{
                Log.e("NEXTPREV","currSongPosition or repeatState is null")
            }

        }
}

    fun initializeSeekbar() {

        controlSeekBar.max = mediaPlayer.seconds
        txtTotalDuration.text = getDuration(mediaPlayer.seconds.toLong())
        runnable = Runnable {
            controlSeekBar.progress = mediaPlayer.currentSeconds
            txtCurrentDuration.text = getDuration(mediaPlayer.currentSeconds.toLong())
            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)
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
            val image = Utility.getAlbumCover(songEntity.albumCover)
            if (image != null) {
                b_sheet_CollapsedMusicCover.setImageBitmap(
                    BitmapFactory.decodeByteArray(
                        image,
                        0,
                        image.size
                    )
                )
            } else {
                b_sheet_CollapsedMusicCover.setImageResource(R.drawable.drawable_cover)
            }
        }
    }


    private fun playPauseMedia(play: Boolean) {
        val pause = !play
        if (this::mediaPlayer.isInitialized) {

            Toast.makeText(this, "Play $play", Toast.LENGTH_SHORT).show()
            if ( pause) {
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
            val image = Utility.getAlbumCover(songEntity.albumCover)
            if (image != null) {
                musicCoverPic.setImageBitmap(
                    BitmapFactory.decodeByteArray(
                        image,
                        0,
                        image.size
                    )
                )
            } else {
                musicCoverPic.setImageResource(R.drawable.drawable_cover)
            }
        }

    }


    fun setUpExpandedNowPlaying() {
        btnMinimizeToolbar.setOnClickListener {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            if (bottomNavigationView.selectedItemId == R.id.nowPlaying)
                bottomNavigationView.selectedItemId =
                    R.id.home_button
        }

        btnSongList.setOnClickListener {
            //TODO testing how to collapse now playing and show queue

            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.frame,
                    SongsQueueFragment()
                ).commit()

            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
           }

        btnNextControl.setOnClickListener {
            //TODO play next song in list
            Log.i("NEXTPREV",mMediaControlViewModel.nowPlayingSong.value.toString())
            if(mMediaControlViewModel.nowPlayingSong.value!=null){
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
                                //TODO move to the first song and pause
                                runBlocking {
                                    mMediaControlViewModel.nowPlayingSong.value=currSongQueue[0]
                                }
                                uiscope.launch {
                                    mMediaControlViewModel.isPlaying.value = false
                                }

                            }else{
                                //TODO Move to next song
                                mMediaControlViewModel.nowPlayingSong.value= currSongQueue[currSongPosition+1]
                            }
                        }
                        RepeatTriStateButton.REPEAT_ONE -> {
                                //TODO Play Again
                            mMediaControlViewModel.nowPlayingSong.value= currSongQueue[currSongPosition]
                        }
                        RepeatTriStateButton.REPEAT_ALL -> {
                            if(currSongPosition==maxSongPosition){
                                //TODO Start first song
                                mMediaControlViewModel.nowPlayingSong.value=currSongQueue[0]
                            }else{
                                //TODO Move to next song
                                mMediaControlViewModel.nowPlayingSong.value= currSongQueue[currSongPosition+1]
                            }
                        }
                        else -> {Log.e("NEXTPREV","repeatState invalid")}
                    }
                }else{
                    Log.e("NEXTPREV","currSongPosition or repeatState is null")
                }

            }else{
                Toast.makeText(this,"No song is playing",Toast.LENGTH_SHORT).show()
            }
        }

        btnPrevControl.setOnClickListener {
            //TODO play prev song
            Log.i("NEXTPREV",mMediaControlViewModel.nowPlayingSong.value.toString())
            if(mMediaControlViewModel.nowPlayingSong.value!=null){
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
                                //TODO move to the first song again
                                runBlocking {
                                    mMediaControlViewModel.nowPlayingSong.value=currSongQueue[0]
                                }
                            }else{
                                //TODO Move to prev song
                                mMediaControlViewModel.nowPlayingSong.value= currSongQueue[currSongPosition-1]
                            }
                        }
                        RepeatTriStateButton.REPEAT_ONE -> {
                            //TODO Play Again
                            mMediaControlViewModel.nowPlayingSong.value= currSongQueue[currSongPosition]
                        }
                        RepeatTriStateButton.REPEAT_ALL -> {
                            if(currSongPosition==0){
                                //TODO Start last song
                                mMediaControlViewModel.nowPlayingSong.value=currSongQueue[maxSongPosition!!]
                            }else{
                                //TODO Move to prev song
                                mMediaControlViewModel.nowPlayingSong.value= currSongQueue[currSongPosition-1]
                            }
                        }
                        else -> {Log.e("NEXTPREV","repeatState invalid")}
                    }
                }else{
                    Log.e("NEXTPREV","currSongPosition or repeatState is null")
                }

            }else{
                Toast.makeText(this,"No song is playing",Toast.LENGTH_SHORT).show()
            }
        }


//        controlSeekBar.max = 50
//        txtCurrentDuration.text = controlSeekBar.progress.toString()
//        txtTotalDuration.text = controlSeekBar.max.toString()
        /*controlSeekBar.max = 50
        txtCurrentDuration.text = controlSeekBar.progress.toString()
        txtTotalDuration.text = controlSeekBar.max.toString()*/


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
//                            testState.text = "NO_REPEAT"
                        }
                        RepeatTriStateButton.REPEAT_ALL -> {
                            Toast.makeText(
                                this@MainActivity,
                                "REPEAT_ALL",
                                Toast.LENGTH_SHORT
                            ).show()
//                            testState.text = "REPEAT_ALL"
                        }
                        RepeatTriStateButton.REPEAT_ONE -> {
                            Toast.makeText(
                                this@MainActivity,
                                "REPEAT_ONE",
                                Toast.LENGTH_SHORT
                            ).show()
//                            testState.text = "REPEAT_ONE"
                        }
                        else -> println("DEFAULT STATE")
                    }
                }

            }
        )

    }

    private suspend fun updateMediaPlayer(mUri: Uri) {
////    private fun updateMediaPlayer(songPath: String) {
//        Log.d("Setting URI", "Inside Update Media Player")
//
//        withContext(Dispatchers.IO) {
//            mediaPlayer?.apply {
//                setDataSource(applicationContext, mUri)
//                prepare()
//                setVolume(0.5f, 0.5f)
//                isLooping = false
//                start()
//            }
//        }

//        mediaPlayer?.setDataSource(songPath)
//        mediaPlayer?.prepare()
//        mediaPlayer?.setVolume(0.5f,0.5f)
//        mediaPlayer?.isLooping = false
//        mediaPlayer?.start()
//            mediaPlayer.apply {
//                setDataSource(applicationContext, mUri)
//                prepare()
//                start()
//            }
    }

    fun initUI() {
        //initially load home_fragment into frame layout...

        bottomNavigationView.selectedItemId =
            R.id.home_button

        supportFragmentManager.beginTransaction()
            .replace(
                R.id.frame,
                homeFragment
//                HomeFragment()
            ).commit()
        //set initial state of bottom sheet
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun clearMediaPlayer() {
        if (this::mediaPlayer.isInitialized) {
            mediaPlayer.stop()
            mediaPlayer.reset()
            mediaPlayer.release()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clearMediaPlayer()
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
                    //TODO : REPLACE WITH PLAYLIST FRAGMENT
                    //testing layout using HomeFragment()
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
                        Toast.makeText(
                            this@MainActivity,
                            "STATE_COLLAPSED",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        //make bottom nav hidden when bottom sheet expanded
                        bottomNavigationView.visibility = View.GONE
                        b_sheet_Collapsed.visibility = View.GONE
                        b_sheet_Expanded.visibility = View.VISIBLE
                        Toast.makeText(
                            this@MainActivity,
                            "STATE_EXPANDED",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        Toast.makeText(
                            this@MainActivity,
                            "STATE_DRAGGING",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                        Toast.makeText(
                            this@MainActivity,
                            "STATE_SETTLING",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        Toast.makeText(
                            this@MainActivity,
                            "STATE_HIDDEN",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> {
                        Toast.makeText(
                            this@MainActivity,
                            "SOME OTHER STATE",
                            Toast.LENGTH_SHORT
                        ).show()
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

            //if current selected tab is now playing then set current selected tab to home tab
            if (bottomNavigationView.selectedItemId == R.id.nowPlaying)
                bottomNavigationView.selectedItemId =
                    R.id.home_button

        } else
            when (supportFragmentManager.findFragmentById(R.id.frame)) {
                is SinglePlaylistFragment -> openPlaylistFrag()
                is FavFragment -> openPlaylistFrag()
                !is HomeFragment -> initUI()
                else -> super.onBackPressed()
            }
    }

    //    fun permissionGranted(): Boolean {
    private fun openPlaylistFrag() {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.frame,
                PlaylistsFragment()
            ).commit()
    }

    fun permissionGranted(): Boolean {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        return (permission == PackageManager.PERMISSION_GRANTED)
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied")
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            READ_STORAGE_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == READ_STORAGE_PERMISSION_REQUEST_CODE) {
            for (i in permissions.indices) {
                val permission: String = permissions[i];
                val grantResult: Int = grantResults[i];

                if (permission == Manifest.permission.READ_EXTERNAL_STORAGE) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        getAudioFiles()
                    }
                }
            }
        }

    }

    private fun getAudioFiles() {

        val songs = mutableListOf<SongEntity>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        // IS_MUSIC : Non-zero if the audio file is music
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"

        // Sort the musics
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        //val sortOrder = MediaStore.Audio.Media.TITLE + " DESC

        val contentResolver1 = ContextWrapper(applicationContext).contentResolver

        val cursor = contentResolver1!!.query(
            uri,
            null,
            selection,
            null,
            sortOrder
        )

        //looping through all rows and adding to list
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val songName =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                val artistName =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                val duration =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                val url =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))//.replace("\","\\\"")
                //val url = File(resolver.openFileDescriptor(uri, "r"))

                val songEntity =
                    SongEntity(
                        songs.size + 1,
                        songName,
                        artistName,
                        parseLong(duration),
                        url,
                        -1
                    )
                Log.i("FetchCheck", songEntity.toString())
                songs.add(songEntity)


                //insert song one by one
                //WORKING!!!
//                mAllSongsViewModel.insertSong(songEntity)


            } while (cursor.moveToNext())
        }
        //TODO - To send songs list to DB
        //TODO set shared preferences for isLoaded
        //fetch and initialize db with all songs at once
        //WORKING!!! TO INSERT MULTIPLE SONGS AT ONCE
        mAllSongsViewModel.insertSongs(songs)
        //finally set shared pref
        sharedPreferences.edit().putBoolean("songLoaded", true).apply()
    }

    private fun isDatabaseInitialized(): Boolean =
        sharedPreferences.getBoolean("songLoaded", false)
}