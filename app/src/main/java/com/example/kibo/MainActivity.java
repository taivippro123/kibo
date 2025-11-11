package com.example.kibo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.CartItemsResponse;
import com.example.kibo.ui.AccountFragment;
import com.example.kibo.ui.CartFragment;
import com.example.kibo.ui.HomeFragment;
import com.example.kibo.ui.OrdersFragment;
import com.example.kibo.utils.SessionManager;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private SwipeRefreshLayout swipeRefreshLayout;
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

        // Init views
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        bottomNav = findViewById(R.id.bottom_navigation);

        // Init services
        apiService = ApiClient.getApiService();
        sessionManager = new SessionManager(this);

        // Initialize fragments
        initializeFragments();

        // Load initial tab
        int selectedTab = getIntent().getIntExtra("selected_tab", 0);
        Fragment initialFragment = getFragmentByTab(selectedTab);
        loadFragment(initialFragment);
        setSelectedTab(selectedTab);

        // Bottom navigation listener
        bottomNav.setOnItemSelectedListener(this::onBottomNavSelected);

        // Pull-to-refresh listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_container);
            if (currentFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .detach(currentFragment)
                        .commitNow();

                getSupportFragmentManager()
                        .beginTransaction()
                        .attach(currentFragment)
                        .commitNow();
            }

            // Refresh cart badge
            refreshCartBadge();

            // Kết thúc hiệu ứng refresh sau 1s
            swipeRefreshLayout.postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 1000);
        });


        // Initial badge refresh
        refreshCartBadge();
    }

    /** Handle bottom navigation clicks */
    private boolean onBottomNavSelected(@NonNull MenuItem item) {
        Fragment selected = null;
        int id = item.getItemId();

        if (id == R.id.nav_home) selected = homeFragment;
        else if (id == R.id.nav_orders) selected = ordersFragment;
        else if (id == R.id.nav_cart) selected = cartFragment;
        else if (id == R.id.nav_account) selected = accountFragment;

        if (selected != null) {
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.frame_container);
            if (current != null && current.getClass().equals(selected.getClass())) {
                return true; // already showing
            }
            loadFragment(selected);
            return true;
        }
        return false;
    }

    /** Load a fragment into container */
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }

    /** Update cart badge number */
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

    /** Fetch and update cart badge */
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
    protected void onNewIntent(Intent intent) {
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
            case 1: return ordersFragment;
            case 2: return cartFragment;
            case 3: return accountFragment;
            default: return homeFragment;
        }
    }

    private void setSelectedTab(int tabIndex) {
        switch (tabIndex) {
            case 1: bottomNav.setSelectedItemId(R.id.nav_orders); break;
            case 2: bottomNav.setSelectedItemId(R.id.nav_cart); break;
            case 3: bottomNav.setSelectedItemId(R.id.nav_account); break;
            default: bottomNav.setSelectedItemId(R.id.nav_home); break;
        }
    }

    // ============ Logout Functionality ============
    public void performLogout() {
        if (sessionManager == null) sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiServiceWithAuth(this);

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
    public boolean isUserLoggedIn() {
        return sessionManager != null && sessionManager.isLoggedIn();
    }

    public void handleSessionExpired() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        });
    }
}
