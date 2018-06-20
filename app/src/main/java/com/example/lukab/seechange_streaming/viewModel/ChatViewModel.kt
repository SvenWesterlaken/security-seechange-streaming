package com.example.lukab.seechange_streaming.viewModel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.util.Base64
import android.util.Log
import com.example.lukab.seechange_streaming.app.util.HexConverter
import com.example.lukab.seechange_streaming.app.utils.DerParser
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.spec.RSAPrivateCrtKeySpec
import javax.crypto.Cipher

class ChatViewModel(url: String, private val username: String, private val key: PrivateKey?) {
    private var socket: Socket = IO.socket(url)
    private val hash: String = hash()


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
        val hexByteArray = HexConverter.bytesToHex(hash).toByteArray()

        val cipher = Cipher.getInstance("AES/ECB/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encrypted = cipher.doFinal(hexByteArray)

        return Base64.encodeToString(encrypted, Base64.DEFAULT)
    }

}