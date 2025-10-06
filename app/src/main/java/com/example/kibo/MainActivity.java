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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Mở mặc định trang Home
        loadFragment(new HomeFragment());

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
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }
}
