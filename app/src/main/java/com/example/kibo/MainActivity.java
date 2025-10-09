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

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);
        apiService = ApiClient.getApiService();
        sessionManager = new SessionManager(this);

        // Check if specific tab should be selected
        int selectedTab = getIntent().getIntExtra("selected_tab", 0);
        
        // Mở mặc định trang Home hoặc tab được chỉ định
        Fragment initialFragment = new HomeFragment();
        if (selectedTab == 1) {
            initialFragment = new OrdersFragment();
        } else if (selectedTab == 2) {
            initialFragment = new CartFragment();
        } else if (selectedTab == 3) {
            initialFragment = new AccountFragment();
        }
        
        loadFragment(initialFragment);
        
        // Set selected tab in bottom navigation
        if (selectedTab == 1) {
            bottomNav.setSelectedItemId(R.id.nav_orders);
        } else if (selectedTab == 2) {
            bottomNav.setSelectedItemId(R.id.nav_cart);
        } else if (selectedTab == 3) {
            bottomNav.setSelectedItemId(R.id.nav_account);
        } else {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selected = null;
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    selected = new HomeFragment();
                } else if (id == R.id.nav_orders) {
                    selected = new OrdersFragment();
                } else if (id == R.id.nav_cart) {
                    selected = new CartFragment();
                } else if (id == R.id.nav_account) {
                    selected = new AccountFragment();
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
        Fragment target = null;
        if (selectedTab == 0) {
            target = new com.example.kibo.ui.HomeFragment();
            bottomNav.setSelectedItemId(R.id.nav_home);
        } else if (selectedTab == 1) {
            target = new com.example.kibo.ui.OrdersFragment();
            bottomNav.setSelectedItemId(R.id.nav_orders);
        } else if (selectedTab == 2) {
            target = new com.example.kibo.ui.CartFragment();
            bottomNav.setSelectedItemId(R.id.nav_cart);
        } else if (selectedTab == 3) {
            target = new com.example.kibo.ui.AccountFragment();
            bottomNav.setSelectedItemId(R.id.nav_account);
        }
        if (target != null) {
            loadFragment(target);
        }
    }
}
