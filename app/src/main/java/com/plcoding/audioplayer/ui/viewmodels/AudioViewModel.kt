package com.plcoding.audioplayer.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.plcoding.audioplayer.exoplayer.AudioServiceConnection

class AudioViewModel @ViewModelInject constructor(
    audioServiceConnection: AudioServiceConnection
) : ViewModel(){
}