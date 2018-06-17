package com.example.lukab.seechange_streaming.viewModel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.preference.*
import com.example.lukab.seechange_streaming.data.network.ServiceGenerator
import com.example.lukab.seechange_streaming.data.network.clients.UserSettingsClient
import com.example.lukab.seechange_streaming.service.model.SeeChangeApiResponse
import okhttp3.MediaType
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import okhttp3.RequestBody
import retrofit2.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.File


class UserSettingsViewModel(application: Application, private val username: String): AndroidViewModel(application) {
    private val serviceGenerator: ServiceGenerator
    private val settingsClient: UserSettingsClient

    init {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application.applicationContext)
        val url = "http://${sharedPreferences.getString("pref_seechange_ip", "145.49.56.174")}:${sharedPreferences.getInt("pref_stream_user_api_port", 8081)}"
        this.serviceGenerator = ServiceGenerator(url)
        this.settingsClient = this.serviceGenerator.createService(UserSettingsClient::class.java)
    }

    fun updatePublicName(name: String): LiveData<Boolean> {
        val paramObject = JSONObject()
        val succeeded: MutableLiveData<Boolean> = MutableLiveData()

        try {

            paramObject.put("username", this.username)
            paramObject.put("publicName", name)
            settingsClient.updatePublicName(
                    RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),
                        paramObject.toString()
                    )
            ).enqueue(object: Callback<SeeChangeApiResponse> {
                override fun onResponse(call: Call<SeeChangeApiResponse>, response: Response<SeeChangeApiResponse>) {
                    succeeded.value = response.isSuccessful
                }

                override fun onFailure(call: Call<SeeChangeApiResponse>, t: Throwable) {
                    succeeded.value = false
                }
            })


        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return succeeded

    }

    fun updateSlogan(slogan: String): LiveData<Boolean> {
        val paramObject = JSONObject()
        val succeeded: MutableLiveData<Boolean> = MutableLiveData()

        try {
            paramObject.put("username", this.username)
            paramObject.put("slogan", slogan)
            settingsClient.updateSlogan(
                    RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),
                        paramObject.toString()
                    )
            ).enqueue(object: Callback<SeeChangeApiResponse> {
                override fun onResponse(call: Call<SeeChangeApiResponse>, response: Response<SeeChangeApiResponse>) {
                    succeeded.value = response.isSuccessful
                }

                override fun onFailure(call: Call<SeeChangeApiResponse>, t: Throwable) {
                    succeeded.value = false
                }
            })
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return succeeded
    }

    fun uploadAvatar(image: File): LiveData<Boolean> {
        val succeeded: MutableLiveData<Boolean> = MutableLiveData()
        val filePart = MultipartBody.Part.createFormData("avatar", image.name, RequestBody.create(MediaType.parse("image/*"), image))
        val username = RequestBody.create(okhttp3.MediaType.parse("text"), this.username)

        settingsClient.updateAvatar(filePart, username).enqueue(object: Callback<SeeChangeApiResponse> {
            override fun onResponse(call: Call<SeeChangeApiResponse>, response: Response<SeeChangeApiResponse>) {
                succeeded.value = response.isSuccessful
            }

            override fun onFailure(call: Call<SeeChangeApiResponse>, t: Throwable) {
                succeeded.value = false
            }
        })

        return succeeded

    }


}