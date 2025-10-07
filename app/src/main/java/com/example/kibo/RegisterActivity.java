package com.example.kibo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.ApiResponse;
import com.example.kibo.models.RegisterRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextPhone;
    private Button buttonRegister;
    private TextView textViewLogin;
    private TextView textViewWelcome;
    
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize API service
        apiService = ApiClient.getApiService();
        
        // Initialize views
        initViews();
        
        // Setup animations
        setupAnimations();
        
        // Setup click listeners
        setupClickListeners();
        
        // Setup back button handling
        setupBackButton();
    }

    private void initViews() {
        editTextUsername = findViewById(R.id.edit_text_username);
        editTextEmail = findViewById(R.id.edit_text_email);
        editTextPassword = findViewById(R.id.edit_text_password);
        editTextPhone = findViewById(R.id.edit_text_phone);
        buttonRegister = findViewById(R.id.button_register);
        textViewLogin = findViewById(R.id.text_view_login);
        textViewWelcome = findViewById(R.id.text_view_welcome);
    }

    private void setupAnimations() {
        // Load animations
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation slideDownAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down);

        // Apply animations with delays
        textViewWelcome.startAnimation(fadeInAnimation);
        
        editTextUsername.startAnimation(slideUpAnimation);
        editTextEmail.startAnimation(slideUpAnimation);
        editTextPassword.startAnimation(slideUpAnimation);
        editTextPhone.startAnimation(slideUpAnimation);
        
        buttonRegister.startAnimation(slideUpAnimation);
        textViewLogin.startAnimation(slideDownAnimation);
    }

    private void setupClickListeners() {
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegister();
            }
        });

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to login activity
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.fade_out);
            }
        });
    }

    private void setupBackButton() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Navigate back to login activity
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void performRegister() {
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        // Basic validation
        if (username.isEmpty()) {
            editTextUsername.setError("Tên người dùng là bắt buộc");
            editTextUsername.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            editTextEmail.setError("Email là bắt buộc");
            editTextEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Mật khẩu là bắt buộc");
            editTextPassword.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            editTextPhone.setError("Số điện thoại là bắt buộc");
            editTextPhone.requestFocus();
            return;
        }

        if (username.length() < 3) {
            editTextUsername.setError("Tên người dùng phải có ít nhất 3 ký tự");
            editTextUsername.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Vui lòng nhập email hợp lệ");
            editTextEmail.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            editTextPassword.requestFocus();
            return;
        }

        if (phone.length() < 10) {
            editTextPhone.setError("Vui lòng nhập số điện thoại hợp lệ");
            editTextPhone.requestFocus();
            return;
        }

        // Show loading state
        buttonRegister.setText("Đang đăng ký...");
        buttonRegister.setEnabled(false);

        // Create register request
        RegisterRequest registerRequest = new RegisterRequest(username, email, password, phone);

        // Debug log
        android.util.Log.d("RegisterActivity", "Register Request - Username: " + username);
        android.util.Log.d("RegisterActivity", "Register Request - Email: " + email);
        android.util.Log.d("RegisterActivity", "Register Request - Phone: " + phone);
        android.util.Log.d("RegisterActivity", "Register Request - Role: " + registerRequest.getRole());

        // Call API
        Call<ApiResponse<String>> call = apiService.register(registerRequest);
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                // Reset button state
                buttonRegister.setText("Đăng ký");
                buttonRegister.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess()) {
                        // Show success message
                        Toast.makeText(RegisterActivity.this, "Đăng ký thành công! Vui lòng xác thực OTP.", Toast.LENGTH_LONG).show();

                        // Navigate to OTP verification activity
                        Intent intent = new Intent(RegisterActivity.this, OtpVerificationActivity.class);
                        intent.putExtra("email", email);
                        intent.putExtra("username", username);
                        intent.putExtra("phone", phone);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    } else {
                        // Handle API error response
                        String errorMessage = apiResponse.getMessage() != null ? 
                            apiResponse.getMessage() : "Đăng ký thất bại";
                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // HTTP error (400, 401, 500, etc.)
                    String errorMessage = "Đăng ký thất bại";
                    
                    if (response.code() == 400) {
                        errorMessage = "Dữ liệu đăng ký không hợp lệ";
                    } else if (response.code() == 409) {
                        errorMessage = "Email đã được sử dụng";
                    } else if (response.code() == 500) {
                        errorMessage = "Lỗi server, vui lòng thử lại sau";
                    }
                    
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                // Reset button state
                buttonRegister.setText("Đăng ký");
                buttonRegister.setEnabled(true);

                // Handle network error
                String errorMessage = "Lỗi kết nối mạng";
                if (t.getMessage() != null && t.getMessage().contains("timeout")) {
                    errorMessage = "Kết nối quá chậm, vui lòng thử lại";
                } else if (t.getMessage() != null && t.getMessage().contains("Unable to resolve host")) {
                    errorMessage = "Không thể kết nối đến server";
                }
                
                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
