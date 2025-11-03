package com.example.kibo.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.kibo.R;
import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.Order;
import com.example.kibo.models.OrderDetail;
import com.example.kibo.models.OrderDetailsResponse;
import com.example.kibo.models.Product;
import com.example.kibo.models.ProductResponse;
import com.bumptech.glide.Glide;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;

public class OrderDetailFragement extends Fragment {
    private static final String ARG_ORDER = "order";
    private static final String GHN_TOKEN = "65f91455-8ee4-11f0-bdaf-ae7fa045a771";

    private ImageButton btnBack;
    private ImageView imgProduct;
    private TextView tvOrderCode;
    private TextView tvOrderStatus;
    private TextView tvProductName;
    private TextView tvPrice;
    private TextView tvQuantity;
    private TextView tvPaymentMethod;
    private TextView tvPaymentStatus;
    private TextView tvCustomerLabel;
    private TextView tvCustomerName;
    private LinearLayout timelineContainer;
    private TextView tvStage1, tvStage2, tvStage3;
    private android.widget.ImageView checkStage1, checkStage2, checkStage3;

    private Order order;
    private View adminToolbar;
    private View adminContainer;
    private int originalTopMargin = -1;

    public static OrderDetailFragement newInstance(Order order) {
        OrderDetailFragement fragment = new OrderDetailFragement();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ORDER, order);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_order_detail, container, false);

        // Get order from arguments
        if (getArguments() != null) {
            Object orderObj = getArguments().getSerializable(ARG_ORDER);
            if (orderObj instanceof Order) {
                order = (Order) orderObj;
            }
        }

        // Initialize views
        btnBack = root.findViewById(R.id.btn_back);
        imgProduct = root.findViewById(R.id.img_product);
        tvOrderCode = root.findViewById(R.id.tv_order_code);
        tvOrderStatus = root.findViewById(R.id.tv_order_status);
        tvProductName = root.findViewById(R.id.tv_product_name);
        tvPrice = root.findViewById(R.id.tv_price);
        tvQuantity = root.findViewById(R.id.tv_quantity);
        tvPaymentMethod = root.findViewById(R.id.tv_payment_method);
        tvPaymentStatus = root.findViewById(R.id.tv_payment_status);
        tvCustomerLabel = root.findViewById(R.id.tv_customer_label);
        tvCustomerName = root.findViewById(R.id.tv_customer_name);
        timelineContainer = root.findViewById(R.id.timeline_container);
        tvStage1 = root.findViewById(R.id.tv_stage1);
        tvStage2 = root.findViewById(R.id.tv_stage2);
        tvStage3 = root.findViewById(R.id.tv_stage3);
        checkStage1 = root.findViewById(R.id.checkStage1);
        checkStage2 = root.findViewById(R.id.checkStage2);
        checkStage3 = root.findViewById(R.id.checkStage3);

        // Ensure header content is visible under status bar/cutout when toolbar is hidden
        try {
            final View header = root.findViewById(R.id.header_order_detail);
            if (header != null) {
                androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(header, (v, insets) -> {
                    int topInset = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.statusBars()).top;
                    v.setPadding(v.getPaddingLeft(), topInset, v.getPaddingRight(), v.getPaddingBottom());
                    return insets;
                });
                // Request insets now
                androidx.core.view.ViewCompat.requestApplyInsets(header);
            }
        } catch (Exception ignored) { }

        // If in admin flow, hide activity toolbar so only fragment header shows
        if (getActivity() != null) {
            adminToolbar = getActivity().findViewById(R.id.admin_toolbar);
            if (adminToolbar != null) {
                adminToolbar.setVisibility(View.GONE);
            }
            // Remove top margin from admin_container so header sticks to top
            adminContainer = getActivity().findViewById(R.id.admin_container);
            if (adminContainer != null && adminContainer.getLayoutParams() instanceof android.view.ViewGroup.MarginLayoutParams) {
                android.view.ViewGroup.MarginLayoutParams lp = (android.view.ViewGroup.MarginLayoutParams) adminContainer.getLayoutParams();
                originalTopMargin = lp.topMargin;
                lp.topMargin = 0;
                adminContainer.setLayoutParams(lp);
            }
        }

        // If opened from admin flow, hide fragment header and set admin toolbar title
        View adminContainer = getActivity() != null ? getActivity().findViewById(R.id.admin_container) : null;
        if (adminContainer != null) {
            View header = root.findViewById(R.id.header_order_detail);
            if (header != null) header.setVisibility(View.GONE);
            if (getActivity() instanceof com.example.kibo.AdminMainActivity) {
                ((com.example.kibo.AdminMainActivity) getActivity()).setToolbarTitle("Chi tiết đơn hàng");
            }
        }

        // Setup back button
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Load order details
        if (order != null) {
            loadOrderDetails();
            loadGHNTracking();
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        // Restore admin toolbar visibility when leaving detail in admin flow
        if (adminToolbar != null) {
            adminToolbar.setVisibility(View.VISIBLE);
            adminToolbar = null;
        }
        if (adminContainer != null && originalTopMargin >= 0 && adminContainer.getLayoutParams() instanceof android.view.ViewGroup.MarginLayoutParams) {
            android.view.ViewGroup.MarginLayoutParams lp = (android.view.ViewGroup.MarginLayoutParams) adminContainer.getLayoutParams();
            lp.topMargin = originalTopMargin;
            adminContainer.setLayoutParams(lp);
            adminContainer = null;
            originalTopMargin = -1;
        }
        super.onDestroyView();
    }

    private void loadOrderDetails() {
        // Display order code and status
        tvOrderCode.setText(order.getOrderCode());
        tvOrderStatus.setText(getStatusText(order.getOrderStatus()));

        // Load order details (products in this order)
        ApiService api = ApiClient.getApiService();
        api.getOrderDetails(order.getOrderId()).enqueue(new retrofit2.Callback<OrderDetailsResponse>() {
            @Override
            public void onResponse(retrofit2.Call<OrderDetailsResponse> call, retrofit2.Response<OrderDetailsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    OrderDetail orderDetail = response.body().getData().get(0);
                    
                    // Display quantity
                    tvQuantity.setText("x" + orderDetail.getQuantity());

                    // Load product details
                    api.getProductById(orderDetail.getProductId()).enqueue(new retrofit2.Callback<ProductResponse>() {
                        @Override
                        public void onResponse(retrofit2.Call<ProductResponse> call2, retrofit2.Response<ProductResponse> resp2) {
                            if (resp2.isSuccessful() && resp2.body() != null && resp2.body().getData() != null && !resp2.body().getData().isEmpty()) {
                                Product product = resp2.body().getData().get(0);
                                
                                // Display product details
                                tvProductName.setText(product.getProductName());
                                tvPrice.setText(String.format("%,.0fđ", product.getPrice()));
                                Glide.with(imgProduct.getContext())
                                    .load(product.getImageUrl())
                                    .into(imgProduct);
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<ProductResponse> call2, Throwable t) {
                            // Handle error silently
                        }
                    });
                }
            }

            @Override
            public void onFailure(retrofit2.Call<OrderDetailsResponse> call, Throwable t) {
                // Handle error silently
            }
        });

        // Load payment details if payment ID exists
        Integer paymentId = order.getPaymentId();
        if (paymentId != null && paymentId > 0) {
            api.getPaymentStatus(paymentId).enqueue(new retrofit2.Callback<java.util.List<com.example.kibo.models.Payment>>() {
                @Override
                public void onResponse(retrofit2.Call<java.util.List<com.example.kibo.models.Payment>> call, retrofit2.Response<java.util.List<com.example.kibo.models.Payment>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        com.example.kibo.models.Payment payment = response.body().get(0);
                        int method = payment.getPaymentMethod();
                        int status = payment.getPaymentStatus();

                        // Display payment method
                        String methodText = getPaymentMethodText(method);
                        tvPaymentMethod.setText(methodText);

                        // Display payment status
                        String statusText = getPaymentStatusText(status);
                        tvPaymentStatus.setText(statusText);
                        
                        // Set color based on status
                        int colorResId = status == 1 ? R.color.green : R.color.red_dark;
                        tvPaymentStatus.setTextColor(getResources().getColor(colorResId, null));
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<java.util.List<com.example.kibo.models.Payment>> call, Throwable t) {
                    // Handle error silently
                    tvPaymentMethod.setText("Không xác định");
                    tvPaymentStatus.setText("Không xác định");
                }
            });
        } else {
            tvPaymentMethod.setText("Chưa có");
            tvPaymentStatus.setText("Chưa có");
        }

        // If opened from admin flow, show customer name (Users?Userid=...)
        View adminContainer = getActivity() != null ? getActivity().findViewById(R.id.admin_container) : null;
        if (adminContainer != null) {
            try {
                ApiService apiForUser = ApiClient.getApiService();
                apiForUser.getUserById(order.getUserId()).enqueue(new retrofit2.Callback<com.example.kibo.models.UserResponse>() {
                    @Override public void onResponse(retrofit2.Call<com.example.kibo.models.UserResponse> call, retrofit2.Response<com.example.kibo.models.UserResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                            String name = response.body().getData().get(0).getUsername();
                            tvCustomerLabel.setVisibility(View.VISIBLE);
                            tvCustomerName.setVisibility(View.VISIBLE);
                            tvCustomerName.setText(name != null ? name : "");
                        }
                    }
                    @Override public void onFailure(retrofit2.Call<com.example.kibo.models.UserResponse> call, Throwable t) { }
                });
            } catch (Exception ignored) { }
        }
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

    private String getPaymentMethodText(int method) {
        switch (method) {
            case 1: return "ZaloPay";
            case 2: return "Tiền mặt";
            default: return "Khác (" + method + ")";
        }
    }

    private String getPaymentStatusText(int status) {
        switch (status) {
            case 0: return "Chưa thanh toán";
            case 1: return "Đã thanh toán";
            default: return "Không xác định";
        }
    }

    private void loadGHNTracking() {
        if (order == null || order.getOrderCode() == null) return;
        
        try {
            // Create request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("order_code", order.getOrderCode());
            
            Log.d("GHNTracking", "Request: " + requestBody.toString());
            
            // Call GHN API using OkHttp directly (since it's external URL)
            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
            okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/detail")
                .addHeader("Content-Type", "application/json")
                .addHeader("Token", GHN_TOKEN)
                .post(okhttp3.RequestBody.create(requestBody.toString(), okhttp3.MediaType.parse("application/json")))
                .build();
            
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, java.io.IOException e) {
                    Log.e("GHNTracking", "Error: " + e.getMessage());
                }
                
                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                    if (response.isSuccessful()) {
                        String jsonData = response.body().string();
                        Log.d("GHNTracking", "Response: " + jsonData);
                        
                        getActivity().runOnUiThread(() -> {
                            parseAndDisplayTracking(jsonData);
                        });
                    }
                }
            });
            
        } catch (Exception e) {
            Log.e("GHNTracking", "Error calling API: " + e.getMessage());
        }
    }

    private void parseAndDisplayTracking(String jsonData) {
        try {
            JSONObject json = new JSONObject(jsonData);
            JSONObject data = json.optJSONObject("data");
            if (data == null) return;
            
            JSONArray logArray = data.optJSONArray("log");
            if (logArray == null || logArray.length() == 0) return;
            
            // Determine current status and update progress
            updateProgressIndicators(logArray);
            
            // Display timeline
            displayTimeline(logArray);
            
        } catch (Exception e) {
            Log.e("GHNTracking", "Error parsing: " + e.getMessage());
        }
    }

    private void updateProgressIndicators(JSONArray logArray) {
        boolean hasShipped = false;
        boolean hasDelivering = false;
        boolean hasDelivered = false;

        for (int i = 0; i < logArray.length(); i++) {
            try {
                JSONObject logItem = logArray.getJSONObject(i);
                String status = logItem.getString("status");

                if ("picked".equals(status)) hasShipped = true;
                if ("delivering".equals(status)) hasDelivering = true;
                if ("delivered".equals(status)) hasDelivered = true;
            } catch (Exception e) { }
        }

        // Reset all to gray
        checkStage1.setImageResource(R.drawable.circle_unchecked);
        checkStage2.setImageResource(R.drawable.circle_unchecked);
        checkStage3.setImageResource(R.drawable.circle_unchecked);

        tvStage1.setTextColor(getResources().getColor(R.color.gray_dark, null));
        tvStage2.setTextColor(getResources().getColor(R.color.gray_dark, null));
        tvStage3.setTextColor(getResources().getColor(R.color.gray_dark, null));

        // Highlight current
        if (hasDelivered) {
            checkStage1.setImageResource(R.drawable.circle_checked);
            checkStage2.setImageResource(R.drawable.circle_checked);
            checkStage3.setImageResource(R.drawable.circle_checked);
            tvStage3.setTextColor(getResources().getColor(R.color.green, null));
        } else if (hasDelivering) {
            checkStage1.setImageResource(R.drawable.circle_checked);
            checkStage2.setImageResource(R.drawable.circle_checked);
            tvStage2.setTextColor(getResources().getColor(R.color.green, null));
        } else if (hasShipped) {
            checkStage1.setImageResource(R.drawable.circle_checked);
            tvStage1.setTextColor(getResources().getColor(R.color.green, null));
        }
    }

    private void displayTimeline(JSONArray logArray) {
        timelineContainer.removeAllViews();
        
        // Add items in reverse order (newest first) and determine current status
        boolean isFirstActive = true;
        String lastStatus = "";
        
        // Get the last status to determine which is active
        if (logArray.length() > 0) {
            try {
                JSONObject lastItem = logArray.getJSONObject(logArray.length() - 1);
                lastStatus = lastItem.getString("status");
            } catch (Exception e) {
                // Ignore
            }
        }
        
        // Display in reverse order (newest to oldest)
        for (int i = logArray.length() - 1; i >= 0; i--) {
            try {
                JSONObject logItem = logArray.getJSONObject(i);
                String status = logItem.getString("status");
                String updatedDate = logItem.getString("updated_date");
                
                boolean isActive = status.equals(lastStatus);
                View timelineItem = createTimelineItem(status, updatedDate, isActive);
                timelineContainer.addView(timelineItem);
                
            } catch (Exception e) {
                Log.e("Timeline", "Error creating item: " + e.getMessage());
            }
        }
    }

    private View createTimelineItem(String status, String dateStr, boolean isActive) {
        // Create item view
        View itemView = LayoutInflater.from(getContext())
            .inflate(R.layout.item_tracking_timeline, timelineContainer, false);
        
        TextView tvStatus = itemView.findViewById(R.id.tv_status_timeline);
        TextView tvDate = itemView.findViewById(R.id.tv_date_timeline);
        View timelineDot = itemView.findViewById(R.id.timeline_dot);
        
        tvStatus.setText(getStatusTextFromGHN(status));
        
        // Parse date
        try {
            String formattedDate = formatDate(dateStr);
            tvDate.setText(formattedDate);
        } catch (Exception e) {
            tvDate.setText(dateStr);
        }
        
        // Set colors and dot based on active state
        if (isActive) {
            // Latest status: green dot, green text
            timelineDot.setBackground(getResources().getDrawable(R.drawable.circle_background, null));
            tvStatus.setTextColor(getResources().getColor(R.color.green, null));
            tvDate.setTextColor(getResources().getColor(R.color.gray_dark, null));
        } else {
            // Older statuses: black dot, gray text
            timelineDot.setBackground(getResources().getDrawable(R.drawable.circle_background_black, null));
            tvStatus.setTextColor(getResources().getColor(R.color.gray_dark, null));
            tvDate.setTextColor(getResources().getColor(R.color.gray_dark, null));
        }
        
        return itemView;
    }

    private String getStatusTextFromGHN(String status) {
        switch (status) {
            case "picking": return "Đã tiếp nhận đơn hàng";
            case "picked": return "Đã vận chuyển";
            case "delivering": return "Đang giao hàng";
            case "delivered": return "Giao hàng thành công";
            case "return": return "Đang trả hàng";
            case "returned": return "Đã trả hàng";
            case "cancel": return "Đã hủy";
            default: return status;
        }
    }

    private String formatDate(String isoDate) {
        try {
            // Parse ISO 8601 format
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US);
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.US);
            inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Date date = inputFormat.parse(isoDate);
            return outputFormat.format(date);
        } catch (Exception e) {
            return isoDate;
        }
    }
}

