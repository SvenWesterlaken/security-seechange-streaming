package com.example.lukab.seechange_streaming.data.network

import android.content.Context
import android.text.TextUtils
import com.example.lukab.seechange_streaming.R
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

class ServiceGenerator(baseUrl: String) {
    private var retrofit: Retrofit
    private val httpClient: OkHttpClient.Builder = OkHttpClient.Builder()
    private val builder: Retrofit.Builder = Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create())
       //.client(getHttpClient())

    init {
        this.retrofit = this.builder.build()
    }

    fun <S> createService(serviceClass: Class<S>): S {
        return retrofit.create(serviceClass)
    }

    fun <S> createService(serviceClass: Class<S>, authToken: String): S {
        if (!TextUtils.isEmpty(authToken)) {
            val interceptor = AuthenticationInterceptor(authToken)

            if (!httpClient.interceptors().contains(interceptor)) {
                httpClient.addInterceptor(interceptor)
                builder.client(httpClient.build())
                this.retrofit = builder.build()
            }
        }

        return retrofit.create(serviceClass)
    }

    private fun getHttpClient(context: Context): OkHttpClient? {
        try {
            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            val cf = CertificateFactory.getInstance("X.509")
            // get InputStream for the certificate
            val caInput = context.resources.openRawResource(R.raw.localhost)
            val ca: Certificate
            ca = cf.generateCertificate(caInput)
            println("ca=" + (ca as X509Certificate).subjectDN)
            // Create a KeyStore containing our trusted CAs

            val keyStoreType = KeyStore.getDefaultType()
            val keyStore = KeyStore.getInstance(keyStoreType)
            keyStore.load(null, null)
            keyStore.setCertificateEntry("ca", ca)
            // Create a TrustManager that trusts the CAs in our KeyStore

            val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
            val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
            tmf.init(keyStore)
            // Create an SSLContext that uses our TrustManager

            val sslcontext = SSLContext.getInstance("TLS")
            sslcontext.init(null, tmf.trustManagers, null)
            val client: OkHttpClient
            client = OkHttpClient.Builder()
                    .sslSocketFactory(sslcontext.socketFactory)
                    .build()
            caInput.close()
            return client
        } catch (e: Exception) {
            return null
        }

    }

}