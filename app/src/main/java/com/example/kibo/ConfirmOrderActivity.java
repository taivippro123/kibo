package com.example.kibo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.adapters.CartItemAdapter;
import com.example.kibo.models.CartItem;
import com.example.kibo.models.CartItemsResponse;
import com.example.kibo.models.FullAddressResponse;
import com.example.kibo.models.User;
import com.example.kibo.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmOrderActivity extends AppCompatActivity {
    
    private SessionManager sessionManager;
    private ApiService apiService;
    private ImageButton buttonBack;
    private TextView textViewUserName;
    private TextView textViewUserPhone;
    private TextView textViewUserAddress;
    private TextView textViewChangeAddress;
    private RecyclerView recyclerViewOrderItems;
    private TextView textViewSubtotal;
    private TextView textViewShipping;
    private TextView textViewTotal;
    private Button buttonConfirmOrder;
    
    private CartItemAdapter cartItemAdapter;
    private java.util.ArrayList<CartItem> items = new java.util.ArrayList<>();
    private int cartId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);
        
        // Initialize SessionManager
        sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiService();
        
        // Initialize views
        initViews();
        
        // Load user data
        loadUserData();
        
        // Setup click listeners
        setupClickListeners();
        
        // Get cart id from intent or session
        cartId = getIntent().getIntExtra("cart_id", -1);
        if (cartId == -1 && sessionManager.hasActiveCart()) {
            cartId = sessionManager.getActiveCartId();
        }
        
        // Setup recycler view and load items
        setupRecyclerView();
        loadCartItems();
    }
    
    private void initViews() {
        buttonBack = findViewById(R.id.button_back);
        textViewUserName = findViewById(R.id.text_view_user_name);
        textViewUserPhone = findViewById(R.id.text_view_user_phone);
        textViewUserAddress = findViewById(R.id.text_view_user_address);
        textViewChangeAddress = findViewById(R.id.text_view_change_address);
        recyclerViewOrderItems = findViewById(R.id.recycler_view_order_items);
        textViewSubtotal = findViewById(R.id.text_view_subtotal);
        textViewShipping = findViewById(R.id.text_view_shipping);
        textViewTotal = findViewById(R.id.text_view_total);
        buttonConfirmOrder = findViewById(R.id.button_confirm_order);
    }
    
    private void loadUserData() {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            // Show login prompt or redirect
            textViewUserName.setText("Vui lòng đăng nhập");
            textViewUserPhone.setText("");
            textViewUserAddress.setText("");
            return;
        }
        
        // Get user data from SessionManager
        User user = sessionManager.getUser();
        
        if (user != null) {
            // Display user name
            String userName = user.getUsername();
            if (userName != null && !userName.isEmpty()) {
                textViewUserName.setText(userName);
            } else {
                textViewUserName.setText("Người dùng");
            }
            
            // Display user phone
            String userPhone = user.getPhonenumber();
            if (userPhone != null && !userPhone.isEmpty()) {
                textViewUserPhone.setText(userPhone);
            } else {
                textViewUserPhone.setText("Chưa cập nhật");
            }
            
            // Display full address: detailed address + fullAddressText
            String address = user.getAddress() != null ? user.getAddress() : "";
            int provinceId = user.getProvinceid();
            int districtId = user.getDistrictid();
            int wardId = user.getWardid();

            if (provinceId > 0 && districtId > 0 && wardId > 0) {
                loadFullAddress(provinceId, districtId, String.valueOf(wardId), address);
            } else {
                // Fallback to stored address only
                textViewUserAddress.setText(address.isEmpty() ? "Chưa cập nhật địa chỉ" : address);
            }
        }
    }

    private void loadFullAddress(int provinceId, int districtId, String wardCode, final String address) {
        Call<FullAddressResponse> call = apiService.getFullAddress(provinceId, districtId, wardCode);
        call.enqueue(new Callback<FullAddressResponse>() {
            @Override
            public void onResponse(Call<FullAddressResponse> call, Response<FullAddressResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FullAddressResponse full = response.body();
                    String fullText = full.getFullAddressText();
                    if (fullText == null) fullText = "";
                    String combined = address != null && !address.isEmpty() ? address + ", " + fullText : fullText;
                    textViewUserAddress.setText(combined);
                } else {
                    textViewUserAddress.setText(address != null && !address.isEmpty() ? address : "Chưa cập nhật địa chỉ");
                }
            }

            @Override
            public void onFailure(Call<FullAddressResponse> call, Throwable t) {
                textViewUserAddress.setText(address != null && !address.isEmpty() ? address : "Chưa cập nhật địa chỉ");
            }
        });
    }
    
    private void setupClickListeners() {
        // Back button
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        // Change address click listener
        textViewChangeAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to PersonalInfoActivity
                Intent intent = new Intent(ConfirmOrderActivity.this, PersonalInfoActivity.class);
                startActivity(intent);
            }
        });
        
        // Confirm order button
        buttonConfirmOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ConfirmOrderActivity.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                // TODO: Implement order confirmation logic
            }
        });
    }
    
    private void setupRecyclerView() {
        recyclerViewOrderItems.setLayoutManager(new LinearLayoutManager(this));
        cartItemAdapter = new CartItemAdapter(items);
        // Disable actions on confirm screen
        cartItemAdapter.setActionsEnabled(false);
        recyclerViewOrderItems.setAdapter(cartItemAdapter);
    }
    
    private void loadCartItems() {
        if (cartId <= 0) return;
        apiService.getCartItems(cartId).enqueue(new Callback<CartItemsResponse>() {
            @Override
            public void onResponse(Call<CartItemsResponse> call, Response<CartItemsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    java.util.List<CartItem> data = response.body().getData();
                    items.clear();
                    items.addAll(data);
                    // load product details to get image and current price/name
                    loadProductDetails(items);
                }
            }

            @Override
            public void onFailure(Call<CartItemsResponse> call, Throwable t) {
            }
        });
    }
    
    private void computeTotals() {
        double subtotal = 0;
        for (CartItem item : items) {
            subtotal += item.getPrice() * item.getQuantity();
        }
        double shipping = items.isEmpty() ? 0 : 30000; // sample flat shipping
        double total = subtotal + shipping;
        
        textViewSubtotal.setText(String.format("%,.0fđ", subtotal));
        textViewShipping.setText(String.format("%,.0fđ", shipping));
        textViewTotal.setText(String.format("%,.0fđ", total));
    }

    private void loadProductDetails(java.util.List<CartItem> list) {
        for (CartItem ci : list) {
            Call<com.example.kibo.models.ProductResponse> call = apiService.getProductById(ci.getProductId());
            call.enqueue(new Callback<com.example.kibo.models.ProductResponse>() {
                @Override
                public void onResponse(Call<com.example.kibo.models.ProductResponse> call, Response<com.example.kibo.models.ProductResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                        com.example.kibo.models.Product p = response.body().getData().get(0);
                        ci.setProductName(p.getProductName());
                        ci.setPrice(p.getPrice());
                        ci.setImageUrl(p.getImageUrl());
                        cartItemAdapter.updateCartItems(items);
                        computeTotals();
                    }
                }

                @Override
                public void onFailure(Call<com.example.kibo.models.ProductResponse> call, Throwable t) {
                }
            });
        }
        // initial paint
        cartItemAdapter.updateCartItems(items);
        computeTotals();
    }

    private void showEditQuantityDialog(CartItem item) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_quantity, null);
        TextView textProductName = dialogView.findViewById(R.id.text_product_name);
        TextView textQuantity = dialogView.findViewById(R.id.text_quantity);
        View btnDecrease = dialogView.findViewById(R.id.button_decrease);
        View btnIncrease = dialogView.findViewById(R.id.button_increase);
        View btnCancel = dialogView.findViewById(R.id.button_cancel);
        View btnConfirm = dialogView.findViewById(R.id.button_confirm);

        textProductName.setText(item.getProductName());
        textQuantity.setText(String.valueOf(item.getQuantity()));

        final android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnDecrease.setOnClickListener(v -> {
            int q = Integer.parseInt(textQuantity.getText().toString());
            if (q > 1) textQuantity.setText(String.valueOf(q - 1));
        });
        btnIncrease.setOnClickListener(v -> {
            int q = Integer.parseInt(textQuantity.getText().toString());
            textQuantity.setText(String.valueOf(q + 1));
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            int newQuantity = Integer.parseInt(textQuantity.getText().toString());
            updateQuantity(item.getProductId(), newQuantity);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateQuantity(int productId, int quantity) {
        if (cartId <= 0) return;
        com.example.kibo.models.UpdateQuantityRequest req = new com.example.kibo.models.UpdateQuantityRequest(cartId, productId, quantity);
        apiService.updateCartItemQuantity(req).enqueue(new Callback<com.example.kibo.models.ApiResponse<String>>() {
            @Override
            public void onResponse(Call<com.example.kibo.models.ApiResponse<String>> call, Response<com.example.kibo.models.ApiResponse<String>> response) {
                if (response.isSuccessful()) {
                    loadCartItems();
                }
            }

            @Override
            public void onFailure(Call<com.example.kibo.models.ApiResponse<String>> call, Throwable t) {
            }
        });
    }

    private void confirmRemove(CartItem item) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc muốn xóa '" + item.getProductName() + "' khỏi giỏ hàng?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) -> removeItem(item))
                .show();
    }

    private void removeItem(CartItem item) {
        if (cartId <= 0) return;
        com.example.kibo.models.RemoveCartItemRequest req = new com.example.kibo.models.RemoveCartItemRequest(cartId, item.getProductId());
        apiService.removeCartItem(req).enqueue(new Callback<com.example.kibo.models.ApiResponse<String>>() {
            @Override
            public void onResponse(Call<com.example.kibo.models.ApiResponse<String>> call, Response<com.example.kibo.models.ApiResponse<String>> response) {
                if (response.isSuccessful()) loadCartItems();
            }

            @Override
            public void onFailure(Call<com.example.kibo.models.ApiResponse<String>> call, Throwable t) { }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload user data when returning from personal info
        loadUserData();
    }
}
