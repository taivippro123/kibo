package com.example.kibo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.FullAddressResponse;
import com.example.kibo.models.User;
import com.example.kibo.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonalInfoActivity extends AppCompatActivity {
    
    private SessionManager sessionManager;
    private ApiService apiService;
    
    // Views
    private ImageView buttonBack;
    private ImageView buttonEdit;
    private TextView textViewName;
    private TextView textViewEmail;
    private TextView textViewPhone;
    private TextView textViewFullAddress;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);
        
        // Initialize SessionManager and ApiService
        sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiService();
        
        // Initialize views
        initViews();
        
        // Load user data
        loadUserData();
        
        // Setup back button
        setupBackButton();
    }
    
    private void initViews() {
        buttonBack = findViewById(R.id.button_back);
        buttonEdit = findViewById(R.id.button_edit);
        textViewName = findViewById(R.id.text_view_name);
        textViewEmail = findViewById(R.id.text_view_email);
        textViewPhone = findViewById(R.id.text_view_phone);
        textViewFullAddress = findViewById(R.id.text_view_full_address);
    }
    
    private void loadUserData() {
        // Get user from SessionManager
        User user = sessionManager.getUser();
        
        if (user != null) {
            // Basic Information - if null or empty, leave blank
            String userName = user.getUsername();
            textViewName.setText(userName != null && !userName.isEmpty() ? userName : "");
            
            String email = user.getEmail();
            textViewEmail.setText(email != null && !email.isEmpty() ? email : "");
            
            String phone = user.getPhonenumber();
            textViewPhone.setText(phone != null && !phone.isEmpty() ? phone : "");
            
            // Load full address if address info exists
            int provinceId = user.getProvinceid();
            int districtId = user.getDistrictid();
            int wardId = user.getWardid();
            String address = user.getAddress();
            
            if (provinceId > 0 && districtId > 0 && wardId > 0) {
                // Load full address from API
                loadFullAddress(provinceId, districtId, String.valueOf(wardId), address);
            } else {
                // No address info, show address only or empty
                textViewFullAddress.setText(address != null && !address.isEmpty() ? address : "");
            }
        }
    }
    
    private void loadFullAddress(int provinceId, int districtId, String wardCode, final String address) {
        Call<FullAddressResponse> call = apiService.getFullAddress(provinceId, districtId, wardCode);
        call.enqueue(new Callback<FullAddressResponse>() {
            @Override
            public void onResponse(Call<FullAddressResponse> call, Response<FullAddressResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FullAddressResponse fullAddress = response.body();
                    String fullAddressText = fullAddress.getFullAddressText();
                    
                    // Ghép address + fullAddressText
                    String completeAddress;
                    if (address != null && !address.isEmpty()) {
                        completeAddress = address + ", " + fullAddressText;
                    } else {
                        completeAddress = fullAddressText;
                    }
                    
                    textViewFullAddress.setText(completeAddress);
                } else {
                    // API failed, show address only
                    textViewFullAddress.setText(address != null && !address.isEmpty() ? address : "");
                }
            }
            
            @Override
            public void onFailure(Call<FullAddressResponse> call, Throwable t) {
                // Network error, show address only
                textViewFullAddress.setText(address != null && !address.isEmpty() ? address : "");
            }
        });
    }
    
    private void setupBackButton() {
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PersonalInfoActivity.this, EditPersonalInfoActivity.class);
                startActivity(intent);
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload user data when returning from edit screen
        loadUserData();
    }
}

