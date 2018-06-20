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

class ChatViewModel(url: String, private val username: String, key: String, application: Application): AndroidViewModel(application) {
    private var socket: Socket = IO.socket(url)
    private val hash: String = hash()
    private val key: PrivateKey? = getPrivateKeyFromString(key)


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

        val cipher = Cipher.getInstance("AES/ECB/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encrypted = cipher.doFinal(HexConverter.bytesToHex(hash).toByteArray())

        return Base64.encodeToString(encrypted, Base64.DEFAULT)
    }

    @Throws(GeneralSecurityException::class)
    private fun getPrivateKeyFromString(privateKey: String): PrivateKey? {
        var privateKeyPEM = privateKey

        privateKeyPEM = privateKeyPEM.replace("\\n".toRegex(), "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
        Log.d("LoginViewModel: ", "privatePem without begin and end: $privateKeyPEM")

        val encoded = Base64.decode(privateKeyPEM, Base64.DEFAULT)

        //		PKCS8EncodedKeySpec keySpecPKCS8 = new EncodedKeySpec();
        val kf = KeyFactory.getInstance("RSA")
        var rsaKeySpec: RSAPrivateCrtKeySpec? = null

        try {
            rsaKeySpec = getRSAKeySpec(encoded)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return if (rsaKeySpec != null) {
            kf.generatePrivate(rsaKeySpec)
        } else {
            null
        }
    }

    @Throws(IOException::class)
    private fun getRSAKeySpec(keyBytes: ByteArray): RSAPrivateCrtKeySpec {

        var parser = DerParser(keyBytes)

        val sequence = parser.read()
        if (sequence.type != DerParser.SEQUENCE)
            throw IOException("Invalid DER: not a sequence") //$NON-NLS-1$

        // Parse inside the sequence
        parser = sequence.parser

        parser.read() // Skip version
        val modulus = parser.read().integer
        val publicExp = parser.read().integer
        val privateExp = parser.read().integer
        val prime1 = parser.read().integer
        val prime2 = parser.read().integer
        val exp1 = parser.read().integer
        val exp2 = parser.read().integer
        val crtCoef = parser.read().integer

        return RSAPrivateCrtKeySpec(
                modulus, publicExp, privateExp, prime1, prime2,
                exp1, exp2, crtCoef)
    }

}