package com.example.lukab.seechange_streaming.data.network

import android.content.Context
import android.text.TextUtils
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

class ServiceGenerator(baseUrl: String, context: Context) {
    private var retrofit: Retrofit
    //private val httpClient: OkHttpClient.Builder = OkHttpClient.Builder()
    private val httpClient: HttpClient = HttpClient()
    private val builder: Retrofit.Builder = Retrofit.Builder().client(httpClient.unsafeOkHttpClient).baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create())
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

                httpClient.addInterceptor(interceptor)
                this.retrofit = builder.build()
        }

        return retrofit.create(serviceClass)
    }



}