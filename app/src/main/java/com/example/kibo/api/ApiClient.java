package com.example.kibo.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static final String BASE_URL = "https://kibo-cbpk.onrender.com/api/"; // Thay đổi URL này thành URL API thực tế của bạn
    
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            // Create logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Create OkHttp client with optimized timeouts for image upload
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(15, TimeUnit.SECONDS)      // 15s to connect
                    .readTimeout(60, TimeUnit.SECONDS)         // 60s to read response  
                    .writeTimeout(60, TimeUnit.SECONDS)        // 60s to send data (optimized for compressed images)
                    .callTimeout(90, TimeUnit.SECONDS)         // 90s total call timeout
                    .retryOnConnectionFailure(true)            // Retry on connection failure
                    .pingInterval(30, TimeUnit.SECONDS)        // Keep connection alive
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

    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofit().create(ApiService.class);
        }
        return apiService;
    }

    // Method to update base URL if needed
    public static void updateBaseUrl(String newBaseUrl) {
        retrofit = null;
        apiService = null;
        // You might want to store the new URL in SharedPreferences or a config file
    }
}
