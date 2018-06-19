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

import com.example.lukab.seechange_streaming.data.network.LoginClient;
import com.example.lukab.seechange_streaming.data.network.ServiceGenerator;
import com.example.lukab.seechange_streaming.service.model.LoginResponse;
import com.example.lukab.seechange_streaming.service.model.UserResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends AndroidViewModel {
	private ServiceGenerator serviceGenerator;
	
	
	String newToken;
	public LoginViewModel(@NonNull Application application) {
		super(application);
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application.getApplicationContext());
		String url = "http://" + sharedPreferences.getString("pref_seechange_ip", "10.0.2.2") + ":" + sharedPreferences.getInt("pref_stream_user_api_port", 3000);
		this.serviceGenerator = new ServiceGenerator(url);
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
							preferences.edit().putString("token", decryptToken(response.body().getToken(), response.body().getPublicKey())).apply();
							preferences.edit().putString("private_key", decryptPrivateKey(password, response.body().getPrivateKey())).apply();
							
							newToken = response.body().getRealToken();
							//ToDo: delete logs
							Log.d("e", preferences.getString("token", null));
							Log.d("e", response.body().getRealToken());
							Log.d("e",  decryptPrivateKey(password, response.body().getPrivateKey()));
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
					Log.d("error", t.getMessage());
					loggedIn.setValue(false);
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return loggedIn;
		
	}
	
	public  static RSAPublicKey getPublicKeyFromString(String publicKey)
		throws GeneralSecurityException {
			String publicKeyPEM = publicKey;
			
			publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----\n", "").replace("\n-----END PUBLIC KEY-----\n", "");
			
			byte[] encoded = Base64.decode(publicKeyPEM, Base64.NO_PADDING);
			
			KeyFactory kf = KeyFactory.getInstance("RSA");
			RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(encoded));
			return pubKey;
		}
		
	public LiveData<Boolean> checkToken(String token, String username){
		LoginClient loginService = this.serviceGenerator.createService(LoginClient.class, token);
	
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
	
	public String decryptToken(String tokenToDecrypt, String publicKey) throws Exception {
		tokenToDecrypt = tokenToDecrypt.replace("[", "").replace("]", "");
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, getPublicKeyFromString(publicKey));
		byte[] clearbyte = cipher.doFinal(Base64.decode(tokenToDecrypt, Base64.NO_PADDING));
		return new String(clearbyte);
	}
	
	
	private static String decryptPrivateKey(String password, String encrypted) throws Exception {
		byte[] key = password.getBytes();
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] thedigest = md.digest(key);
		SecretKeySpec skey = new SecretKeySpec(thedigest, "AES/ECB/PKCS7Padding");
		Cipher dcipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
		dcipher.init(Cipher.DECRYPT_MODE, skey);
		byte[] clearbyte = dcipher.doFinal(toByte(encrypted));
		return new String(clearbyte);
	}
	
	public static byte[] toByte(String hexString) {
		int len = hexString.length() / 2;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++)
			result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
		return result;
	}
	
}


