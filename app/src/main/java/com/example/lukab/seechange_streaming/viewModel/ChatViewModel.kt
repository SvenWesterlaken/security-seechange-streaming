package com.example.lukab.seechange_streaming.viewModel

import android.util.Base64
import com.example.lukab.seechange_streaming.app.util.HexConverter
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.interfaces.RSAPrivateKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class ChatViewModel(url: String, private val username: String, key: String) {
    private var socket: Socket = IO.socket(url)
    private var key: RSAPrivateKey = stringToKey(key)
    private var hash: String = encryptedHash()


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

    private fun encryptedHash(): String {
        val hash = MessageDigest.getInstance("SHA-256").digest(username.toByteArray())
        val cipher = Cipher.getInstance("AES/ECB/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return Base64.encodeToString(cipher.doFinal(HexConverter.bytesToHex(hash).toByteArray()), Base64.DEFAULT)
    }

    @Throws(GeneralSecurityException::class)
    private fun stringToKey(privateKey: String): RSAPrivateKey {
        val privateKeyPEM = privateKey.replace("-----BEGIN RSA PRIVATE KEY-----", "").replace("-----END RSA PRIVATE KEY-----", "")
        val encoded = Base64.decode(privateKeyPEM, Base64.NO_PADDING)
        val kf = KeyFactory.getInstance("RSA")
        return kf.generatePrivate(X509EncodedKeySpec(encoded)) as RSAPrivateKey
    }

}