package com.example.kibo.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.kibo.ui.AdminProductsFragment;
import com.example.kibo.ui.AdminCategoryFragment;

public class AdminManagementPagerAdapter extends FragmentStateAdapter {
    
    public AdminManagementPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new AdminCategoryFragment();
            case 1:
                return new AdminProductsFragment();
            default:
                return new AdminCategoryFragment();
        }
    }
    
    @Override
    public int getItemCount() {
        return 2; // Products và Categories
    }
}
