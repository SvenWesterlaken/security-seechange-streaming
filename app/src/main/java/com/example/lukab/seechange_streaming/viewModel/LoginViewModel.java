package com.example.lukab.seechange_streaming.viewModel;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.example.lukab.seechange_streaming.ui.activities.LoginActivity;

import javax.inject.Inject;

public class LoginViewModel extends AndroidViewModel {
	
	
	public LoginViewModel(@NonNull Application application) {
		super(application);
	}
	
	public  boolean isUsernameAndPasswordValid(String username, String password){
		
		if (TextUtils.isEmpty(username)) {
			return false;
		}
		if (TextUtils.isEmpty(password)) {
			return false;
		}
		return true;
	}
	
	public void login(){
		//check username and password
		
		//valid?
		
	}
	
}
