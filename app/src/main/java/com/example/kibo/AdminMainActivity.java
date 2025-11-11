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
        
        // Handle intent extras for navigation, nếu không có thì load default
        if (!handleIntentExtras()) {
            loadInitialFragment();
        }
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Quan trọng: cập nhật Intent mới để handleIntentExtras() có thể đọc được
        // Xử lý Intent mới khi Activity đã tồn tại trong stack
        if (!isFinishing() && !isDestroyed()) {
            handleIntentExtras();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Chỉ set selected item nếu thực sự cần thiết và không trigger listener
        // Tránh set lại nếu đã đúng để không gây giật
        String fragment = getIntent().getStringExtra("fragment");
        if (fragment != null && bottomNav != null) {
            int targetItemId = -1;
            switch (fragment) {
                case "orders":
                    targetItemId = R.id.nav_admin_orders;
                    break;
                case "dashboard":
                    targetItemId = R.id.nav_admin_dashboard;
                    break;
                case "messages":
                    targetItemId = R.id.nav_admin_messages;
                    break;
                case "account":
                    targetItemId = R.id.nav_admin_account;
                    break;
            }
            
            // Chỉ set nếu item hiện tại khác với item cần set
            if (targetItemId != -1 && bottomNav.getSelectedItemId() != targetItemId) {
                // Tạm thời remove listener để tránh trigger khi set selected item
                bottomNav.setOnItemSelectedListener(null);
                bottomNav.setSelectedItemId(targetItemId);
                // Restore listener sau khi set xong
                bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected);
            }
        }
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
        } else if (id == R.id.nav_admin_orders) {
            // Use user OrdersFragment for admin view (shared UI)
            selected = new com.example.kibo.ui.OrdersFragment();
            setToolbarTitle("Quản lý đơn hàng");
        } else if (id == R.id.nav_admin_messages) {
            // Mở AdminChatListActivity
            Intent intent = new Intent(this, AdminChatListActivity.class);
            startActivity(intent);
            return true;
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
        if (fragment != null && !isFinishing() && !isDestroyed()) {
            try {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.admin_container, fragment)
                        .commitAllowingStateLoss(); // commitAllowingStateLoss() để tránh exception khi Activity đang trong quá trình lifecycle
            } catch (Exception e) {
                e.printStackTrace();
                // Nếu commit() fail, thử commitNow() nhưng phải trong UI thread
                if (!isFinishing() && !isDestroyed()) {
                    try {
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.admin_container, fragment)
                                .commit();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }
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

    // Public helper for fragments to update the toolbar title in admin flow
    public void setToolbarTitle(String title) {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.admin_toolbar);
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
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
    
    private boolean handleIntentExtras() {
        String fragment = getIntent().getStringExtra("fragment");
        if (fragment != null) {
            // Tạm thời remove listener để tránh trigger khi set selected item
            if (bottomNav != null) {
                bottomNav.setOnItemSelectedListener(null);
            }
            
            switch (fragment) {
                case "dashboard":
                    loadFragment(dashboardFragment);
                    if (bottomNav != null) {
                        bottomNav.setSelectedItemId(R.id.nav_admin_dashboard);
                    }
                    break;
                case "orders":
                    // Load OrdersFragment
                    Fragment ordersFragment = new com.example.kibo.ui.OrdersFragment();
                    loadFragment(ordersFragment);
                    setToolbarTitle("Quản lý đơn hàng");
                    if (bottomNav != null) {
                        bottomNav.setSelectedItemId(R.id.nav_admin_orders);
                    }
                    break;
                case "messages":
                    loadFragment(messagesFragment);
                    if (bottomNav != null) {
                        bottomNav.setSelectedItemId(R.id.nav_admin_messages);
                    }
                    break;
                case "account":
                    loadFragment(accountFragment);
                    if (bottomNav != null) {
                        bottomNav.setSelectedItemId(R.id.nav_admin_account);
                    }
                    break;
            }
            
            // Restore listener sau khi set xong
            if (bottomNav != null) {
                bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected);
            }
            return true;
        }
        return false;
    }
}