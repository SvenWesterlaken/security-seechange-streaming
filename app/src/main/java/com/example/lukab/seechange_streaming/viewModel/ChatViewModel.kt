package com.example.lukab.seechange_streaming.viewModel

import android.app.Activity
import android.util.Base64
import com.example.lukab.seechange_streaming.app.util.HexConverter
import com.example.lukab.seechange_streaming.viewModel.LoginViewModel.toByte
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import java.net.URISyntaxException
import java.security.MessageDigest
import java.security.PrivateKey
import javax.crypto.Cipher
import kotlin.experimental.and

class ChatViewModel(url: String, private val username: String, private val key: String) {
    private var socket: Socket = IO.socket(url)

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
        socket.emit("chat message", username, username, message, hash(username), System.currentTimeMillis())
    }

    fun authenticate(token: String, successListener: Emitter.Listener) {
        socket.on("authenticate", successListener)
        socket.emit("authenticate", username, hash(), token)
    }

    fun subscribe() {
        socket.emit("subscribe", username, hash())
    }

    fun unsubscribe() {
        socket.emit("unsubscribe", username, hash())
    }

    fun addMessageListener(messageListener: Emitter.Listener) {
        socket.on("chat message", messageListener)
    }

    fun addErrorListener(errorListener: Emitter.Listener) {
        socket.on("error", errorListener)
    }

    private fun hash(): String {
        val hash = MessageDigest.getInstance("SHA-256").digest(username.toByteArray())
        HexConverter.bytesToHex(hash)


        val cipher = Cipher.getInstance("AES/ECB/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key as PrivateKey)
        val clearbyte = cipher.doFinal()

        return Base64.encodeToString(hash, Base64.DEFAULT)






    }

}