package com.example.kibo.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.content.Context;
import com.example.kibo.utils.SessionManager;

public class ApiClient {
    private static final String BASE_URL = "https://kibo-cbpk.onrender.com/api/"; // Thay đổi URL này thành URL API thực tế của bạn
    
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            // Create logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Create OkHttp client
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            // Create Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getRetrofitWithAuth(Context context) {
        // Create logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Create authentication interceptor
        okhttp3.Interceptor authInterceptor = chain -> {
            okhttp3.Request originalRequest = chain.request();
            SessionManager sessionManager = new SessionManager(context);
            String token = sessionManager.getAccessToken();
            
            if (token != null && !token.isEmpty()) {
                okhttp3.Request newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .build();
                return chain.proceed(newRequest);
            }
            
            return chain.proceed(originalRequest);
        };

        // Create OkHttp client with auth interceptor
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .build();

        // Create Retrofit instance
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofit().create(ApiService.class);
        }
        return apiService;
    }

    public static ApiService getApiServiceWithAuth(Context context) {
        return getRetrofitWithAuth(context).create(ApiService.class);
    }

    // Method to update base URL if needed
    public static void updateBaseUrl(String newBaseUrl) {
        retrofit = null;
        apiService = null;
        // You might want to store the new URL in SharedPreferences or a config file
    }
}
