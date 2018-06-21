package com.example.lukab.seechange_streaming.viewModel

import android.util.Base64
import android.util.Log
import com.example.lukab.seechange_streaming.app.util.HexConverter
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import net.ossrs.rtmp.Security
import java.io.IOException
import java.security.MessageDigest

class ChatViewModel(url: String, private val username: String) {
    private var socket: Socket
    private val hash: String = hash()

    init {

        try {
            this.socket = IO.socket(url)
        } catch (e: Exception) {
            throw e
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

        Log.d("Chat", HexConverter.bytesToHex(hash))

        val hexByteArray = Security.hexStringToByteArray(HexConverter.bytesToHex(hash))

        return Base64.encodeToString(Security.EncryptData(hexByteArray), Base64.NO_PADDING).toLowerCase()
    }

}