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

import com.example.lukab.seechange_streaming.data.network.LoginClient;
import com.example.lukab.seechange_streaming.service.model.LoginResponse;
import com.example.lukab.seechange_streaming.data.network.ServiceGenerator;


import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.Call;
import retrofit2.Callback;

import static android.content.Context.MODE_PRIVATE;

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
	
	public LiveData<Boolean> login(String username, String password) {
		
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
						Log.d("token", response.body().getToken());
						SharedPreferences preferences = getApplication().getSharedPreferences("user", Context.MODE_PRIVATE);
						preferences.edit().putString("token", response.body().getToken()).commit();
						loggedIn.setValue(true);
						
					} else {
						Log.d("error", "error");
						loggedIn.setValue(false);
					}
				}
				
				@Override
				public void onFailure(Call<LoginResponse> call, Throwable t) {
					// something went completely south (like no internet connection)
					Log.d("error", t.getMessage());
					loggedIn.setValue(false);
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return loggedIn;
		
	}
	
}
