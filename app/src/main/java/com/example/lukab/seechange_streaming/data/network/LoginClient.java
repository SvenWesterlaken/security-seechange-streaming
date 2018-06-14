package com.example.lukab.seechange_streaming.data.network;


import com.example.lukab.seechange_streaming.service.model.LoginResponse;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface LoginClient {
	
	//post url
	@Headers({
			"Accept: application/json", "Content-Type: application/json"
	})
	@POST("api/v1/login")
	Call<LoginResponse> login(@Body RequestBody params);
	
	@Headers({
			"Accept: application/json", "Content-Type: application/json"
	})
	@POST("api/v1/token")
	Call<String> validateToken(@Body RequestBody params);
	
	
	
}
