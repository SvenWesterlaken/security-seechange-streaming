package com.example.lukab.seechange_streaming.service.model

import com.google.gson.annotations.SerializedName

class SeeChangeApiResponse {
    @SerializedName("msg")
    lateinit var message: String

    @SerializedName("error")
    lateinit var error: String

}