package com.example.lukab.seechange_streaming.data.network;

import android.text.TextUtils;
import android.util.Base64;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class ServiceGenerator {
    //url frorm api
    public static final String API_BASE_URL = "http://10.0.2.2:3000";
    
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    
    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());
    
    private static Retrofit retrofit = builder.build();
    
    public static <S> S createService(Class<S> serviceClass) {
        //return createService(serviceClass, null, null);
        return retrofit.create(serviceClass);
    }
    

    public static <S> S createService(
            Class<S> serviceClass, final String authToken) {
        if (!TextUtils.isEmpty(authToken)) {
            AuthenticationInterceptor interceptor =
                    new AuthenticationInterceptor(authToken);
            
            if (!httpClient.interceptors().contains(interceptor)) {
                httpClient.addInterceptor(interceptor);
                
                builder.client(httpClient.build());
                retrofit = builder.build();
            }
        }
        
        return retrofit.create(serviceClass);
    }


}
