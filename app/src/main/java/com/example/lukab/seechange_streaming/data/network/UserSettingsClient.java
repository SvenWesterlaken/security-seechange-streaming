package com.example.lukab.seechange_streaming.data.network;

import com.example.lukab.seechange_streaming.service.model.SeeChangeApiResponse;

import org.json.JSONObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;


public interface UserSettingsClient {
    @Headers({
            "Accept: application/json",
            "Content-type: application/json"
    })
    @PUT(SettingParametersKt.updatePublicNameUrl)
    Call<SeeChangeApiResponse> updatePublicName(@Body RequestBody params);

    @Headers({
            "Accept: application/json",
            "Content-type: application/json"
    })
    @PUT(SettingParametersKt.updateSloganUrl)
    Call<SeeChangeApiResponse> updateSlogan(@Body RequestBody params);

    @Multipart
    @PUT(SettingParametersKt.avatarUrl)
    Call<String> updateAvatar(@Part MultipartBody.Part filePart);

    @Headers({
            "Accept: application/json",
            "Content-type: application/json"
    })
    @GET(SettingParametersKt.getUserInfoUrl)
    Call<String> getUserInfoUrl(@Body RequestBody params);
}

