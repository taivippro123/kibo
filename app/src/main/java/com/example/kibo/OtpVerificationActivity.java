package com.example.kibo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.ApiResponse;
import com.example.kibo.models.LoginResponse;
import com.example.kibo.models.OtpRequest;
import com.example.kibo.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpVerificationActivity extends AppCompatActivity {

    private EditText editTextHiddenOtp;
    private TextView textViewOtp1, textViewOtp2, textViewOtp3, textViewOtp4, textViewOtp5, textViewOtp6;
    private ImageView cursorOtp1, cursorOtp2, cursorOtp3, cursorOtp4, cursorOtp5, cursorOtp6;
    private LinearLayout otpContainer;
    private Button buttonVerify;
    private TextView textViewTimer, textViewResend, textViewEmailDisplay;
    private CountDownTimer countDownTimer;
    private Animation cursorBlinkAnimation;
    private String userEmail;
    private String userOtp = ""; // This would normally come from your backend
    
    private ApiService apiService;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Get email from intent
        userEmail = getIntent().getStringExtra("email");
        if (userEmail == null) {
            userEmail = "example@email.com";
        }

        // Initialize API service and SharedPreferences
        apiService = ApiClient.getApiService();
        sharedPreferences = getSharedPreferences("KiboPrefs", MODE_PRIVATE);

        // Initialize views
        initViews();
        
        // Setup animations
        setupAnimations();
        
        // Setup OTP input handling
        setupOtpInputHandling();
        
        // Setup click listeners
        setupClickListeners();
        
        // Setup back button handling
        setupBackButton();
        
        // Start timer
        startCountDownTimer();
        
        // Auto focus on hidden input and show initial cursor
        editTextHiddenOtp.post(new Runnable() {
            @Override
            public void run() {
                editTextHiddenOtp.requestFocus();
                // Show cursor at position 0 (first digit)
                showCursorAtPosition(0);
            }
        });
    }

    private void initViews() {
        editTextHiddenOtp = findViewById(R.id.edit_text_hidden_otp);
        textViewOtp1 = findViewById(R.id.text_view_otp_1);
        textViewOtp2 = findViewById(R.id.text_view_otp_2);
        textViewOtp3 = findViewById(R.id.text_view_otp_3);
        textViewOtp4 = findViewById(R.id.text_view_otp_4);
        textViewOtp5 = findViewById(R.id.text_view_otp_5);
        textViewOtp6 = findViewById(R.id.text_view_otp_6);
        
        cursorOtp1 = findViewById(R.id.cursor_otp_1);
        cursorOtp2 = findViewById(R.id.cursor_otp_2);
        cursorOtp3 = findViewById(R.id.cursor_otp_3);
        cursorOtp4 = findViewById(R.id.cursor_otp_4);
        cursorOtp5 = findViewById(R.id.cursor_otp_5);
        cursorOtp6 = findViewById(R.id.cursor_otp_6);
        
        otpContainer = findViewById(R.id.otp_container);
        
        buttonVerify = findViewById(R.id.button_verify);
        textViewTimer = findViewById(R.id.text_view_timer);
        textViewResend = findViewById(R.id.text_view_resend);
        textViewEmailDisplay = findViewById(R.id.text_view_email_display);
        
        // Set email display
        textViewEmailDisplay.setText(userEmail);
        
        // Load cursor animation
        cursorBlinkAnimation = AnimationUtils.loadAnimation(this, R.anim.cursor_blink);
        
        // Ensure animation is properly configured
        if (cursorBlinkAnimation != null) {
            cursorBlinkAnimation.setRepeatCount(Animation.INFINITE);
            cursorBlinkAnimation.setRepeatMode(Animation.REVERSE);
        }
    }

    private void setupAnimations() {
        // Load animations
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        // Apply animations with delays
        findViewById(R.id.text_view_otp_title).startAnimation(fadeInAnimation);
        
        textViewOtp1.startAnimation(slideUpAnimation);
        textViewOtp2.startAnimation(slideUpAnimation);
        textViewOtp3.startAnimation(slideUpAnimation);
        textViewOtp4.startAnimation(slideUpAnimation);
        textViewOtp5.startAnimation(slideUpAnimation);
        textViewOtp6.startAnimation(slideUpAnimation);
        
        buttonVerify.startAnimation(slideUpAnimation);
    }

    private void setupOtpInputHandling() {
        // Setup click listener for OTP container to focus hidden input
        otpContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextHiddenOtp.requestFocus();
            }
        });

        // Add text watcher to hidden OTP input
        editTextHiddenOtp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateOtpDisplay(s.toString());
                checkOtpComplete();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClickListeners() {
        buttonVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyOtp();
            }
        });

        textViewResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendOtp();
            }
        });

        findViewById(R.id.text_view_back_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to register activity
                Intent intent = new Intent(OtpVerificationActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    private void setupBackButton() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Navigate back to register activity
                Intent intent = new Intent(OtpVerificationActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void startCountDownTimer() {
        countDownTimer = new CountDownTimer(120000, 1000) { // 2 minutes
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                long minutes = seconds / 60;
                seconds = seconds % 60;
                
                String timeString = String.format("Gửi lại mã sau %02d:%02d", minutes, seconds);
                textViewTimer.setText(timeString);
            }

            @Override
            public void onFinish() {
                textViewTimer.setVisibility(View.GONE);
                textViewResend.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void verifyOtp() {
        String enteredOtp = editTextHiddenOtp.getText().toString();

        if (enteredOtp.length() != 6) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ 6 số OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        buttonVerify.setText("Đang xác thực...");
        buttonVerify.setEnabled(false);

        // Create OTP request
        OtpRequest otpRequest = new OtpRequest(userEmail, enteredOtp);

        // Call API
        Call<LoginResponse> call = apiService.verifyOtp(otpRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                // Reset button state
                buttonVerify.setText("Xác thực");
                buttonVerify.setEnabled(false);

                System.out.println("DEBUG: onResponse called");
                System.out.println("DEBUG: response.isSuccessful() = " + response.isSuccessful());
                System.out.println("DEBUG: response.body() != null = " + (response.body() != null));

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    
                    System.out.println("DEBUG: loginResponse.isSuccess() = " + loginResponse.isSuccess());
                    System.out.println("DEBUG: loginResponse.getUser() != null = " + (loginResponse.getUser() != null));
                    
                    if (loginResponse.isSuccess() && loginResponse.getUser() != null) {
                        System.out.println("DEBUG: Entering success block");
                        System.out.println("DEBUG: AccessToken = " + loginResponse.getAccessToken());
                        System.out.println("DEBUG: RefreshToken = " + loginResponse.getRefreshToken());
                        System.out.println("DEBUG: User email = " + loginResponse.getUser().getEmail());
                        User user = loginResponse.getUser();
                        
                        // Save user data to SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        if (loginResponse.getAccessToken() != null) {
                            editor.putString("user_token", loginResponse.getAccessToken());
                        }
                        if (loginResponse.getRefreshToken() != null) {
                            editor.putString("refresh_token", loginResponse.getRefreshToken());
                        }
                        editor.putString("user_email", user.getEmail());
                        editor.putString("user_name", user.getUsername());
                        editor.putInt("user_id", user.getUserid());
                        editor.putString("user_phone", user.getPhonenumber());
                        editor.putInt("user_role", user.getRole());
                        editor.putString("user_role_name", user.getRoleName());
                        editor.putBoolean("is_logged_in", true);
                        editor.apply();

                        // Show success message
                        Toast.makeText(OtpVerificationActivity.this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();

                        // Debug log
                        System.out.println("DEBUG: OTP verification successful, navigating to MainActivity");
                        
                        // Navigate to main activity with delay
                        new android.os.Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    System.out.println("DEBUG: About to create Intent for MainActivity");
                                    Intent intent = new Intent(OtpVerificationActivity.this, MainActivity.class);
                                    System.out.println("DEBUG: Intent created successfully");
                                    
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    System.out.println("DEBUG: Intent flags set");
                                    
                                    startActivity(intent);
                                    System.out.println("DEBUG: startActivity called");
                                    
                                    finish();
                                    System.out.println("DEBUG: finish() called - Navigation to MainActivity completed");
                                } catch (Exception e) {
                                    System.out.println("DEBUG: Error navigating to MainActivity: " + e.getMessage());
                                    e.printStackTrace();
                                    // Fallback to LoginActivity if MainActivity fails
                                    Intent fallbackIntent = new Intent(OtpVerificationActivity.this, LoginActivity.class);
                                    startActivity(fallbackIntent);
                                    finish();
                                }
                            }
                        }, 1500); // 1.5 second delay
                    } else {
                        // Handle API error response
                        String errorMessage = loginResponse.getMessage() != null ? 
                            loginResponse.getMessage() : "Mã OTP không hợp lệ";
                        Toast.makeText(OtpVerificationActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        buttonVerify.setEnabled(true);
                    }
                } else {
                    // HTTP error (400, 401, 500, etc.)
                    String errorMessage = "Xác thực thất bại";
                    
                    if (response.code() == 400) {
                        errorMessage = "Mã OTP không hợp lệ";
                    } else if (response.code() == 401) {
                        errorMessage = "Mã OTP đã hết hạn";
                    } else if (response.code() == 500) {
                        errorMessage = "Lỗi server, vui lòng thử lại sau";
                    }
                    
                    Toast.makeText(OtpVerificationActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    buttonVerify.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Reset button state
                buttonVerify.setText("Xác thực");
                buttonVerify.setEnabled(true);

                // Handle network error
                String errorMessage = "Lỗi kết nối mạng";
                if (t.getMessage() != null && t.getMessage().contains("timeout")) {
                    errorMessage = "Kết nối quá chậm, vui lòng thử lại";
                } else if (t.getMessage() != null && t.getMessage().contains("Unable to resolve host")) {
                    errorMessage = "Không thể kết nối đến server";
                }
                
                Toast.makeText(OtpVerificationActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOtpDisplay(String otp) {
        // Clear all text views first
        textViewOtp1.setText("");
        textViewOtp2.setText("");
        textViewOtp3.setText("");
        textViewOtp4.setText("");
        textViewOtp5.setText("");
        textViewOtp6.setText("");

        // Hide all cursors first
        hideAllCursors();

        // Update text views based on input length
        if (otp.length() >= 1) textViewOtp1.setText(String.valueOf(otp.charAt(0)));
        if (otp.length() >= 2) textViewOtp2.setText(String.valueOf(otp.charAt(1)));
        if (otp.length() >= 3) textViewOtp3.setText(String.valueOf(otp.charAt(2)));
        if (otp.length() >= 4) textViewOtp4.setText(String.valueOf(otp.charAt(3)));
        if (otp.length() >= 5) textViewOtp5.setText(String.valueOf(otp.charAt(4)));
        if (otp.length() >= 6) textViewOtp6.setText(String.valueOf(otp.charAt(5)));

        // Show cursor at current position (next empty position)
        // If OTP is empty, show cursor at position 0
        int cursorPosition = otp.isEmpty() ? 0 : otp.length();
        showCursorAtPosition(cursorPosition);
    }

    private void hideAllCursors() {
        cursorOtp1.setVisibility(View.GONE);
        cursorOtp2.setVisibility(View.GONE);
        cursorOtp3.setVisibility(View.GONE);
        cursorOtp4.setVisibility(View.GONE);
        cursorOtp5.setVisibility(View.GONE);
        cursorOtp6.setVisibility(View.GONE);
        
        // Stop all cursor animations
        cursorOtp1.clearAnimation();
        cursorOtp2.clearAnimation();
        cursorOtp3.clearAnimation();
        cursorOtp4.clearAnimation();
        cursorOtp5.clearAnimation();
        cursorOtp6.clearAnimation();
    }

    private void showCursorAtPosition(int position) {
        // Don't show cursor if position is 6 (all digits filled)
        if (position >= 6) {
            return;
        }
        
        ImageView cursor = null;
        
        switch (position) {
            case 0:
                cursor = cursorOtp1;
                break;
            case 1:
                cursor = cursorOtp2;
                break;
            case 2:
                cursor = cursorOtp3;
                break;
            case 3:
                cursor = cursorOtp4;
                break;
            case 4:
                cursor = cursorOtp5;
                break;
            case 5:
                cursor = cursorOtp6;
                break;
        }
        
        if (cursor != null) {
            cursor.setVisibility(View.VISIBLE);
            // Clear any existing animation first
            cursor.clearAnimation();
            // Start the blinking animation
            cursor.startAnimation(cursorBlinkAnimation);
        }
    }

    private void resendOtp() {
        // Reset timer
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        
        // Clear OTP input
        editTextHiddenOtp.setText("");
        updateOtpDisplay(""); // This will also hide all cursors and show cursor at position 0
        
        // Focus on hidden input
        editTextHiddenOtp.requestFocus();
        
        // Hide resend button and show timer
        textViewResend.setVisibility(View.GONE);
        textViewTimer.setVisibility(View.VISIBLE);
        
        // Call API to resend OTP
        Call<ApiResponse<String>> call = apiService.resendOtp(userEmail);
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess()) {
                        // Start new timer
                        startCountDownTimer();
                        
                        // Show success message
                        Toast.makeText(OtpVerificationActivity.this, "Mã OTP mới đã được gửi", Toast.LENGTH_SHORT).show();
                    } else {
                        // Handle API error response
                        String errorMessage = apiResponse.getMessage() != null ? 
                            apiResponse.getMessage() : "Không thể gửi lại mã OTP";
                        Toast.makeText(OtpVerificationActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        
                        // Show resend button again
                        textViewResend.setVisibility(View.VISIBLE);
                        textViewTimer.setVisibility(View.GONE);
                    }
                } else {
                    // HTTP error (400, 401, 500, etc.)
                    String errorMessage = "Không thể gửi lại mã OTP";
                    
                    if (response.code() == 400) {
                        errorMessage = "Email không hợp lệ";
                    } else if (response.code() == 429) {
                        errorMessage = "Vui lòng đợi trước khi gửi lại";
                    } else if (response.code() == 500) {
                        errorMessage = "Lỗi server, vui lòng thử lại sau";
                    }
                    
                    Toast.makeText(OtpVerificationActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    
                    // Show resend button again
                    textViewResend.setVisibility(View.VISIBLE);
                    textViewTimer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                // Handle network error
                String errorMessage = "Lỗi kết nối mạng";
                if (t.getMessage() != null && t.getMessage().contains("timeout")) {
                    errorMessage = "Kết nối quá chậm, vui lòng thử lại";
                } else if (t.getMessage() != null && t.getMessage().contains("Unable to resolve host")) {
                    errorMessage = "Không thể kết nối đến server";
                }
                
                Toast.makeText(OtpVerificationActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                
                // Show resend button again
                textViewResend.setVisibility(View.VISIBLE);
                textViewTimer.setVisibility(View.GONE);
            }
        });
    }

    private void checkOtpComplete() {
        String enteredOtp = editTextHiddenOtp.getText().toString();

        if (enteredOtp.length() == 6) {
            buttonVerify.setEnabled(true);
        } else {
            buttonVerify.setEnabled(false);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
