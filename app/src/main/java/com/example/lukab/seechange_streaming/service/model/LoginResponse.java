package com.example.lukab.seechange_streaming.service.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
	@SerializedName("message")
	public String message;
	
	public String getMessage(){return message;}
	
	@SerializedName("token")
	public String token;
	
	public String getToken(){return token;}
	
}
