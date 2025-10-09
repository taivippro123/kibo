package com.example.kibo;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.kibo.ui.AccountFragment;
import com.example.kibo.ui.CartFragment;
import com.example.kibo.ui.HomeFragment;
import com.example.kibo.ui.OrdersFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.badge.BadgeDrawable;
import androidx.core.content.ContextCompat;
import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.CartItemsResponse;
import com.example.kibo.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.Intent;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private ApiService apiService;
    private SessionManager sessionManager;
    
    // Fragment instances for reuse
    private HomeFragment homeFragment;
    private OrdersFragment ordersFragment;
    private CartFragment cartFragment;
    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);
        apiService = ApiClient.getApiService();
        sessionManager = new SessionManager(this);
        
        // Initialize fragments
        initializeFragments();

        // Check if specific tab should be selected
        int selectedTab = getIntent().getIntExtra("selected_tab", 0);
        
        // Mở mặc định trang Home hoặc tab được chỉ định
        Fragment initialFragment = getFragmentByTab(selectedTab);
        loadFragment(initialFragment);
        setSelectedTab(selectedTab);

        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selected = null;
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    selected = homeFragment;
                } else if (id == R.id.nav_orders) {
                    selected = ordersFragment;
                } else if (id == R.id.nav_cart) {
                    selected = cartFragment;
                } else if (id == R.id.nav_account) {
                    selected = accountFragment;
                }

                if (selected != null) {
                    loadFragment(selected);
                    return true;
                }

                return false;
            }
        });

        // Refresh cart badge immediately on launch (e.g., right after login)
        refreshCartBadge();
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }

    // Badge helpers for cart count
    public void updateCartBadge(int count) {
        if (bottomNav == null) return;
        BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_cart);
        if (count > 0) {
            badge.setVisible(true);
            badge.setNumber(count);
            badge.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_color));
            badge.setBadgeTextColor(ContextCompat.getColor(this, android.R.color.white));
        } else {
            badge.clearNumber();
            badge.setVisible(false);
        }
    }

    public void refreshCartBadge() {
        if (sessionManager == null) sessionManager = new SessionManager(this);
        if (apiService == null) apiService = ApiClient.getApiService();
        
        if (!sessionManager.isLoggedIn() || !sessionManager.hasActiveCart()) {
            updateCartBadge(0);
            return;
        }
        int cartId = sessionManager.getActiveCartId();
        apiService.getCartItems(cartId).enqueue(new Callback<CartItemsResponse>() {
            @Override
            public void onResponse(Call<CartItemsResponse> call, Response<CartItemsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    updateCartBadge(response.body().getData().size());
                } else {
                    updateCartBadge(0);
                }
            }

            @Override
            public void onFailure(Call<CartItemsResponse> call, Throwable t) {
                updateCartBadge(0);
            }
        });
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent == null || bottomNav == null) return;
        int selectedTab = intent.getIntExtra("selected_tab", -1);
        if (selectedTab == -1) return;
        
        Fragment target = getFragmentByTab(selectedTab);
        if (target != null) {
            loadFragment(target);
            setSelectedTab(selectedTab);
        }
    }
    
    // ============ Helper Methods ============
    
    private void initializeFragments() {
        homeFragment = new HomeFragment();
        ordersFragment = new OrdersFragment();
        cartFragment = new CartFragment();
        accountFragment = new AccountFragment();
    }
    
    private Fragment getFragmentByTab(int tabIndex) {
        switch (tabIndex) {
            case 0: return homeFragment;
            case 1: return ordersFragment;
            case 2: return cartFragment;
            case 3: return accountFragment;
            default: return homeFragment;
        }
    }
    
    private void setSelectedTab(int tabIndex) {
        switch (tabIndex) {
            case 0: bottomNav.setSelectedItemId(R.id.nav_home); break;
            case 1: bottomNav.setSelectedItemId(R.id.nav_orders); break;
            case 2: bottomNav.setSelectedItemId(R.id.nav_cart); break;
            case 3: bottomNav.setSelectedItemId(R.id.nav_account); break;
            default: bottomNav.setSelectedItemId(R.id.nav_home); break;
        }
    }
    
    // ============ Logout Functionality ============
    
    /**
     * Perform logout with API call and navigate to login
     */
    public void performLogout() {
        if (sessionManager == null) sessionManager = new SessionManager(this);
        if (apiService == null) apiService = ApiClient.getApiService();
        
        sessionManager.logout(apiService, new SessionManager.LogoutCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                });
            }
            
            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                });
            }
        });
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    // ============ Session Management ============
    
    /**
     * Check if user is still logged in
     */
    public boolean isUserLoggedIn() {
        return sessionManager != null && sessionManager.isLoggedIn();
    }
    
    /**
     * Handle session expired
     */
    public void handleSessionExpired() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        });
    }
}
