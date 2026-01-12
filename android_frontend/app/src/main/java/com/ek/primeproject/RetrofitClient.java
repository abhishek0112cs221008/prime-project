package com.ek.primeproject;

import android.content.Context;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static Retrofit retrofit = null;

    public static ApiService getService(Context context) {
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(chain -> {
            Request original = chain.request();
            SessionManager session = new SessionManager(context);
            if (session.isLoggedIn()) {
                Request newRequest = original.newBuilder()
                        .header("Authorization", "Bearer " + session.getToken())
                        .build();
                return chain.proceed(newRequest);
            }
            return chain.proceed(original);
        }).build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(ApiService.class);
    }
}
