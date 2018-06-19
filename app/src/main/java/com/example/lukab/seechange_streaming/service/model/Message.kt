package com.example.lukab.seechange_streaming.service.model

import java.util.*

class Message(val fromStreamer: Boolean, val username: String, val message: String) {
    private val timestamp: String = Calendar.getInstance().time.toString()
}