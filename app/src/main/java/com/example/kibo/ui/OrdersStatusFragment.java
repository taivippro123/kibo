package com.example.kibo.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kibo.R;
import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.Order;
import com.example.kibo.models.OrderDetail;
import com.example.kibo.models.OrderDetailsResponse;
import com.example.kibo.models.Product;
import com.example.kibo.models.ProductResponse;
import com.example.kibo.utils.SessionManager;

import com.bumptech.glide.Glide;
import android.widget.ImageView;
import android.widget.TextView;

public class OrdersStatusFragment extends Fragment {
    private static final String ARG_STATUS = "status";

    public static OrdersStatusFragment newInstance(String status) {
        OrdersStatusFragment fragment = new OrdersStatusFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_orders_status, container, false);
        RecyclerView rv = root.findViewById(R.id.rv_orders);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        OrdersAdapter adapter = new OrdersAdapter();
        rv.setAdapter(adapter);

        // Load orders for current user
        SessionManager sm = new SessionManager(requireContext());
        int userId = sm.getUserId();
        ApiService api = ApiClient.getApiService();
        api.getOrders(userId).enqueue(new retrofit2.Callback<java.util.List<Order>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<Order>> call, retrofit2.Response<java.util.List<Order>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setOrders(response.body());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<Order>> call, Throwable t) { }
        });

        return root;
    }

    private static class OrdersAdapter extends RecyclerView.Adapter<OrderVH> {
        private final java.util.List<Order> orders = new java.util.ArrayList<>();

        void setOrders(java.util.List<Order> list) {
            orders.clear();
            orders.addAll(list);
            notifyDataSetChanged();
        }

        @NonNull @Override public OrderVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
            return new OrderVH(v);
        }

        @Override public void onBindViewHolder(@NonNull OrderVH holder, int position) {
            Order o = orders.get(position);
            holder.tvStatus.setText(String.valueOf(o.getOrderStatus()));
            holder.tvOrderCode.setText("Mã đơn: " + (o.getOrderCode() != null ? o.getOrderCode() : ""));

            // Fetch first order detail
            ApiService api = ApiClient.getApiService();
            api.getOrderDetails(o.getOrderId()).enqueue(new retrofit2.Callback<OrderDetailsResponse>() {
                @Override
                public void onResponse(retrofit2.Call<OrderDetailsResponse> call, retrofit2.Response<OrderDetailsResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                        OrderDetail od = response.body().getData().get(0);
                        holder.tvQuantity.setText("x" + od.getQuantity());
                        // Fetch product
                        api.getProductById(od.getProductId()).enqueue(new retrofit2.Callback<ProductResponse>() {
                            @Override
                            public void onResponse(retrofit2.Call<ProductResponse> call2, retrofit2.Response<ProductResponse> resp2) {
                                if (resp2.isSuccessful() && resp2.body() != null && resp2.body().getData() != null && !resp2.body().getData().isEmpty()) {
                                    Product p = resp2.body().getData().get(0);
                                    holder.tvName.setText(p.getProductName());
                                    holder.tvPrice.setText(String.format("%,.0fđ", p.getPrice()));
                                    Glide.with(holder.img.getContext()).load(p.getImageUrl()).into(holder.img);
                                }
                            }

                            @Override public void onFailure(retrofit2.Call<ProductResponse> call2, Throwable t) { }
                        });
                    }
                }

                @Override public void onFailure(retrofit2.Call<OrderDetailsResponse> call, Throwable t) { }
            });
        }

        @Override public int getItemCount() { return orders.size(); }
    }

    private static class OrderVH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName;
        TextView tvPrice;
        TextView tvStatus;
        TextView tvOrderCode;
        TextView tvQuantity;

        OrderVH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img_product);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvStatus = itemView.findViewById(R.id.tv_order_status);
            tvOrderCode = itemView.findViewById(R.id.tv_order_code);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
        }
    }
}


