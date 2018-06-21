package com.example.lukab.seechange_streaming.viewModel;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.security.spec.RSAPrivateCrtKeySpec;

import com.example.lukab.seechange_streaming.app.utils.Asn1Object;
import com.example.lukab.seechange_streaming.app.utils.Crypto;
import com.example.lukab.seechange_streaming.app.utils.DerParser;
import com.example.lukab.seechange_streaming.data.network.LoginClient;
import com.example.lukab.seechange_streaming.data.network.ServiceGenerator;
import com.example.lukab.seechange_streaming.service.model.LoginResponse;
import com.example.lukab.seechange_streaming.service.model.UserResponse;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends AndroidViewModel {
	
	
	public LoginViewModel(@NonNull Application application) {
		super(application);
	}
	
	public boolean isUsernameAndPasswordValid(String username, String password) {
		
		if (TextUtils.isEmpty(username)) {
			return false;
		}
		if (TextUtils.isEmpty(password)) {
			return false;
		}
		return true;
	}
	
	public LiveData<Boolean> login(final String username, final String password) {
		
		LoginClient loginService =
				ServiceGenerator.createService(LoginClient.class);
		JSONObject paramObject = new JSONObject();
		RequestBody body;
		final MutableLiveData<Boolean> loggedIn = new MutableLiveData<>();
		
		try {
			
			paramObject.put("username", username);
			paramObject.put("password", password);
			body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (paramObject).toString());
			
			Call<LoginResponse> call = loginService.login(body);
			call.enqueue(new Callback<LoginResponse>() {
				@Override
				public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
					if (response.isSuccessful()) {
						
						try {
							
							SharedPreferences preferences = getApplication().getSharedPreferences("user", Context.MODE_PRIVATE);
							preferences.edit().clear().apply();
							preferences.edit().putString("username", username).apply();
							preferences.edit().putString("token", response.body().getToken()).apply();
							preferences.edit().putString("private_key", Crypto.decryptPrivateKey(password, response.body().getPrivateKey())).apply();
							preferences.edit().putString("public_key", response.body().getPublicKey()).apply();

							//ToDo: delete logs
							Log.d("e", preferences.getString("token", null));
							Log.d("e",  Crypto.decryptPrivateKey(password, response.body().getPrivateKey()));
							loggedIn.setValue(true);
						} catch (Exception e) {
							Log.e("error", e.toString());
						}
						
					} else {
						Log.d("error", "error");
						loggedIn.setValue(false);
					}
				}
				
				@Override
				public void onFailure(Call<LoginResponse> call, Throwable t) {
					Log.d("error", "message: " + t.getMessage());
					loggedIn.setValue(false);
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return loggedIn;
		
	}

	public LiveData<Boolean> checkToken(String token, String username){
		LoginClient loginService =
				ServiceGenerator.createService(LoginClient.class, token);
	
		final MutableLiveData<Boolean> loggedIn = new MutableLiveData<>();
			
			Call<UserResponse> call = loginService.getUser(username);
			call.enqueue(new Callback<UserResponse>() {
				@Override
				public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
					if (response.isSuccessful()) {
						
						try {
							Log.e("tokenSuccesful", response.message());
						
						} catch (Exception e) {
							Log.e("error", e.toString());
						}
						
						
					} else {
						Log.d("error", response.message());
						loggedIn.setValue(false);
					}
				}
				
				@Override
				public void onFailure(Call<UserResponse> call, Throwable t) {
					Log.d("error", t.getMessage());
					loggedIn.setValue(false);
				}});
		
		
		return loggedIn;
		
		
	}

}


