package com.example.kibo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ViewTreeObserver;
import android.graphics.Rect;
import android.widget.ScrollView;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import org.json.JSONObject;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.ApiResponse;
import com.example.kibo.models.LoginRequest;
import com.example.kibo.models.LoginResponse;
import com.example.kibo.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView buttonRegister;
    private TextView textViewWelcome;
    private TextView textViewSubtitle;
    private ImageView imageViewLogo;
    private ViewGroup logoContainer;
    private ViewGroup inputContainer;
    private boolean isKeyboardVisible = false;
    
    private ApiService apiService;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize API service and SharedPreferences
        apiService = ApiClient.getApiService();
        sharedPreferences = getSharedPreferences("KiboPrefs", MODE_PRIVATE);
        
        // Initialize views
        initViews();
        
        // Setup animations
        setupAnimations();
        
        // Setup click listeners
        setupClickListeners();
        
        // Setup back button handling
        setupBackButton();
        
        // Setup keyboard handling
        setupKeyboardHandling();
        
        // Setup focus listeners for better UX
        setupFocusListeners();
        
        // Check if user is already logged in
        checkAutoLogin();
        
        // Debug: Add long press listener to clear data
        setupDebugClearData();
    }

    private void initViews() {
        editTextEmail = findViewById(R.id.edit_text_email);
        editTextPassword = findViewById(R.id.edit_text_password);
        buttonLogin = findViewById(R.id.button_login);
        buttonRegister = findViewById(R.id.button_register);
        textViewWelcome = findViewById(R.id.text_view_welcome);
        textViewSubtitle = findViewById(R.id.text_view_subtitle);
        imageViewLogo = findViewById(R.id.image_view_logo);
        logoContainer = findViewById(R.id.logo_container);
        inputContainer = findViewById(R.id.input_container);
    }

    private void setupAnimations() {
        // Load animations
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation slideDownAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down);

        // Apply animations with delays
        textViewWelcome.startAnimation(fadeInAnimation);
        
        editTextEmail.startAnimation(slideUpAnimation);
        editTextPassword.startAnimation(slideUpAnimation);
        
        buttonLogin.startAnimation(slideUpAnimation);
        buttonRegister.startAnimation(slideDownAnimation);
    }

    private void setupClickListeners() {
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to register activity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.fade_out);
            }
        });
    }

    private void setupBackButton() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Navigate back to splash screen
                Intent intent = new Intent(LoginActivity.this, SplashActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void setupKeyboardHandling() {
        final View rootView = findViewById(android.R.id.content);
        final ScrollView scrollView = findViewById(R.id.scroll_view_login);
        
        if (rootView != null && scrollView != null) {
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Rect r = new Rect();
                    rootView.getWindowVisibleDisplayFrame(r);
                    int screenHeight = rootView.getRootView().getHeight();
                    int keypadHeight = screenHeight - r.bottom;
                    
                    if (keypadHeight > screenHeight * 0.15) { // Keyboard is visible
                        if (!isKeyboardVisible) {
                            isKeyboardVisible = true;
                            animateLayoutForKeyboard(true);
                        }
                    } else {
                        if (isKeyboardVisible) {
                            isKeyboardVisible = false;
                            animateLayoutForKeyboard(false);
                        }
                    }
                }
            });
        }
    }

    private void animateLayoutForKeyboard(boolean keyboardVisible) {
        if (logoContainer == null || imageViewLogo == null || inputContainer == null) return;
        
        if (keyboardVisible) {
            // Animate logo smaller
            ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(imageViewLogo, "scaleX", 1.0f, 0.6f);
            ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(imageViewLogo, "scaleY", 1.0f, 0.6f);
            ObjectAnimator logoAlpha = ObjectAnimator.ofFloat(imageViewLogo, "alpha", 1.0f, 0.7f);
            
            // Animate logo container to move up (translateY instead of margin)
            ObjectAnimator logoTranslationY = ObjectAnimator.ofFloat(logoContainer, "translationY", 0f, -100f);
            
            // Animate welcome text to move up with logo
            ObjectAnimator welcomeTranslationY = ObjectAnimator.ofFloat(textViewWelcome, "translationY", 0f, -100f);
            ObjectAnimator welcomeAlpha = ObjectAnimator.ofFloat(textViewWelcome, "alpha", 1.0f, 0.3f);
            
            // Animate subtitle text to move up with logo
            ObjectAnimator subtitleTranslationY = ObjectAnimator.ofFloat(textViewSubtitle, "translationY", 0f, -100f);
            ObjectAnimator subtitleAlpha = ObjectAnimator.ofFloat(textViewSubtitle, "alpha", 1.0f, 0.3f);
            
            // Animate input container to move up
            ObjectAnimator inputTranslationY = ObjectAnimator.ofFloat(inputContainer, "translationY", 0f, -150f);
            
            // Start animations
            logoScaleX.setDuration(300);
            logoScaleY.setDuration(300);
            logoAlpha.setDuration(300);
            logoTranslationY.setDuration(300);
            welcomeTranslationY.setDuration(300);
            welcomeAlpha.setDuration(300);
            subtitleTranslationY.setDuration(300);
            subtitleAlpha.setDuration(300);
            inputTranslationY.setDuration(300);
            
            logoScaleX.start();
            logoScaleY.start();
            logoAlpha.start();
            logoTranslationY.start();
            welcomeTranslationY.start();
            welcomeAlpha.start();
            subtitleTranslationY.start();
            subtitleAlpha.start();
            inputTranslationY.start();
            
        } else {
            // Animate logo back to original size and position
            ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(imageViewLogo, "scaleX", 0.6f, 1.0f);
            ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(imageViewLogo, "scaleY", 0.6f, 1.0f);
            ObjectAnimator logoAlpha = ObjectAnimator.ofFloat(imageViewLogo, "alpha", 0.7f, 1.0f);
            
            // Animate logo container back to original position
            ObjectAnimator logoTranslationY = ObjectAnimator.ofFloat(logoContainer, "translationY", -100f, 0f);
            
            // Animate welcome text back to original position
            ObjectAnimator welcomeTranslationY = ObjectAnimator.ofFloat(textViewWelcome, "translationY", -100f, 0f);
            ObjectAnimator welcomeAlpha = ObjectAnimator.ofFloat(textViewWelcome, "alpha", 0.3f, 1.0f);
            
            // Animate subtitle text back to original position
            ObjectAnimator subtitleTranslationY = ObjectAnimator.ofFloat(textViewSubtitle, "translationY", -100f, 0f);
            ObjectAnimator subtitleAlpha = ObjectAnimator.ofFloat(textViewSubtitle, "alpha", 0.3f, 1.0f);
            
            // Animate input container back to original position
            ObjectAnimator inputTranslationY = ObjectAnimator.ofFloat(inputContainer, "translationY", -150f, 0f);
            
            // Start animations
            logoScaleX.setDuration(300);
            logoScaleY.setDuration(300);
            logoAlpha.setDuration(300);
            logoTranslationY.setDuration(300);
            welcomeTranslationY.setDuration(300);
            welcomeAlpha.setDuration(300);
            subtitleTranslationY.setDuration(300);
            subtitleAlpha.setDuration(300);
            inputTranslationY.setDuration(300);
            
            logoScaleX.start();
            logoScaleY.start();
            logoAlpha.start();
            logoTranslationY.start();
            welcomeTranslationY.start();
            welcomeAlpha.start();
            subtitleTranslationY.start();
            subtitleAlpha.start();
            inputTranslationY.start();
        }
    }

    private void setupFocusListeners() {
        final ScrollView scrollView = findViewById(R.id.scroll_view_login);
        
        editTextPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && scrollView != null) {
                    // Small scroll to ensure password field is visible
                    scrollView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.smoothScrollTo(0, 200); // Small scroll to show password field
                        }
                    }, 100);
                }
            }
        });
    }

    private void checkAutoLogin() {
        // Check if user is already logged in
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);
        String accessToken = sharedPreferences.getString("user_token", null);
        
        if (isLoggedIn && accessToken != null && !accessToken.isEmpty()) {
            // Check if token is expired
            if (isTokenValid(accessToken)) {
                // Token is valid, navigate to MainActivity
                navigateToMainActivity();
            } else {
                // Token is expired, clear stored data
                clearStoredUserData();
            }
        }
    }

    private boolean isTokenValid(String token) {
        try {
            // Decode JWT token to check expiration
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }
            
            // Decode payload (second part)
            String payload = parts[1];
            // Add padding if needed
            while (payload.length() % 4 != 0) {
                payload += "=";
            }
            
            byte[] decodedBytes = android.util.Base64.decode(payload, android.util.Base64.DEFAULT);
            String decodedPayload = new String(decodedBytes, "UTF-8");
            
            // Parse JSON to get expiration time
            JSONObject jsonObject = new JSONObject(decodedPayload);
            long exp = jsonObject.getLong("exp");
            long currentTime = System.currentTimeMillis() / 1000; // Convert to seconds
            
            // Check if token is not expired (with 5 minutes buffer)
            return exp > (currentTime + 300); // 300 seconds = 5 minutes buffer
            
        } catch (Exception e) {
            System.out.println("Error validating token: " + e.getMessage());
            return false;
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void clearStoredUserData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("user_token");
        editor.remove("refresh_token");
        editor.remove("user_email");
        editor.remove("user_name");
        editor.remove("user_id");
        editor.remove("user_phone");
        editor.remove("user_role");
        editor.remove("user_role_name");
        editor.remove("is_logged_in");
        editor.apply();
    }

    private void attemptTokenRefresh() {
        String refreshToken = sharedPreferences.getString("refresh_token", null);
        
        if (refreshToken != null && !refreshToken.isEmpty()) {
            // TODO: Implement refresh token API call
            // For now, just clear stored data and show login screen
            clearStoredUserData();
        } else {
            clearStoredUserData();
        }
    }

    private void setupDebugClearData() {
        // Long press on logo to clear all user data (DEBUG ONLY)
        if (imageViewLogo != null) {
            imageViewLogo.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    clearStoredUserData();
                    Toast.makeText(LoginActivity.this, "User data cleared!", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
    }

    private void performLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Basic validation
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

        // Show loading state
        buttonLogin.setText("Đang đăng nhập...");
        buttonLogin.setEnabled(false);

        // Create login request
        LoginRequest loginRequest = new LoginRequest(email, password);

        // TEMPORARY: Uncomment the line below to test with raw response
        // testLoginWithRawResponse(loginRequest);
        // return;

        // Call API
        Call<LoginResponse> call = apiService.login(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                // Reset button state
                buttonLogin.setText("Đăng nhập");
                buttonLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    // HTTP 200 - Success
                    LoginResponse loginResponse = response.body();
                    
                    // Debug log
                    android.util.Log.d("LoginActivity", "API Response - Success: " + loginResponse.isSuccess());
                    android.util.Log.d("LoginActivity", "API Response - Message: " + loginResponse.getMessage());
                    android.util.Log.d("LoginActivity", "API Response - User: " + (loginResponse.getUser() != null ? "Not null" : "Null"));
                    android.util.Log.d("LoginActivity", "API Response - AccessToken: " + (loginResponse.getAccessToken() != null ? "Not null" : "Null"));
                    
                    if (loginResponse.isSuccess()) {
                        User user = loginResponse.getUser();
                        
                        if (user != null) {
                            // Save user data to SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("user_token", loginResponse.getAccessToken());
                            editor.putString("refresh_token", loginResponse.getRefreshToken());
                            editor.putString("user_email", user.getEmail());
                            editor.putString("user_name", user.getUsername());
                            editor.putInt("user_id", user.getUserid());
                            editor.putString("user_phone", user.getPhonenumber());
                            editor.putInt("user_role", user.getRole());
                            editor.putString("user_role_name", user.getRoleName());
                            editor.putBoolean("is_logged_in", true);
                            editor.apply();

                            // Show success message
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                            // Navigate to main activity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        } else {
                            // User data is null but API says success
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công nhưng thiếu thông tin người dùng", Toast.LENGTH_SHORT).show();
                            
                            // Still navigate to main activity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }
                    } else {
                        // API returned success=false
                        String errorMessage = loginResponse.getMessage() != null ? 
                            loginResponse.getMessage() : "Đăng nhập thất bại";
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                } else if (response.isSuccessful() && response.body() == null) {
                    // HTTP 200 but no body - might be a different response format
                    android.util.Log.d("LoginActivity", "HTTP 200 but response body is null");
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to main activity anyway
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                } else {
                    // HTTP error (400, 401, 500, etc.)
                    String errorMessage = "Đăng nhập thất bại";
                    
                    if (response.code() == 401) {
                        errorMessage = "Email hoặc mật khẩu không đúng";
                    } else if (response.code() == 400) {
                        errorMessage = "Dữ liệu đăng nhập không hợp lệ";
                    } else if (response.code() == 500) {
                        errorMessage = "Lỗi server, vui lòng thử lại sau";
                    } else if (response.code() == 404) {
                        errorMessage = "Không tìm thấy tài khoản";
                    }
                    
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Reset button state
                buttonLogin.setText("Đăng nhập");
                buttonLogin.setEnabled(true);

                // Handle network error
                String errorMessage = "Lỗi kết nối mạng";
                if (t.getMessage() != null && t.getMessage().contains("timeout")) {
                    errorMessage = "Kết nối quá chậm, vui lòng thử lại";
                } else if (t.getMessage() != null && t.getMessage().contains("Unable to resolve host")) {
                    errorMessage = "Không thể kết nối đến server";
                }
                
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // Test method to debug API response format
    private void testLoginWithRawResponse(LoginRequest loginRequest) {
        Call<Object> call = apiService.loginRaw(loginRequest);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                android.util.Log.d("LoginActivity", "Raw Response Code: " + response.code());
                android.util.Log.d("LoginActivity", "Raw Response Body: " + response.body());
                
                if (response.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công (Raw)!", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to main activity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                android.util.Log.e("LoginActivity", "Raw Response Error: " + t.getMessage());
            }
        });
    }

}
