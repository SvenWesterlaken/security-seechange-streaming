package com.example.lukab.seechange_streaming.viewModel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.example.lukab.seechange_streaming.data.network.ServiceGenerator
import com.example.lukab.seechange_streaming.data.network.UserSettingsClient
import retrofit2.Call
import retrofit2.Callback
import okhttp3.RequestBody
import retrofit2.Response
import org.json.JSONException
import org.json.JSONObject





class UserSettingsViewModel(application: Application): AndroidViewModel(application) {

    fun updatePublicname(name: String): LiveData<Boolean> {
        val settingsClient: UserSettingsClient = ServiceGenerator.createService(UserSettingsClient::class.java)
        val paramObject: JSONObject = JSONObject()
        val succeeded: MutableLiveData<Boolean> = MutableLiveData()

        try {

            paramObject.put("username", name)
            settingsClient.updatePublicName(
                    RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),
                        paramObject.toString()
                    )
            ).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    succeeded.value = response.isSuccessful
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    succeeded.value = false
                }
            })


        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return succeeded

    }


}