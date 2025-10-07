package com.example.kibo.api;

import com.example.kibo.models.ApiResponse;
import com.example.kibo.models.LoginRequest;
import com.example.kibo.models.LoginResponse;
import com.example.kibo.models.OtpRequest;
import com.example.kibo.models.RegisterRequest;
import com.example.kibo.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

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
}
