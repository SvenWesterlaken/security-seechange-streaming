package com.example.lukab.seechange_streaming.viewModel

import com.example.lukab.seechange_streaming.app.util.HexConverter
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import net.ossrs.rtmp.Security
import java.security.MessageDigest

class ChatViewModel(url: String, private val username: String) {
    private lateinit var socket: Socket
    private val hash: String = hash()

    init {

        try {
            this.socket = IO.socket(url)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun connect(): Socket {
        return this.socket.connect()
    }

    fun connected(): Boolean {
        return this.socket.connected()
    }

    fun close(): Socket {
        return this.socket.close()
    }

    fun sendMessage(message: String) {
        socket.emit("chat message", username, username, message, hash, System.currentTimeMillis())
    }

    fun authenticate(token: String, successListener: Emitter.Listener) {
        socket.on("authenticate", successListener)
        socket.emit("authenticate", username, hash, token)
    }

    fun subscribe() {
        socket.emit("subscribe", username, hash)
    }

    fun unsubscribe() {
        socket.emit("unsubscribe", username, hash)
    }

    fun addMessageListener(messageListener: Emitter.Listener) {
        socket.on("chat message", messageListener)
    }

    fun addErrorListener(errorListener: Emitter.Listener) {
        socket.on("error", errorListener)
    }

    fun destroy() {
        socket.off()
        close()
    }

    fun isStreamer(username: String): Boolean {
        return username.toLowerCase() == this.username.toLowerCase()
    }

    private fun hash(): String {
        val hash = MessageDigest.getInstance("SHA-256").digest(username.toByteArray())

        return HexConverter.bytesToHex(hash).toLowerCase()
    }

}