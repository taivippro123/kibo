package com.example.kibo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.CartItemsResponse;
import com.example.kibo.notifications.NotificationHelper;
import com.example.kibo.ui.AccountFragment;
import com.example.kibo.ui.CartFragment;
import com.example.kibo.ui.HomeFragment;
import com.example.kibo.ui.OrdersFragment;
import com.example.kibo.utils.SessionManager;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_POST_NOTIFICATIONS = 1001;

    private BottomNavigationView bottomNav;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ApiService apiService;
    private SessionManager sessionManager;

    // Fragments
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

        // Handle bottom nav
        bottomNav.setOnItemSelectedListener(this::onBottomNavSelected);

        // Pull-to-refresh listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_container);
            if (currentFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .detach(currentFragment)
                        .commitNow();
                getSupportFragmentManager().beginTransaction()
                        .attach(currentFragment)
                        .commitNow();
            }

            refreshCartBadge();
            swipeRefreshLayout.postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 1000);
        });

        // Request notification permission first
        requestNotificationPermission();

        // Initial badge refresh and show notification if has items
        bottomNav.post(() -> refreshCartBadge(true)); // Show notification on first launch

        // Schedule background badge updater
        scheduleCartBadgeWorker();
    }

    /**
     * Request POST_NOTIFICATIONS permission for Android 13+
     * Required for both notifications and badge to work
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                // Check if we should show rationale
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.POST_NOTIFICATIONS)) {
                    // User has denied permission before, show explanation
                    Toast.makeText(this,
                            "Cần quyền thông báo để hiển thị badge giỏ hàng và nhận thông báo",
                            Toast.LENGTH_LONG).show();
                }

                // Request permission
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.POST_NOTIFICATIONS },
                        REQUEST_POST_NOTIFICATIONS);
            } else {
                // Permission already granted
                android.util.Log.d("MainActivity", "POST_NOTIFICATIONS permission already granted");
            }
        } else {
            // Below Android 13, no runtime permission needed
            android.util.Log.d("MainActivity", "Android version < 13, no POST_NOTIFICATIONS permission needed");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted - refresh badge and show notification
                android.util.Log.d("MainActivity", "POST_NOTIFICATIONS permission granted");
                Toast.makeText(this, "Đã bật thông báo thành công", Toast.LENGTH_SHORT).show();
                refreshCartBadge(true);
            } else {
                // Permission denied
                android.util.Log.d("MainActivity", "POST_NOTIFICATIONS permission denied");

                // Check if user selected "Don't ask again"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.POST_NOTIFICATIONS)) {
                        // User selected "Don't ask again" - show instruction to go to settings
                        Toast.makeText(this,
                                "Vui lòng bật quyền thông báo trong Cài đặt > Ứng dụng > Kibo > Quyền",
                                Toast.LENGTH_LONG).show();
                    } else {
                        // User just denied, can ask again later
                        Toast.makeText(this,
                                "Badge giỏ hàng có thể không hiển thị do thiếu quyền thông báo",
                                Toast.LENGTH_LONG).show();
                    }
                }

                // Still try to refresh badge (ShortcutBadger might work without permission)
                refreshCartBadge(false);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh badge every time user returns to MainActivity
        // Show notification reminder when coming back to app
        refreshCartBadge(true);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }

    /** Cập nhật badge giỏ hàng */
    public void updateCartBadge(int count) {
        if (bottomNav == null)
            return;

        bottomNav.removeBadge(R.id.nav_cart);

        if (count > 0) {
            BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_cart);
            badge.setVisible(true);
            badge.setNumber(count);
            badge.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_color));
            badge.setBadgeTextColor(ContextCompat.getColor(this, android.R.color.white));
            badge.setMaxCharacterCount(3);
        }

        // Update app icon badge (no notification, just badge)
        NotificationHelper.updateCartBadge(this, count);
    }

    /** Lấy dữ liệu giỏ hàng và cập nhật badge */
    public void refreshCartBadge() {
        refreshCartBadge(false); // Default: don't show notification
    }

    /** Lấy dữ liệu giỏ hàng và cập nhật badge, optionally show notification */
    public void refreshCartBadge(boolean showNotificationIfHasItems) {
        if (sessionManager == null)
            sessionManager = new SessionManager(this);
        if (apiService == null)
            apiService = ApiClient.getApiService();

        if (!sessionManager.isLoggedIn() || !sessionManager.hasActiveCart()) {
            updateCartBadge(0);
            return;
        }

        int cartId = sessionManager.getActiveCartId();
        apiService.getCartItems(cartId).enqueue(new Callback<CartItemsResponse>() {
            @Override
            public void onResponse(Call<CartItemsResponse> call, Response<CartItemsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    int itemCount = response.body().getData().size();
                    updateCartBadge(itemCount);

                    // Show notification if requested and cart has items
                    if (showNotificationIfHasItems && itemCount > 0) {
                        NotificationHelper.showCartReminderNotification(MainActivity.this, itemCount);
                    }
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

        android.util.Log.d("MainActivity", "onNewIntent called - refreshing badge");

        // Refresh badge when coming back from other activities
        refreshCartBadge();

        if (intent == null || bottomNav == null)
            return;
        int selectedTab = intent.getIntExtra("selected_tab", -1);
        android.util.Log.d("MainActivity", "selected_tab from intent: " + selectedTab);
        if (selectedTab == -1)
            return;

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

    /** Xác định fragment theo tab */
    private Fragment getFragmentByTab(int tabIndex) {
        switch (tabIndex) {
            case 1:
                return ordersFragment;
            case 2:
                return cartFragment;
            case 3:
                return accountFragment;
            default:
                return homeFragment;
        }
    }

    /** Đặt tab đang chọn */
    private void setSelectedTab(int tabIndex) {
        int id;
        switch (tabIndex) {
            case 1:
                id = R.id.nav_orders;
                break;
            case 2:
                id = R.id.nav_cart;
                break;
            case 3:
                id = R.id.nav_account;
                break;
            default:
                id = R.id.nav_home;
                break;
        }
        bottomNav.setSelectedItemId(id);
    }

    /** Xử lý sự kiện chọn bottom navigation */
    private boolean onBottomNavSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            selectedFragment = homeFragment;
        } else if (itemId == R.id.nav_orders) {
            selectedFragment = ordersFragment;
        } else if (itemId == R.id.nav_cart) {
            selectedFragment = cartFragment;
        } else if (itemId == R.id.nav_account) {
            selectedFragment = accountFragment;
        }

        if (selectedFragment != null) {
            loadFragment(selectedFragment);
            return true;
        }
        return false;
    }

    /** Lên lịch chạy Worker để cập nhật badge nền */
    private void scheduleCartBadgeWorker() {
        try {
            // chạy ngay 1 lần đầu tiên sau 3s
            OneTimeWorkRequest initialWork = new OneTimeWorkRequest.Builder(
                    com.example.kibo.workers.CartBadgeWorker.class)
                    .setInitialDelay(3, TimeUnit.SECONDS)
                    .build();
            WorkManager.getInstance(this).enqueue(initialWork);

            // schedule lặp 15 phút
            PeriodicWorkRequest periodicWork = new PeriodicWorkRequest.Builder(
                    com.example.kibo.workers.CartBadgeWorker.class,
                    15, TimeUnit.MINUTES)
                    .build();

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                    "cart_badge_worker",
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicWork);
        } catch (Exception ignored) {
        }
    }

    /** Logout user */
    public void performLogout() {
        if (sessionManager == null)
            sessionManager = new SessionManager(this);
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

    /** Điều hướng về LoginActivity */
    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /** Kiểm tra user còn đăng nhập không */
    public boolean isUserLoggedIn() {
        return sessionManager != null && sessionManager.isLoggedIn();
    }

    /** Xử lý khi session hết hạn */
    public void handleSessionExpired() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        });
    }
}
