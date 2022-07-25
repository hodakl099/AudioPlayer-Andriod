package com.plcoding.spotifycloneyt.data.entities.other

import javax.net.ssl.SSLEngineResult

class Resource<out T>(val status:Status, val data: T?, message: String?){

    companion object {
        fun <T> success(data: T?) = Resource(Status.SUCCESS, data, null)

        fun <T> error(data: T? , message: String) = Resource(Status.ERROR, data, message)

        fun <T> loading(data: T?) = Resource(Status.LOADING,data,null)

    }
}

enum class Status {
    SUCCESS,
    LOADING,
    ERROR
}