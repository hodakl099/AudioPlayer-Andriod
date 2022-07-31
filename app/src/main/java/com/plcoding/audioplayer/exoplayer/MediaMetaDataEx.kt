package com.plcoding.audioplayer.exoplayer

import android.support.v4.media.MediaMetadataCompat
import com.plcoding.audioplayer.data.entities.Audio


fun MediaMetadataCompat.toAudio(): Audio? {
    return description?.let {
        Audio(
            it.mediaId ?: "",
            it.title.toString(),
            it.subtitle.toString(),
            it.mediaUri.toString(),
            it.iconUri.toString()
        )
    }
}