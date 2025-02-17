package com.example.lukab.seechange_streaming.viewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.example.lukab.seechange_streaming.app.utils.Crypto;
import com.example.lukab.seechange_streaming.data.network.LoginClient;
import com.example.lukab.seechange_streaming.data.network.ServiceGenerator;
import com.example.lukab.seechange_streaming.service.model.LoginResponse;
import com.example.lukab.seechange_streaming.service.model.UserResponse;

import org.json.JSONException;
import org.json.JSONObject;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends AndroidViewModel {
	private ServiceGenerator serviceGenerator;
	
	
	public LoginViewModel(@NonNull Application application) {
		super(application);
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application.getApplicationContext());
//		String url = "http://145.49.2.71:3000";
//    	String url = "http:/145.49.19.129:3000";
//    	String url = "http:/145.49.44.67:3000";
		String url = "http:/145.49.18.217:3000";
		this.serviceGenerator = new ServiceGenerator(url);
	}
	
	public boolean isUsernameAndPasswordValid(String username, String password) {
		
		return !TextUtils.isEmpty(username) && !TextUtils.isEmpty(password);
	}
	
	public LiveData<Boolean> login(final String username, final String password) {
		
		LoginClient loginService = this.serviceGenerator.createService(LoginClient.class);
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
		LoginClient loginService =  serviceGenerator.createService(LoginClient.class);
		JSONObject paramObject = new JSONObject();
		RequestBody body;
		final MutableLiveData<Boolean> validToken = new MutableLiveData<>();
		
		try {
			paramObject.put("token", token);
			body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (paramObject).toString());
			
			Call<UserResponse> call = loginService.verifyToken(username, body);
			call.enqueue(new Callback<UserResponse>() {
				@Override
				public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
					if (response.isSuccessful()) {
						
						try {
							validToken.setValue(true);
						} catch (Exception e) {
							Log.e("error", e.toString());
						}
						
					} else {
						Log.d("error", response.message());
						validToken.setValue(false);
					}
				}
				
				@Override
				public void onFailure(Call<UserResponse> call, Throwable t) {
					Log.d("error", t.getMessage());
					validToken.setValue(false);
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return validToken;
		
		
	}

}


