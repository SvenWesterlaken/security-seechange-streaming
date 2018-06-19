package com.example.lukab.seechange_streaming.data.network;

import android.content.Context;
import android.text.TextUtils;

import com.example.lukab.seechange_streaming.R;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ServiceGenerator {
    //url frorm api
    public static final String API_BASE_URL = "http://10.0.2.2:3000";
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    
    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    //.client(getHttpClient())
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());
    
                  

    private static Retrofit retrofit = builder.build();
    
    public static <S> S createService(Class<S> serviceClass) {
        return retrofit.create(serviceClass);
    }
    
    
    private static OkHttpClient getHttpClient(Context context){
        try {
            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // get InputStream for the certificate
            InputStream caInput = context.getResources().openRawResource(R.raw.localhost);
            Certificate ca;
            ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            // Create a KeyStore containing our trusted CAs
            
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);
            // Create a TrustManager that trusts the CAs in our KeyStore
            
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);
            // Create an SSLContext that uses our TrustManager
            
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, tmf.getTrustManagers(), null);
            final OkHttpClient client;
            client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslcontext.getSocketFactory())
                    .build();
            caInput.close();
            return client;
        } catch (Exception e) {
            return null;
        }
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
