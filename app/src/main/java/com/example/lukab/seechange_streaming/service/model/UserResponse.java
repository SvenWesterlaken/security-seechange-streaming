package com.example.lukab.seechange_streaming.service.model;

import com.google.gson.annotations.SerializedName;

public class UserResponse {
	@SerializedName("message")
	public String message;
	
	public String getMessage(){return message;}
	
	@SerializedName("username")
	public String username;
	
	public String getUsername(){return username;}
}
