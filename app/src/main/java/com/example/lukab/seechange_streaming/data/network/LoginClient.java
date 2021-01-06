package com.example.lukab.seechange_streaming.data.network;


import com.example.lukab.seechange_streaming.service.model.LoginResponse;
import com.example.lukab.seechange_streaming.service.model.UserResponse;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

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
	@POST("api/v1/verify/{name}")
	Call<UserResponse> verifyToken(@Path("name") String username, @Body RequestBody params);
	
	
	
}
