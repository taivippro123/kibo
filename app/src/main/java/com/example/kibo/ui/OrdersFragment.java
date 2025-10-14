package com.example.kibo.ui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.kibo.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.viewpager2.widget.ViewPager2;

public class OrdersFragment extends Fragment {
    private static final String[] TAB_TITLES = new String[]{
            "Tất cả", "Chờ lấy hàng", "Chờ giao hàng", "Đã giao", "Trả hàng", "Đã hủy"
    };

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private TabLayoutMediator tabMediator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);

        // Disable state restoration to avoid ViewPager2 restoring stale fragment keys
        viewPager.setSaveEnabled(false);

        // Setup adapter
        viewPager.setAdapter(new OrdersPagerAdapter(this));

        // Attach tabs
        tabMediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(TAB_TITLES[position]));
        tabMediator.attach();
    }

    @Override
    public void onDestroyView() {
        if (tabMediator != null) {
            tabMediator.detach();
            tabMediator = null;
        }
        viewPager = null;
        tabLayout = null;
        super.onDestroyView();
    }
}
