package com.example.kibo.api;

import com.example.kibo.models.ApiResponse;
import com.example.kibo.models.LoginRequest;
import com.example.kibo.models.LoginResponse;
import com.example.kibo.models.OtpRequest;
import com.example.kibo.models.RegisterRequest;
import com.example.kibo.models.User;
import com.example.kibo.models.ProductResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    
    @POST("Auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);
    
    @POST("Auth/register")
    Call<ApiResponse<String>> register(@Body RegisterRequest registerRequest);
    
    @POST("Auth/verify-otp")
    Call<LoginResponse> verifyOtp(@Body OtpRequest otpRequest);
    
    @POST("Auth/resend-otp")
    Call<ApiResponse<String>> resendOtp(@Body String email);
    
    // Alternative method for login that returns raw response
    @POST("Auth/login")
    Call<Object> loginRaw(@Body LoginRequest loginRequest);
    
    // Get products with pagination
    @GET("Products")
    Call<ProductResponse> getProducts(
        @Query("pageNumber") int pageNumber,
        @Query("pageSize") int pageSize
    );
    
    // Get all products without pagination
    @GET("Products")
    Call<ProductResponse> getAllProducts();
    
    // Get product by ID
    @GET("Products")
    Call<ProductResponse> getProductById(@Query("Productid") int productId);
}
