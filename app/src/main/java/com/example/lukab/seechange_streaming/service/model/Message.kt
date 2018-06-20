package com.example.lukab.seechange_streaming.service.model

import java.text.SimpleDateFormat
import java.util.*

class Message(val fromStreamer: Boolean, val username: String, val message: String, timestamp: Long, hash: String) {
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.US)
    var timestamp: String = dateFormat.format(timestamp)

    fun isValid(): Boolean {
        return true
        //Integrity Check
    }
}