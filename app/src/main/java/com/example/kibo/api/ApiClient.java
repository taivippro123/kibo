package com.example.kibo.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.concurrent.TimeUnit;
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

            // Create OkHttp client with optimized timeouts for image upload
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)      // 15s to connect
                .readTimeout(90, TimeUnit.SECONDS)         // 90s to read response  
                .writeTimeout(90, TimeUnit.SECONDS)        // 90s to send data
                .callTimeout(120, TimeUnit.SECONDS)        // 120s total call timeout
                .retryOnConnectionFailure(true)            // Retry on connection failure
                .pingInterval(20, TimeUnit.SECONDS)        // Keep connection alive - giảm interval
                .protocols(java.util.Arrays.asList(okhttp3.Protocol.HTTP_1_1)) // Force HTTP/1.1 to avoid PROTOCOL_ERROR
                .connectionPool(new okhttp3.ConnectionPool(10, 5, TimeUnit.MINUTES)) // Tăng connection pool
                .build();

            // Configure Gson to parse ISO8601 with fractional seconds
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
                    .create();

            // Create Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
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

        // Configure Gson to parse ISO8601 with fractional seconds
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
                .create();

        // Create Retrofit instance
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
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
