package com.example.kibo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;
import com.example.kibo.R;
import com.example.kibo.adapters.AdminManagementPagerAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AdminManagementActivity extends AppCompatActivity {
    
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private AdminManagementPagerAdapter adapter;
    private BottomNavigationView bottomNav;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_management);
        
        setupToolbar();
        setupTabLayout();
        setupViewPager();
        setupTabMediator();
        setupBottomNavigation();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_admin);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Quản lý Admin");
        }
        
        // Toolbar sẽ tự động fit với system windows từ CoordinatorLayout
    }
    
    private void setupTabLayout() {
        tabLayout = findViewById(R.id.tab_layout);
    }
    
    private void setupViewPager() {
        viewPager = findViewById(R.id.view_pager);
        adapter = new AdminManagementPagerAdapter(this);
        viewPager.setAdapter(adapter);
    }
    
    private void setupTabMediator() {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Danh mục");
                    tab.setIcon(R.drawable.ic_category);
                    break;
                case 1:
                    tab.setText("Sản phẩm");
                    tab.setIcon(R.drawable.ic_keyboard);
                    break;
            }
        }).attach();
    }
    
    private void setupBottomNavigation() {
        bottomNav = findViewById(R.id.admin_bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_admin_products); // Highlight Products tab
        bottomNav.setOnItemSelectedListener(this::onBottomNavigationSelected);
        
        // Mở Danh mục tab mặc định (position 0)
        viewPager.setCurrentItem(0);
    }
    
    private boolean onBottomNavigationSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_admin_dashboard) {
            Intent intent = new Intent(this, AdminMainActivity.class);
            intent.putExtra("fragment", "dashboard");
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_admin_products) {
            // Already in Products, do nothing
            return true;
        } else if (id == R.id.nav_admin_messages) {
            Intent intent = new Intent(this, AdminMainActivity.class);
            intent.putExtra("fragment", "messages");
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_admin_account) {
            Intent intent = new Intent(this, AdminMainActivity.class);
            intent.putExtra("fragment", "account");
            startActivity(intent);
            return true;
        }
        
        return false;
    }
    
    // Method để nhảy qua tab "Sản phẩm" và hiện tên sản phẩm trong ô tìm kiếm
    public void navigateToProductsWithSearch(String productName) {
        // Chuyển sang tab "Sản phẩm" (position 1)
        viewPager.setCurrentItem(1);
        
        // Delay để đảm bảo fragment đã được tạo và sẵn sàng
        viewPager.postDelayed(() -> {
            androidx.fragment.app.Fragment productsFragment = adapter.getFragment(1);
            if (productsFragment instanceof com.example.kibo.ui.AdminProductsFragment) {
                ((com.example.kibo.ui.AdminProductsFragment) productsFragment).setSearchQuery(productName);
            }
        }, 300); // Tăng delay lên 300ms
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
