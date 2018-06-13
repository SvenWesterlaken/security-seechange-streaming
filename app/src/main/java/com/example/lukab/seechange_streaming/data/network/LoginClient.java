package com.example.lukab.seechange_streaming.data.network;


import android.arch.lifecycle.LiveData;



import org.json.JSONObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

import java.util.ArrayList;

public interface LoginClient {
	
	//post url
	@Headers({
			"Accept: application/json", "Content-Type: application/json"
	})
	@POST("api/v1/login")
	Call<LoginResponse> login(@Body RequestBody params);
	
	
}
