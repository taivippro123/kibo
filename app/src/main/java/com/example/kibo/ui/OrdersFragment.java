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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.Order;
import com.example.kibo.utils.SessionManager;

public class OrdersFragment extends Fragment {
    private static final String[] TAB_TITLES = new String[]{
            "Tất cả", "Chờ lấy hàng", "Chờ giao hàng", "Đã giao"
    };

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private TabLayoutMediator tabMediator;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ViewPager2.OnPageChangeCallback pageChangeCallback;

    // In-memory cache for this screen session
    private static java.util.List<Order> cachedOrders = null;
    private OrdersPagerAdapter pagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = view.findViewById(R.id.tab_layout);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        viewPager = view.findViewById(R.id.view_pager);

        // Hide fragment header when running inside Admin (use Admin toolbar instead)
        View adminContainer = getActivity() != null ? getActivity().findViewById(R.id.admin_container) : null;
        if (adminContainer != null) {
            View header = view.findViewById(R.id.header_orders);
            if (header != null) header.setVisibility(View.GONE);
            if (getActivity() instanceof com.example.kibo.AdminMainActivity) {
                ((com.example.kibo.AdminMainActivity) getActivity()).setToolbarTitle("Quản lý đơn hàng");
            }
        }

        // Disable state restoration to avoid ViewPager2 restoring stale fragment keys
        viewPager.setSaveEnabled(false);

        // Setup adapter
        pagerAdapter = new OrdersPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Attach tabs
        tabMediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(TAB_TITLES[position]));
        tabMediator.attach();

        // Pull-to-refresh: trigger server sync then reload orders
        swipeRefreshLayout.setOnRefreshListener(() -> {
            syncWithGhnAndReload();
        });

        // Listen to page changes to update child fragments with cached data
        pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // When user swipes to a new tab, immediately show cached data
                if (cachedOrders != null && !cachedOrders.isEmpty()) {
                    dispatchOrdersToChildren(cachedOrders);
                }
            }
        };
        viewPager.registerOnPageChangeCallback(pageChangeCallback);

        // On first load, fetch data from server
        if (cachedOrders == null || cachedOrders.isEmpty()) {
            syncWithGhnAndReload();
        } else {
            // If we have cached data, immediately show it
            dispatchOrdersToChildren(cachedOrders);
        }
    }

    @Override
    public void onDestroyView() {
        if (tabMediator != null) {
            tabMediator.detach();
            tabMediator = null;
        }
        if (viewPager != null && pageChangeCallback != null) {
            viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
        }
        pageChangeCallback = null;
        viewPager = null;
        tabLayout = null;
        swipeRefreshLayout = null;
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Only sync and load on first entry or when returning from other activities
        // Don't reload every time onResume is called (e.g., when swiping between tabs)
        if (cachedOrders == null || cachedOrders.isEmpty()) {
            syncWithGhnAndReload();
        } else {
            // If we have cached data, just distribute it to children
            dispatchOrdersToChildren(cachedOrders);
        }
    }

    private void syncWithGhnAndReload() {
        ApiService api = ApiClient.getApiService();
        // Step 1: Ask server to sync with GHN (no params)
        api.syncOrdersWithGHN().enqueue(new retrofit2.Callback<Object>() {
            @Override public void onResponse(retrofit2.Call<Object> call, retrofit2.Response<Object> response) {
                // Regardless of result, load orders for current user
                loadOrdersFromServer();
            }
            @Override public void onFailure(retrofit2.Call<Object> call, Throwable t) {
                loadOrdersFromServer();
            }
        });
    }

    private void loadOrdersFromServer() {
        SessionManager sm = new SessionManager(requireContext());
        int userId = sm.getUserId();
        boolean isAdmin = false;
        try { isAdmin = sm.getUserRole() == 0; } catch (Exception ignored) { }
        ApiService api = ApiClient.getApiService();
        retrofit2.Call<java.util.List<Order>> call = isAdmin ? api.getAllOrders() : api.getOrders(userId);
        call.enqueue(new retrofit2.Callback<java.util.List<Order>>() {
            @Override public void onResponse(retrofit2.Call<java.util.List<Order>> call, retrofit2.Response<java.util.List<Order>> response) {
                if (!isAdded()) return;
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    cachedOrders = response.body();
                    dispatchOrdersToChildren(cachedOrders);
                }
            }
            @Override public void onFailure(retrofit2.Call<java.util.List<Order>> call, Throwable t) {
                if (!isAdded()) return;
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void dispatchOrdersToChildren(java.util.List<Order> orders) {
        // Iterate through current fragments and push data to those that are visible/created
        for (Fragment f : getChildFragmentManager().getFragments()) {
            if (f instanceof OrdersStatusFragment) {
                ((OrdersStatusFragment) f).updateOrders(orders);
            }
        }
    }

    // Public method to allow child fragments to request cached data
    public void dispatchCachedOrdersToChildren() {
        if (cachedOrders != null && !cachedOrders.isEmpty()) {
            dispatchOrdersToChildren(cachedOrders);
        }
    }
}
