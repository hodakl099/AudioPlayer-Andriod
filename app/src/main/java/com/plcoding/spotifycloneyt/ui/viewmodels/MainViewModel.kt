package com.plcoding.spotifycloneyt.ui.viewmodels
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.plcoding.spotifycloneyt.data.entities.Audio
import com.plcoding.spotifycloneyt.data.entities.other.Constants.MEDIA_ROOT_ID
import com.plcoding.spotifycloneyt.data.entities.other.Resource
import com.plcoding.spotifycloneyt.exoplayer.*


class MainViewModel @ViewModelInject constructor(
    private val audioServiceConnection: AudioServiceConnection
) : ViewModel() {
    private val _mediaItems = MutableLiveData<Resource<List<Audio>>>()
    val mediaItems: LiveData<Resource<List<Audio>>> = _mediaItems

    val isConnected = audioServiceConnection.isConnected
    val networkError = audioServiceConnection.networkError
    val curPlayingAudio = audioServiceConnection.curPlayingSong
    val playbackState = audioServiceConnection.playbackState

    init {
        _mediaItems.postValue(Resource.loading(null))
        audioServiceConnection.subscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback() {
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                val items = children.map {
                    Audio(
                        it.mediaId!!,
                        it.description.title.toString(),
                        it.description.subtitle.toString(),
                        it.description.mediaUri.toString(),
                        it.description.iconUri.toString()
                    )
                }
                _mediaItems.postValue(Resource.success(items))
            }
        })
    }

    fun skipToNextSong() {
        audioServiceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousSong() {
        audioServiceConnection.transportControls.skipToPrevious()
    }

    fun seekTo(pos: Long) {
        audioServiceConnection.transportControls.seekTo(pos)
    }

    fun playOrToggleSong(mediaItem: Audio, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false
        if(isPrepared && mediaItem.mediaId ==
            curPlayingAudio.value?.getString(METADATA_KEY_MEDIA_ID)) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if(toggle) audioServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> audioServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            audioServiceConnection.transportControls.playFromMediaId(mediaItem.mediaId, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioServiceConnection.unsubscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback() {})
    }
}




