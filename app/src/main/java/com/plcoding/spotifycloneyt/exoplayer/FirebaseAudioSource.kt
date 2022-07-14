package com.plcoding.spotifycloneyt.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.plcoding.spotifycloneyt.data.entities.remote.AudioDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


//FireBase
class FirebaseAudioSource @Inject constructor(private val audioDatabase: AudioDatabase) {



    var audios = emptyList<MediaMetadataCompat>()

    suspend fun fetchMediaData() = withContext(Dispatchers.IO) {
        state = State.STATE_INITIALIZING
        val allAudios = audioDatabase.getAllAudio()
        audios = allAudios.map { audio ->
            MediaMetadataCompat.Builder()
                .putString(METADATA_KEY_ARTIST, audio.subtitle)
                .putString(METADATA_KEY_MEDIA_ID, audio.MediaId)
                .putString(METADATA_KEY_TITLE, audio.title)
                .putString(METADATA_KEY_DISPLAY_TITLE, audio.title)
                .putString(METADATA_KEY_DISPLAY_ICON_URI, audio.imageUrl)
                .putString(METADATA_KEY_MEDIA_URI, audio.audioUrl)
                .putString(METADATA_KEY_ALBUM_ART_URI, audio.imageUrl)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE, audio.subtitle)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION, audio.subtitle)
                .build()
        }
        state = State.STATE_INITIALIZED
    }

    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        audios.forEach { audio ->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(audio.getString(METADATA_KEY_MEDIA_URI).toUri())
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    fun asMediaItems() = audios.map { audio ->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(audio.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(audio.description.title)
            .setSubtitle(audio.description.subtitle)
            .setMediaId(audio.description.mediaId)
            .setIconUri(audio.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
    }


    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var state: State = State.STATE_CREATED
        set(value) {
            if(value == State.STATE_INITIALIZED || value == State.STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == State.STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }


    fun whenReady(action: (Boolean) -> Unit): Boolean {
        if(state == State.STATE_CREATED || state == State.STATE_INITIALIZING) {
            onReadyListeners += action
            return false
        } else {
            action(state == State.STATE_INITIALIZED)
            return true
        }
    }
}

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}