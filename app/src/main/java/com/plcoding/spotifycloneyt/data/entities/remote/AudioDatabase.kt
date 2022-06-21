package com.plcoding.spotifycloneyt.data.entities.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.plcoding.spotifycloneyt.data.entities.Audio
import com.plcoding.spotifycloneyt.data.entities.other.Constants.AUDIO_COLLECION
import kotlinx.coroutines.tasks.await

class AudioDatabase {


    private val fireStore = FirebaseFirestore.getInstance()
    private val audioCollection = fireStore.collection(AUDIO_COLLECION)

    suspend fun getAllAudio(): List<Audio> {
        return try {
            audioCollection.get().await().toObjects(Audio::class.java)
        } catch (e: Exception){
            emptyList()
        }
    }



}