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
import com.example.kibo.models.ProductImage;
import com.example.kibo.models.CartItem;
import com.example.kibo.models.CartItemsResponse;
import com.example.kibo.utils.SessionManager;

import com.bumptech.glide.Glide;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.util.Log;

public class OrdersStatusFragment extends Fragment {
    private static final String ARG_STATUS = "status";
    
    private LinearLayout layoutEmptyOrders;
    private RecyclerView recyclerViewOrders;
    private OrdersAdapter adapter;
    private String statusFilter;

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
        
        // Initialize views
        recyclerViewOrders = root.findViewById(R.id.rv_orders);
        layoutEmptyOrders = root.findViewById(R.id.layout_empty_orders);
        
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrdersAdapter(this);
        recyclerViewOrders.setAdapter(adapter);

        // Remember status filter
        statusFilter = getArguments() != null ? getArguments().getString(ARG_STATUS) : "ALL";

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Try to get cached orders from parent immediately
        Fragment parent = getParentFragment();
        if (parent instanceof OrdersFragment) {
            ((OrdersFragment) parent).dispatchCachedOrdersToChildren();
        }
    }

    // Receive orders from parent cache and render filtered list
    public void updateOrders(java.util.List<Order> allOrders) {
        if (!isAdded() || allOrders == null) return;
        java.util.List<Order> filtered = filterOrdersByStatus(allOrders, statusFilter);
        adapter.setOrders(filtered);
    }
    
    private java.util.List<Order> filterOrdersByStatus(java.util.List<Order> orders, String statusFilter) {
        if (statusFilter == null || "ALL".equals(statusFilter)) {
            return orders;
        }
        
        java.util.List<Order> filtered = new java.util.ArrayList<>();
        for (Order order : orders) {
            int orderStatus = order.getOrderStatus();
            boolean shouldInclude = false;
            
            switch (statusFilter) {
                case "READY_TO_PICK":
                    // Chờ lấy hàng: 0 (ready_to_pick), 2 (picking)
                    shouldInclude = orderStatus == 0 || orderStatus == 2;
                    break;
                case "DELIVERING":
                    // Chờ giao hàng: 3 (picked), 4 (delivering)
                    shouldInclude = orderStatus == 3 || orderStatus == 4;
                    break;
                case "DELIVERED":
                    // Đã giao: 5 (delivered)
                    shouldInclude = orderStatus == 5;
                    break;
                case "RETURNED":
                    // Trả hàng: 7 (return), 8 (returned)
                    shouldInclude = orderStatus == 7 || orderStatus == 8;
                    break;
                case "CANCELLED":
                    // Đã hủy: 1 (cancel)
                    shouldInclude = orderStatus == 1;
                    break;
            }
            
            if (shouldInclude) {
                filtered.add(order);
            }
        }
        
        return filtered;
    }
    
    public void showEmptyState() {
        layoutEmptyOrders.setVisibility(View.VISIBLE);
        recyclerViewOrders.setVisibility(View.GONE);
    }
    
    public void showOrders() {
        layoutEmptyOrders.setVisibility(View.GONE);
        recyclerViewOrders.setVisibility(View.VISIBLE);
    }

    private void navigateToOrderDetail(Order order) {
        if (getActivity() != null) {
            Fragment detailFragment = OrderDetailFragement.newInstance(order);
            int containerId = R.id.frame_container;
            android.view.View adminContainer = getActivity().findViewById(R.id.admin_container);
            if (adminContainer != null) {
                containerId = R.id.admin_container;
            }
            // Update admin toolbar title if in admin flow
            if (getActivity() instanceof com.example.kibo.AdminMainActivity && getActivity().findViewById(R.id.admin_container) != null) {
                ((com.example.kibo.AdminMainActivity) getActivity()).setToolbarTitle("Chi tiết đơn hàng");
            }
            getActivity().getSupportFragmentManager().beginTransaction()
                .replace(containerId, detailFragment)
                .addToBackStack(null)
                .commit();
        }
    }

    private static class OrdersAdapter extends RecyclerView.Adapter<OrderVH> {
        private final java.util.List<Order> orders = new java.util.ArrayList<>();
        private final OrdersStatusFragment fragment;

        OrdersAdapter(OrdersStatusFragment fragment) {
            this.fragment = fragment;
        }

        void setOrders(java.util.List<Order> list) {
            orders.clear();
            orders.addAll(list);
            notifyDataSetChanged();
            
            // Show/hide empty state based on orders count
            if (orders.isEmpty()) {
                fragment.showEmptyState();
            } else {
                fragment.showOrders();
            }
        }

        @NonNull @Override public OrderVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
            return new OrderVH(v);
        }

        @Override public void onBindViewHolder(@NonNull OrderVH holder, int position) {
            Order o = orders.get(position);
            String statusText = getStatusText(o.getOrderStatus());
            holder.tvStatus.setText(statusText);
            holder.tvOrderCode.setText("Mã đơn: " + (o.getOrderCode() != null ? o.getOrderCode() : ""));

            // Set click listener to navigate to order detail
            holder.itemView.setOnClickListener(v -> {
                fragment.navigateToOrderDetail(o);
            });

            // Load order details to get accurate product data (use orderId from order)
            ApiService api = ApiClient.getApiService();
            loadOrderDetailsForOrder(o, holder);

            // Payment info: fetch by paymentId if present
            Integer paymentId = o.getPaymentId();
            if (paymentId != null && paymentId > 0) {
                api.getPaymentStatus(paymentId).enqueue(new retrofit2.Callback<java.util.List<com.example.kibo.models.Payment>>() {
                    @Override
                    public void onResponse(retrofit2.Call<java.util.List<com.example.kibo.models.Payment>> call, retrofit2.Response<java.util.List<com.example.kibo.models.Payment>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            com.example.kibo.models.Payment p = response.body().get(0);
                            int method = p.getPaymentMethod();
                            int status = p.getPaymentStatus();
                            double amount = p.getAmount();

                            // Filter rules
                            boolean shouldShow = (method == 1 && status == 1) || (method == 2);

                            holder.itemView.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
                            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
                            if (params != null) {
                                params.height = shouldShow ? RecyclerView.LayoutParams.WRAP_CONTENT : 0;
                                holder.itemView.setLayoutParams(params);
                            }

                            // Bind method and status text
                            String methodText = method == 1 ? "ZaloPay" : method == 2 ? "Tiền mặt" : ("Khác (" + method + ")");
                            String statusText = status == 1 ? "Đã thanh toán" : "Chưa thanh toán";
                            holder.tvPaymentMethod.setText("Phương thức thanh toán: " + methodText);
                            holder.tvPaymentStatus.setText("Trạng thái: " + statusText);

                            // Display total order amount from payment
                            holder.tvPrice.setText(String.format("%,.0fđ", amount));
                        }
                    }

                    @Override public void onFailure(retrofit2.Call<java.util.List<com.example.kibo.models.Payment>> call, Throwable t) { }
                });
            } else {
                holder.tvPaymentMethod.setText("Phương thức thanh toán: N/A");
                holder.tvPaymentStatus.setText("Trạng thái: Chưa thanh toán");
            }
        }

        @Override public int getItemCount() { return orders.size(); }
        
        private void loadProductImage(int productId, ImageView imageView) {
            ApiService api = ApiClient.getApiService();
            api.getProductImages(productId).enqueue(new retrofit2.Callback<java.util.List<ProductImage>>() {
                @Override
                public void onResponse(retrofit2.Call<java.util.List<ProductImage>> call, retrofit2.Response<java.util.List<ProductImage>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        java.util.List<ProductImage> images = response.body();
                        String chosenUrl = null;
                        // Prefer primary image, otherwise use first image
                        for (ProductImage img : images) {
                            if (img.isPrimary()) {
                                chosenUrl = img.getImageUrl();
                                break;
                            }
                        }
                        if (chosenUrl == null) {
                            chosenUrl = images.get(0).getImageUrl();
                        }
                        if (chosenUrl != null) {
                            Glide.with(imageView.getContext()).load(chosenUrl).into(imageView);
                        }
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<java.util.List<ProductImage>> call, Throwable t) { }
            });
        }
        
        private void loadOrderDetailsForOrder(Order order, OrderVH holder) {
            ApiService api = ApiClient.getApiService();
            api.getOrderDetails(order.getOrderId()).enqueue(new retrofit2.Callback<OrderDetailsResponse>() {
                @Override
                public void onResponse(retrofit2.Call<OrderDetailsResponse> call, retrofit2.Response<OrderDetailsResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                        java.util.List<OrderDetail> orderDetails = response.body().getData();
                        // Display first product (index 0) for list view
                        OrderDetail firstDetail = orderDetails.get(0);
                        holder.tvName.setText(firstDetail.getProductName());
                        holder.tvPrice.setText(String.format("%,.0fđ", firstDetail.getUnitPrice()));
                        holder.tvQuantity.setText("x" + firstDetail.getQuantity());
                        
                        // Load product image using ProductImages API
                        loadProductImage(firstDetail.getProductId(), holder.img);
                    }
                }
                @Override public void onFailure(retrofit2.Call<OrderDetailsResponse> call, Throwable t) { }
            });
        }
        
        private String getStatusText(int orderStatus) {
            switch (orderStatus) {
                case 0: return "Chờ lấy hàng";
                case 1: return "Đã hủy";
                case 2: return "Đang lấy hàng";
                case 3: return "Đã lấy hàng";
                case 4: return "Đang giao hàng";
                case 5: return "Đã giao";
                case 6: return "Giao hàng thất bại";
                case 7: return "Đang trả hàng";
                case 8: return "Đã trả hàng";
                default: return "Không xác định";
            }
        }
    }

    private static class OrderVH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName;
        TextView tvPrice;
        TextView tvStatus;
        TextView tvOrderCode;
        TextView tvQuantity;
        TextView tvPaymentMethod;
        TextView tvPaymentStatus;

        OrderVH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img_product);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvStatus = itemView.findViewById(R.id.tv_order_status);
            tvOrderCode = itemView.findViewById(R.id.tv_order_code);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvPaymentMethod = itemView.findViewById(R.id.tv_payment_method);
            tvPaymentStatus = itemView.findViewById(R.id.tv_payment_status);
        }
    }
}


