package com.plcoding.spotifycloneyt.exoplayer
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.plcoding.spotifycloneyt.data.entities.other.Constants.MEDIA_ROOT_ID
import com.plcoding.spotifycloneyt.exoplayer.callbacks.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

private const val SERVICE_TAG = "AudioService"

@AndroidEntryPoint
class AudioService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    @Inject
    lateinit var firebaseAudioSource: FirebaseAudioSource

    private lateinit var musicNotificationManager: AudioNotificationManager

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediaSession: MediaSessionCompat

    private lateinit var mediaSessionConnector: MediaSessionConnector

    private lateinit var audioPlayerEventListener: AudioPlayerEventListener

    var isForegroundService = false

    private var isPlayerInitialized = false

    private var curPlayingSong: MediaMetadataCompat? = null

    companion object {
        private var curAudioDuration = 0L
        private set
    }

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            firebaseAudioSource.fetchMediaData()
        }
        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }


        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken

        musicNotificationManager = AudioNotificationManager(
            this,
            mediaSession.sessionToken,
            AudioPlayerNotificationListener(this)
        ) {
            curAudioDuration = exoPlayer.duration

        }

        val musicPlaybackPreparer = MusicPlaybackPreparer(firebaseAudioSource) {
            curPlayingSong = it
            preparePlayer(
                firebaseAudioSource.audios,
                it,
                true
            )
        }

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)

        audioPlayerEventListener = AudioPlayerEventListener(this)
        exoPlayer.addListener(audioPlayerEventListener)
        musicNotificationManager.showNotification(exoPlayer)
    }
    private inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return firebaseAudioSource.audios[windowIndex].description
        }
    }

    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        val curSongIndex = if(curPlayingSong == null) 0 else songs.indexOf(itemToPlay)
        exoPlayer.prepare(firebaseAudioSource.asMediaSource(dataSourceFactory))
        exoPlayer.seekTo(curSongIndex, 0L)
        exoPlayer.playWhenReady = playNow
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        exoPlayer.removeListener(audioPlayerEventListener)
        exoPlayer.release()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
       return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when(parentId) {
            MEDIA_ROOT_ID -> {
               val resultsSent = firebaseAudioSource.whenReady { isInitialzied ->
                    if (isInitialzied) {
                        result.sendResult(firebaseAudioSource.asMediaItems())
                        if (isPlayerInitialized && firebaseAudioSource.audios.isNotEmpty()) {
                            preparePlayer(firebaseAudioSource.audios, firebaseAudioSource.audios[0], false)
                            isPlayerInitialized = true


                        }
                    }else   {
                        result.sendResult(null)
                    }


                }
                if (!resultsSent) {
                    result.detach()
                }
            }
        }

    }
}