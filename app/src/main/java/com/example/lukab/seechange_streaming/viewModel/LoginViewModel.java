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
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.example.lukab.seechange_streaming.data.network.LoginClient;
import com.example.lukab.seechange_streaming.service.model.LoginResponse;
import com.example.lukab.seechange_streaming.data.network.ServiceGenerator;
import com.example.lukab.seechange_streaming.service.model.UserResponse;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import android.util.Base64;

import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.Call;
import retrofit2.Callback;

import static android.content.Context.MODE_PRIVATE;
import static android.util.Base64.decode;

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
							preferences.edit().putString("private_key", decryptPrivateKey(password, response.body().getPrivateKey())).apply();
							
							//ToDo: delete logs
							Log.d("e", preferences.getString("token", null));
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
	
	public  static RSAPrivateKey getPrivateKeyFromString(String privateKey)
			throws GeneralSecurityException {
		String privateKeyPEM = privateKey;
		
		privateKeyPEM = privateKeyPEM.replace("-----BEGIN RSA PRIVATE KEY-----", "").replace("-----END RSA PRIVATE KEY-----", "");
		
		byte[] encoded = Base64.decode(privateKeyPEM, Base64.NO_PADDING);
		
		KeyFactory kf = KeyFactory.getInstance("RSA");
		RSAPrivateKey privKey = (RSAPrivateKey) kf.generatePrivate(new X509EncodedKeySpec(encoded));
		return privKey;
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


