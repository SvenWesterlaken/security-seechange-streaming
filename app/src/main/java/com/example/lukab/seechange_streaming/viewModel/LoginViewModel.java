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
import android.util.Base64;

import java.io.IOException;
import java.math.BigInteger;
import java.security.spec.EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import javax.crypto.*;

import com.example.lukab.seechange_streaming.app.utils.Asn1Object;
import com.example.lukab.seechange_streaming.app.utils.DerParser;
import com.example.lukab.seechange_streaming.data.network.LoginClient;
import com.example.lukab.seechange_streaming.data.network.ServiceGenerator;
import com.example.lukab.seechange_streaming.service.model.LoginResponse;
import com.example.lukab.seechange_streaming.service.model.UserResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends AndroidViewModel {
	private ServiceGenerator serviceGenerator;
	
	
	public LoginViewModel(@NonNull Application application) {
		super(application);
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application.getApplicationContext());
		String url = "http://145.49.4.23:3000";
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
							preferences.edit().putString("private_key", decryptPrivateKey(password, response.body().getPrivateKey())).apply();
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
	
	public static RSAPublicKey getPublicKeyFromString(String publicKey)
		throws GeneralSecurityException {
			String publicKeyPEM = publicKey;
			
			publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----\n", "").replace("\n-----END PUBLIC KEY-----\n", "");
			
			byte[] encoded = Base64.decode(publicKeyPEM, Base64.NO_PADDING);
			
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(encoded));
		}
	
	public PrivateKey getPrivateKeyFromString(String privateKey)
			throws GeneralSecurityException {
		String privateKeyPEM = privateKey;

		privateKeyPEM = privateKeyPEM
				.replace("-----BEGIN RSA PRIVATE KEY-----", "")
				.replace("-----END RSA PRIVATE KEY-----", "");
		Log.d("LoginViewModel: ", "privatePem without begin and end: " + privateKeyPEM);
		
		byte[] encoded = Base64.decode(privateKeyPEM, Base64.NO_PADDING);

//		PKCS8EncodedKeySpec keySpecPKCS8 = new EncodedKeySpec();
        KeyFactory kf = KeyFactory.getInstance("RSA");
		RSAPrivateCrtKeySpec rsaKeySpec = null;

		try {
			rsaKeySpec = getRSAKeySpec(encoded);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (rsaKeySpec != null) {
			return kf.generatePrivate(rsaKeySpec);
		} else {
			return null;
		}
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

	public RSAPrivateCrtKeySpec getRSAKeySpec(byte[] keyBytes) throws IOException {

		DerParser parser = new DerParser(keyBytes);

		Asn1Object sequence = parser.read();
		if (sequence.getType() != DerParser.SEQUENCE)
			throw new IOException("Invalid DER: not a sequence"); //$NON-NLS-1$

		// Parse inside the sequence
		parser = sequence.getParser();

		parser.read(); // Skip version
		BigInteger modulus = parser.read().getInteger();
		BigInteger publicExp = parser.read().getInteger();
		BigInteger privateExp = parser.read().getInteger();
		BigInteger prime1 = parser.read().getInteger();
		BigInteger prime2 = parser.read().getInteger();
		BigInteger exp1 = parser.read().getInteger();
		BigInteger exp2 = parser.read().getInteger();
		BigInteger crtCoef = parser.read().getInteger();

		RSAPrivateCrtKeySpec keySpec = new RSAPrivateCrtKeySpec(
				modulus, publicExp, privateExp, prime1, prime2,
				exp1, exp2, crtCoef);

		return keySpec;
	}
	
}


