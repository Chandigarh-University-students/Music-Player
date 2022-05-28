# Sangeet <a href="https://apt.izzysoft.de/fdroid/repo/com.projects.musicplayer_1.apk" target="blank"> <img src="https://img.shields.io/badge/F_Droid-1976D2?style=plastic&logo=f-droid&logoColor=white"  title="Download apk"> </a>
A Music Player App to play songs from local storage, built on MVVM architecture with repository pattern and livedata. Users can play songs, keep track of recent songs, mark their favorite songs, create their own playlists and also shuffle & repeat songs.

***

<h3>Screenshots</h3>

<div class="row">
      <img src="/screenshots/4.jpeg" width="250" title="Home Tab">
      <img src="/screenshots/7.jpeg" width="250" title="Now Playing Tab">     
      <img src="/screenshots/8.jpeg" width="250" title="Play Queue">
</div>


<div class="row">
      <img src="/screenshots/10.jpeg" width="250" title="Create Playlist">
      <img src="/screenshots/5.jpeg" width="250" title="Add To Playlist">     
      <img src="/screenshots/6.jpeg" width="250" title="Chose Playlist">
</div>


<div class="row">
      <img src="/screenshots/11.jpeg" width="250" title="Playlist Tab">
      <img src="/screenshots/14.jpeg" width="250" title="Single Playlist">     
      <img src="/screenshots/15.jpeg" width="250" title="Remove From Playlist">
</div>


<div class="row">
      <img src="/screenshots/11.jpeg" width="250" title="Playlist Tab">
      <img src="/screenshots/12.jpeg" width="250" title="Empty Favorite">     
      <img src="/screenshots/13.jpeg" width="250" title="Full Fav">
</div>

## It makes use of following:-
- **LiveData & MutableLiveData** for updating and listening to realtime datachanges in app. Also livedata can survive configuration changes.
- **ViewModel** with **ViewModelFactory** to acess livedata
- **Repository** as **single source of truth** to make Database operations
- **Coroutines** to perform databse operations in background thread without blocking main thread.
- Various Coroutine Context based on type of operation: e.g Dispatchers.Main for updaing UI in main htread and Dispatchers.IO to work in background thread.
- Also, database dao methods are defined suspend fun methods so that they run in background thread. This work is done internally by Room library automatically.
- Also, LiveData in ViewModel are update in viewModelScope, a Coroutine scope provided for livedata.

- **ConstraintLayout** for maki device compatible ui in screen.
- **BottomSheet** a material design ui component for opening a bottomsheet
- **BottomNavigationView** another material design ui component for changing tabs

***
<p> <img src="https://developer.android.com/codelabs/android-training-livedata-viewmodel/img/fd28069527c8d615.png"> </p>
