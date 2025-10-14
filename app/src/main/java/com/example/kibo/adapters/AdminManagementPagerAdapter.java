package com.example.kibo.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.kibo.ui.AdminProductsFragment;
import com.example.kibo.ui.AdminCategoryFragment;

public class AdminManagementPagerAdapter extends FragmentStateAdapter {
    
    private Fragment[] fragments = new Fragment[2];
    
    public AdminManagementPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new AdminCategoryFragment();
                break;
            case 1:
                fragment = new AdminProductsFragment();
                break;
            default:
                fragment = new AdminCategoryFragment();
                break;
        }
        fragments[position] = fragment;
        return fragment;
    }
    
    @Override
    public int getItemCount() {
        return 2; // Products và Categories
    }
    
    // Method để lấy fragment theo position
    public Fragment getFragment(int position) {
        return fragments[position];
    }
}
