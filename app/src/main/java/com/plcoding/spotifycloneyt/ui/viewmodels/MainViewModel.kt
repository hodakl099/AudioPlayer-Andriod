package com.plcoding.spotifycloneyt.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import com.plcoding.spotifycloneyt.exoplayer.AudioServiceConnection

class MainViewModel @ViewModelInject constructor (
    private val audioServiceConnection: AudioServiceConnection
        ) {
}