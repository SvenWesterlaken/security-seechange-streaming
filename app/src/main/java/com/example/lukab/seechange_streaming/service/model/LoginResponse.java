package com.example.lukab.seechange_streaming.service.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;


public class LoginResponse {
	@SerializedName("message")
	public String message;
	
	public String getMessage(){return message;}
	
	@SerializedName("token")
	public String token;
	
	public String getToken(){return token;}
	
	@SerializedName("realToken")
	public String realToken;
	
	public String getRealToken(){return realToken;}
	
	@SerializedName("privateKey")
	public String privateKey;
	
	public String getPrivateKey(){ return privateKey; }
	
	@SerializedName("publicKey")
	public String publicKey;
	
	public String getPublicKey(){ return publicKey; }
}
