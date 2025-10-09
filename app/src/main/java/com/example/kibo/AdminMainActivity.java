package com.example.kibo;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.kibo.R;
import com.example.kibo.ui.AdminDashboardFragment;
import com.example.kibo.ui.AdminProductsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminMainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        setupBottomNavigation();
        loadInitialFragment();
    }

    private void setupBottomNavigation() {
        bottomNav = findViewById(R.id.admin_bottom_nav);
        bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private void loadInitialFragment() {
        // Mở Dashboard mặc định
        loadFragment(new AdminDashboardFragment());
        bottomNav.setSelectedItemId(R.id.nav_admin_dashboard);
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selected = null;
        int id = item.getItemId();

        if (id == R.id.nav_admin_dashboard) {
            selected = new AdminDashboardFragment();
        } else if (id == R.id.nav_admin_products) {
            selected = new AdminProductsFragment();
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
}