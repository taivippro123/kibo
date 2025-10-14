package com.example.kibo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.kibo.R;
import com.example.kibo.ui.AdminDashboardFragment;
import com.example.kibo.ui.AdminProductsFragment;
import com.example.kibo.ui.AdminAccountFragment;
import com.example.kibo.ui.AdminMessagesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.utils.SessionManager;

public class AdminMainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private ApiService apiService;
    private SessionManager sessionManager;
    
    // Fragment instances for reuse
    private AdminDashboardFragment dashboardFragment;
    private AdminProductsFragment productsFragment;
    private AdminAccountFragment accountFragment;
    private AdminMessagesFragment messagesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        // Initialize services
        apiService = ApiClient.getApiService();
        sessionManager = new SessionManager(this);
        
        // Initialize fragments
        initializeFragments();
        
        setupBottomNavigation();
        loadInitialFragment();
        
        // Handle intent extras for navigation
        handleIntentExtras();
    }

    private void setupBottomNavigation() {
        bottomNav = findViewById(R.id.admin_bottom_nav);
        bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private void loadInitialFragment() {
        // Mở Dashboard mặc định
        loadFragment(dashboardFragment);
        bottomNav.setSelectedItemId(R.id.nav_admin_dashboard);
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selected = null;
        int id = item.getItemId();

        if (id == R.id.nav_admin_dashboard) {
            selected = dashboardFragment;
        } else if (id == R.id.nav_admin_products) {
            // Mở AdminManagementActivity với TabLayout
            Intent intent = new Intent(this, AdminManagementActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_admin_messages) {
            selected = messagesFragment;
        } else if (id == R.id.nav_admin_account) {
            selected = accountFragment;
        }

        if (selected != null) {
            loadFragment(selected);
            return true;
        }

        return false;
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.admin_container, fragment)
                .commit();
    }
    
    // ============ Helper Methods ============
    
    private void initializeFragments() {
        dashboardFragment = new AdminDashboardFragment();
        productsFragment = new AdminProductsFragment();
        accountFragment = new AdminAccountFragment();
        messagesFragment = new AdminMessagesFragment();
    }
    
    // ============ Logout Functionality ============
    
    /**
     * Perform logout with API call and navigate to login
     */
    public void performLogout() {
        if (sessionManager == null) sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiServiceWithAuth(this);
        
        sessionManager.logout(apiService, new SessionManager.LogoutCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(AdminMainActivity.this, "Admin đã đăng xuất thành công", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                });
            }
            
            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(AdminMainActivity.this, "Admin đã đăng xuất", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                });
            }
        });
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(AdminMainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    // ============ Session Management ============
    
    /**
     * Check if admin is still logged in
     */
    public boolean isAdminLoggedIn() {
        return sessionManager != null && sessionManager.isLoggedIn() && sessionManager.getUserRole() == 0;
    }
    
    /**
     * Handle session expired
     */
    public void handleSessionExpired() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Phiên admin đã hết hạn", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        });
    }
    
    private void handleIntentExtras() {
        String fragment = getIntent().getStringExtra("fragment");
        if (fragment != null) {
            switch (fragment) {
                case "dashboard":
                    loadFragment(dashboardFragment);
                    bottomNav.setSelectedItemId(R.id.nav_admin_dashboard);
                    break;
                case "messages":
                    loadFragment(messagesFragment);
                    bottomNav.setSelectedItemId(R.id.nav_admin_messages);
                    break;
                case "account":
                    loadFragment(accountFragment);
                    bottomNav.setSelectedItemId(R.id.nav_admin_account);
                    break;
            }
        }
    }
}