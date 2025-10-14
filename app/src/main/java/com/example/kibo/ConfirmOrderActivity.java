package com.example.kibo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.LinearLayout;
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
import com.example.kibo.models.Voucher;
import com.example.kibo.models.VoucherUseResponse;
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
    private TextView textViewShippingFee;
    private LinearLayout layoutVoucherSelection;
    private TextView textViewVoucherCode;
    private TextView textViewVoucherDiscount;
    private LinearLayout layoutVoucherDiscount;
    private TextView textViewVoucherDiscountAmount;
    private RecyclerView recyclerViewOrderItems;
    private TextView textViewSubtotal;
    private TextView textViewShipping;
    private TextView textViewTotal;
    private Button buttonConfirmOrder;
    private RadioGroup radioGroupPayment;
    private RadioButton radioPaymentZaloPay;
    private RadioButton radioPaymentCash;
    
    private CartItemAdapter cartItemAdapter;
    private java.util.ArrayList<CartItem> items = new java.util.ArrayList<>();
    private int cartId;
    private double actualShippingFee = 0;
    private Voucher selectedVoucher;
    private double voucherDiscount = 0;
    private com.example.kibo.models.Product firstProduct; // Store first product for dimensions
    private int firstProductQuantity = 0;
    
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
        textViewShippingFee = findViewById(R.id.text_view_shipping_fee);
        layoutVoucherSelection = findViewById(R.id.layout_voucher_selection);
        textViewVoucherCode = findViewById(R.id.text_view_voucher_code);
        textViewVoucherDiscount = findViewById(R.id.text_view_voucher_discount);
        layoutVoucherDiscount = findViewById(R.id.layout_voucher_discount);
        textViewVoucherDiscountAmount = findViewById(R.id.text_view_voucher_discount_amount);
        recyclerViewOrderItems = findViewById(R.id.recycler_view_order_items);
        textViewSubtotal = findViewById(R.id.text_view_subtotal);
        textViewShipping = findViewById(R.id.text_view_shipping);
        textViewTotal = findViewById(R.id.text_view_total);
        buttonConfirmOrder = findViewById(R.id.button_confirm_order);
        radioGroupPayment = findViewById(R.id.radio_group_payment);
        radioPaymentZaloPay = findViewById(R.id.radio_payment_zalopay);
        radioPaymentCash = findViewById(R.id.radio_payment_cash);
        // Default select ZaloPay
        if (radioPaymentZaloPay != null) radioPaymentZaloPay.setChecked(true);
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
        
        // Voucher selection click listener
        layoutVoucherSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Calculate current order value
                double subtotal = 0;
                for (CartItem item : items) {
                    subtotal += item.getPrice() * item.getQuantity();
                }
                double orderValue = subtotal + actualShippingFee;
                
                // Navigate to VoucherSelectionActivity
                Intent intent = new Intent(ConfirmOrderActivity.this, VoucherSelectionActivity.class);
                intent.putExtra("order_value", orderValue);
                startActivityForResult(intent, 1001); // Request code for voucher selection
            }
        });
        
        // Confirm order button
        buttonConfirmOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createShippingOrder();
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
                    if (items.size() > 0) {
                        firstProductQuantity = items.get(0).getQuantity();
                    }
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
        double shipping = actualShippingFee; // Use actual shipping fee from API
        double total = subtotal + shipping - voucherDiscount; // Apply voucher discount
        
        textViewSubtotal.setText(String.format("%,.0fđ", subtotal));
        textViewShipping.setText(String.format("%,.0fđ", shipping));
        textViewTotal.setText(String.format("%,.0fđ", total));
        
        // Show/hide voucher discount line
        if (voucherDiscount > 0) {
            layoutVoucherDiscount.setVisibility(View.VISIBLE);
            textViewVoucherDiscountAmount.setText(String.format("-%,.0fđ", voucherDiscount));
        } else {
            layoutVoucherDiscount.setVisibility(View.GONE);
        }
    }

    private void loadProductDetails(java.util.List<CartItem> list) {
        final int[] loadedCount = {0};
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
                        
                        // Calculate shipping fee once we have the first product details
                        loadedCount[0]++;
                        if (loadedCount[0] == 1) {
                            firstProduct = p; // Store first product for order creation
                            calculateShippingFee(p);
                        }
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
    
    private void calculateShippingFee(com.example.kibo.models.Product firstProduct) {
        // Get user info to extract districtId and wardId
        User user = sessionManager.getUser();
        if (user == null) {
            textViewShippingFee.setText("Chưa thể tính");
            return;
        }
        
        int userId = user.getUserid();
        
        // Fetch user details from API to ensure we have latest district and ward info
        apiService.getUserById(userId).enqueue(new Callback<com.example.kibo.models.UserResponse>() {
            @Override
            public void onResponse(Call<com.example.kibo.models.UserResponse> call, Response<com.example.kibo.models.UserResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    User fetchedUser = response.body().getData().get(0);
                    int districtId = fetchedUser.getDistrictid();
                    int wardId = fetchedUser.getWardid();
                    
                    // Get product dimensions (from first product)
                    int width = firstProduct.getWidth();
                    int length = firstProduct.getLength();
                    int height = firstProduct.getHeight();
                    int weight = firstProduct.getWeight();
                    
                    // Default to 1000g (1kg) if weight is not set
                    if (weight <= 0) {
                        weight = 1000;
                    }
                    
                    // Create shipping fee request with serviceTypeId = 2
                    com.example.kibo.models.ShippingFeeRequest request = new com.example.kibo.models.ShippingFeeRequest(
                        2, // serviceTypeId
                        districtId,
                        String.valueOf(wardId),
                        height,
                        length,
                        width,
                        weight
                    );
                    
                    // Call shipping fee API
                    apiService.calculateShippingFee(request).enqueue(new Callback<com.example.kibo.models.ShippingFeeResponse>() {
                        @Override
                        public void onResponse(Call<com.example.kibo.models.ShippingFeeResponse> call, Response<com.example.kibo.models.ShippingFeeResponse> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                com.example.kibo.models.ShippingFeeData data = response.body().getData();
                                if (data != null) {
                                    actualShippingFee = data.getTotal();
                                    textViewShippingFee.setText(String.format("%,.0fđ", actualShippingFee));
                                    computeTotals(); // Recalculate totals with actual shipping fee
                                }
                            } else {
                                textViewShippingFee.setText("Không tính được");
                                actualShippingFee = 30000; // fallback
                                computeTotals();
                            }
                        }

                        @Override
                        public void onFailure(Call<com.example.kibo.models.ShippingFeeResponse> call, Throwable t) {
                            textViewShippingFee.setText("Không tính được");
                            actualShippingFee = 30000; // fallback
                            computeTotals();
                        }
                    });
                } else {
                    textViewShippingFee.setText("Không tính được");
                    actualShippingFee = 30000; // fallback
                    computeTotals();
                }
            }

            @Override
            public void onFailure(Call<com.example.kibo.models.UserResponse> call, Throwable t) {
                textViewShippingFee.setText("Không tính được");
                actualShippingFee = 30000; // fallback
                computeTotals();
            }
        });
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            // Handle voucher selection result
            Voucher selectedVoucher = (Voucher) data.getSerializableExtra("selected_voucher");
            if (selectedVoucher != null) {
                this.selectedVoucher = selectedVoucher;
                useVoucher(selectedVoucher.getCode());
            }
        }
    }
    
    private void useVoucher(String voucherCode) {
        // Calculate current order value
        double subtotal = 0;
        for (CartItem item : items) {
            subtotal += item.getPrice() * item.getQuantity();
        }
        double orderValue = subtotal + actualShippingFee;
        
        // Send only orderValue as decimal number
        apiService.useVoucher(voucherCode, orderValue).enqueue(new Callback<VoucherUseResponse>() {
            @Override
            public void onResponse(Call<VoucherUseResponse> call, Response<VoucherUseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    VoucherUseResponse.VoucherUseData data = response.body().getData();
                    if (data != null) {
                        voucherDiscount = data.getDiscountAmount();
                        
                        // Update UI
                        textViewVoucherCode.setText(selectedVoucher.getCode());
                        textViewVoucherDiscount.setText(selectedVoucher.getDiscountDisplayText());
                        textViewVoucherDiscount.setVisibility(View.VISIBLE);
                        
                        // Recalculate totals
                        computeTotals();
                        
                        Toast.makeText(ConfirmOrderActivity.this, "Áp dụng mã giảm giá thành công!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMessage = "Không thể áp dụng mã giảm giá";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMessage = response.body().getMessage();
                    }
                    Toast.makeText(ConfirmOrderActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<VoucherUseResponse> call, Throwable t) {
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void createShippingOrder() {
        // Validate user info
        User user = sessionManager.getUser();
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (firstProduct == null) {
            Toast.makeText(this, "Chưa có thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get user address info
        String toName = user.getUsername();
        String toPhone = user.getPhonenumber();
        String toAddress = user.getAddress();
        int toDistrictId = user.getDistrictid();
        int toWardId = user.getWardid();
        String toWardCode = String.valueOf(toWardId);
        int userId = user.getUserid();
        
        // Get product dimensions
        int weight = firstProduct.getWeight() > 0 ? firstProduct.getWeight() : 1000; // Default 1kg
        int length = firstProduct.getLength() > 0 ? firstProduct.getLength() : 10;
        int width = firstProduct.getWidth() > 0 ? firstProduct.getWidth() : 10;
        int height = firstProduct.getHeight() > 0 ? firstProduct.getHeight() : 10;

        // Determine payment method: 1=bank(ZaloPay), 2=cash
        int paymentMethod = 1;
        if (radioPaymentCash != null && radioPaymentCash.isChecked()) {
            paymentMethod = 2;
        }
        
        // Create shipping order request
        com.example.kibo.models.ShippingOrderRequest request = new com.example.kibo.models.ShippingOrderRequest(
            toName, toPhone, toAddress,
            toWardCode, toDistrictId,
            weight, length, width, height,
            2, // serviceTypeId
            2, // paymentTypeId
            0, // codAmount (0 if prepaid)
            0, // insuranceValue
            null, // content
            null, // note
            null, // requiredNote
            paymentMethod, // 1 bank (ZaloPay), 2 cash
            actualShippingFee, // shippingFee
            userId,
            cartId,
            firstProduct.getProductId(),
            firstProductQuantity > 0 ? firstProductQuantity : 1
        );
        
        // Show loading
        buttonConfirmOrder.setEnabled(false);
        buttonConfirmOrder.setText("Đang xử lý...");
        
        // Call API
        apiService.createShippingOrder(request).enqueue(new Callback<com.example.kibo.models.ShippingOrderResponse>() {
            @Override
            public void onResponse(Call<com.example.kibo.models.ShippingOrderResponse> call, Response<com.example.kibo.models.ShippingOrderResponse> response) {
                buttonConfirmOrder.setEnabled(true);
                buttonConfirmOrder.setText("THANH TOÁN");
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    com.example.kibo.models.ShippingOrderResponse.ShippingOrderData data = response.body().getData();
                    String message = "Đặt hàng thành công!";
                    if (data != null && data.getOrderCode() != null) {
                        message += "\nMã đơn hàng: " + data.getOrderCode();
                    }
                    
                    Toast.makeText(ConfirmOrderActivity.this, message, Toast.LENGTH_LONG).show();
                    
                    // Navigate to Orders tab
                    android.content.Intent intent = new android.content.Intent(ConfirmOrderActivity.this, MainActivity.class);
                    intent.putExtra("selected_tab", 1); // Orders tab index
                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP | android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    String errorMessage = "Không thể tạo đơn hàng";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMessage = response.body().getMessage();
                    }
                    Toast.makeText(ConfirmOrderActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.example.kibo.models.ShippingOrderResponse> call, Throwable t) {
                buttonConfirmOrder.setEnabled(true);
                buttonConfirmOrder.setText("THANH TOÁN");
                Toast.makeText(ConfirmOrderActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload user data when returning from personal info
        loadUserData();
    }
}
