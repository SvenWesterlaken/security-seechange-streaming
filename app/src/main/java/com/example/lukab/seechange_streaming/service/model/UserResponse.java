package com.example.lukab.seechange_streaming.service.model;

import com.google.gson.annotations.SerializedName;

public class UserResponse {
	@SerializedName("publicKey")
	public String publicKey;
	
	public String getPublicKey(){ return publicKey; }
}
