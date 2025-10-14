package com.example.kibo.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class OrdersPagerAdapter extends FragmentStateAdapter {
    private static final int TAB_COUNT = 6;
    private static final long[] STABLE_IDS = new long[]{0,1,2,3,4,5};

    public OrdersPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Map each tab to a simple list fragment filtered by status
        switch (position) {
            case 0: return OrdersStatusFragment.newInstance("ALL");
            case 1: return OrdersStatusFragment.newInstance("READY_TO_PICK");
            case 2: return OrdersStatusFragment.newInstance("DELIVERING");
            case 3: return OrdersStatusFragment.newInstance("DELIVERED");
            case 4: return OrdersStatusFragment.newInstance("RETURNED");
            case 5: return OrdersStatusFragment.newInstance("CANCELLED");
            default: return OrdersStatusFragment.newInstance("ALL");
        }
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }

    // Provide stable IDs so ViewPager2/FragmentStateAdapter can restore state safely
    @Override
    public long getItemId(int position) {
        return STABLE_IDS[position];
    }

    @Override
    public boolean containsItem(long itemId) {
        for (long id : STABLE_IDS) {
            if (id == itemId) return true;
        }
        return false;
    }
}


