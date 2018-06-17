package com.example.lukab.seechange_streaming.data.network.clients;

import com.example.lukab.seechange_streaming.app.util.ConstantsKt;
import com.example.lukab.seechange_streaming.service.model.SeeChangeApiResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Part;


public interface UserSettingsClient {
    @Headers({
            "Accept: application/json",
            "Content-type: application/json"
    })
    @PUT(ConstantsKt.updatePublicNameUrl)
    Call<SeeChangeApiResponse> updatePublicName(@Body RequestBody params);

    @Headers({
            "Accept: application/json",
            "Content-type: application/json"
    })
    @PUT(ConstantsKt.updateSloganUrl)
    Call<SeeChangeApiResponse> updateSlogan(@Body RequestBody params);

    @Multipart
    @PUT(ConstantsKt.avatarUrl)
    Call<SeeChangeApiResponse> updateAvatar(@Part MultipartBody.Part filePart, @Part("username") RequestBody username);

    @Headers({
            "Accept: application/json",
            "Content-type: application/json"
    })
    @GET(ConstantsKt.getUserInfoUrl)
    Call<String> getUserInfoUrl(@Body RequestBody params);
}

