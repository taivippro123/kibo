package com.example.kibo.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kibo.ConfirmOrderActivity;
import com.example.kibo.R;
import com.example.kibo.adapters.CartItemAdapter;
import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.CartItem;
import com.example.kibo.models.CartItemsResponse;
import com.example.kibo.models.Product;
import com.example.kibo.models.ProductResponse;
import com.example.kibo.models.UpdateQuantityRequest;
import com.example.kibo.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartFragment extends Fragment {
    
    private static final String TAG = "CartFragment";
    
    private SessionManager sessionManager;
    private ApiService apiService;
    private RecyclerView recyclerViewCartItems;
    private LinearLayout layoutEmptyCart;
    private TextView textViewEmptyCart;
    private TextView textViewItemCount;
    private Button buttonContinue;
    
    private CartItemAdapter cartItemAdapter;
    private List<CartItem> cartItems = new ArrayList<>();
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        
        // Initialize services
        sessionManager = new SessionManager(requireContext());
        apiService = ApiClient.getApiService();
        
        // Initialize views
        initViews(view);
        
        // Setup recycler view
        setupRecyclerView();
        
        // Setup click listeners
        setupClickListeners();
        
        // Load cart items
        loadCartItems();
        
        return view;
    }
    
    private void initViews(View view) {
        recyclerViewCartItems = view.findViewById(R.id.recycler_view_cart_items);
        layoutEmptyCart = view.findViewById(R.id.layout_empty_cart);
        textViewEmptyCart = view.findViewById(R.id.text_view_empty_cart);
        textViewItemCount = view.findViewById(R.id.text_view_item_count);
        buttonContinue = view.findViewById(R.id.button_continue);
    }
    
    private void setupRecyclerView() {
        cartItemAdapter = new CartItemAdapter(cartItems);
        cartItemAdapter.setOnQuantityClickListener(new CartItemAdapter.OnQuantityClickListener() {
            @Override
            public void onQuantityClick(CartItem item) {
                showEditQuantityDialog(item);
            }

            @Override
            public void onRemoveClick(CartItem item) {
                confirmRemove(item);
            }
        });
        recyclerViewCartItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewCartItems.setAdapter(cartItemAdapter);
    }
    
    private void setupClickListeners() {
        // Continue button click listener
        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ConfirmOrderActivity
                Intent intent = new Intent(requireContext(), ConfirmOrderActivity.class);
                if (sessionManager.hasActiveCart()) {
                    intent.putExtra("cart_id", sessionManager.getActiveCartId());
                }
                startActivity(intent);
            }
        });
    }
    
    private void showEditQuantityDialog(CartItem item) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_quantity, null);
        TextView textProductName = dialogView.findViewById(R.id.text_product_name);
        TextView textQuantity = dialogView.findViewById(R.id.text_quantity);
        View btnDecrease = dialogView.findViewById(R.id.button_decrease);
        View btnIncrease = dialogView.findViewById(R.id.button_increase);
        View btnCancel = dialogView.findViewById(R.id.button_cancel);
        View btnConfirm = dialogView.findViewById(R.id.button_confirm);

        textProductName.setText(item.getProductName());
        textQuantity.setText(String.valueOf(item.getQuantity()));

        final android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext())
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
        if (!sessionManager.hasActiveCart()) return;
        int cartId = sessionManager.getActiveCartId();
        UpdateQuantityRequest request = new UpdateQuantityRequest(cartId, productId, quantity);
        apiService.updateCartItemQuantity(request).enqueue(new retrofit2.Callback<com.example.kibo.models.ApiResponse<String>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.kibo.models.ApiResponse<String>> call, retrofit2.Response<com.example.kibo.models.ApiResponse<String>> response) {
                if (response.isSuccessful()) {
                    // reload cart items
                    loadCartItems();
                } else {
                    Log.e(TAG, "Update quantity failed: " + response.code());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.kibo.models.ApiResponse<String>> call, Throwable t) {
                Log.e(TAG, "Update quantity error: " + t.getMessage());
            }
        });
    }

    private void confirmRemove(CartItem item) {
        new android.app.AlertDialog.Builder(requireContext())
            .setTitle("Xóa sản phẩm")
            .setMessage("Bạn có chắc muốn xóa '" + item.getProductName() + "' khỏi giỏ hàng?")
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Xóa", (d, w) -> removeItem(item))
            .show();
    }

    private void removeItem(CartItem item) {
        if (!sessionManager.hasActiveCart()) return;
        int cartId = sessionManager.getActiveCartId();
        com.example.kibo.models.RemoveCartItemRequest request = new com.example.kibo.models.RemoveCartItemRequest(cartId, item.getProductId());
        apiService.removeCartItem(request).enqueue(new retrofit2.Callback<com.example.kibo.models.ApiResponse<String>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.kibo.models.ApiResponse<String>> call, retrofit2.Response<com.example.kibo.models.ApiResponse<String>> response) {
                if (response.isSuccessful()) {
                    loadCartItems();
                } else {
                    Log.e(TAG, "Remove item failed: " + response.code());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.kibo.models.ApiResponse<String>> call, Throwable t) {
                Log.e(TAG, "Remove item error: " + t.getMessage());
            }
        });
    }

    private void loadCartItems() {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            showEmptyState();
            return;
        }
        
        // Check if user has an active cart
        if (!sessionManager.hasActiveCart()) {
            showEmptyState();
            return;
        }
        
        int cartId = sessionManager.getActiveCartId();
        Log.d(TAG, "Loading cart items for cart ID: " + cartId);
        
        Call<CartItemsResponse> call = apiService.getCartItems(cartId);
        call.enqueue(new Callback<CartItemsResponse>() {
            @Override
            public void onResponse(Call<CartItemsResponse> call, Response<CartItemsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CartItemsResponse cartItemsResponse = response.body();
                    List<CartItem> items = cartItemsResponse.getData();
                    
                    if (items.isEmpty()) {
                        showEmptyState();
                    } else {
                        // Load detailed product info for each cart item
                        loadProductDetails(items);
                    }
                    
                    Log.d(TAG, "Loaded " + items.size() + " cart items");
                } else {
                    Log.e(TAG, "Failed to load cart items: " + response.code());
                    showEmptyState();
                }
            }
            
            @Override
            public void onFailure(Call<CartItemsResponse> call, Throwable t) {
                Log.e(TAG, "Error loading cart items: " + t.getMessage());
                showEmptyState();
            }
        });
    }
    
    private void loadProductDetails(List<CartItem> items) {
        cartItems.clear();
        cartItems.addAll(items);
        
        // Load product details for each item
        for (CartItem cartItem : cartItems) {
            loadProductDetail(cartItem);
        }
        
        showCartItems();
    }
    
    private void loadProductDetail(CartItem cartItem) {
        Call<ProductResponse> call = apiService.getProductById(cartItem.getProductId());
        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProductResponse productResponse = response.body();
                    if (productResponse.getData() != null && !productResponse.getData().isEmpty()) {
                        Product product = productResponse.getData().get(0);
                        
                        // Update cart item with product details
                        cartItem.setProductName(product.getProductName());
                        cartItem.setPrice(product.getPrice());
                        cartItem.setImageUrl(product.getImageUrl());
                        
                        // Update adapter
                        cartItemAdapter.updateCartItems(cartItems);
                        
                        Log.d(TAG, "Loaded product details for: " + product.getProductName());
                    }
                } else {
                    Log.e(TAG, "Failed to load product details for ID: " + cartItem.getProductId());
                }
            }
            
            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Log.e(TAG, "Error loading product details: " + t.getMessage());
            }
        });
    }
    
    private void showEmptyState() {
        // Show empty state and hide cart items
        layoutEmptyCart.setVisibility(View.VISIBLE);
        recyclerViewCartItems.setVisibility(View.GONE);
        buttonContinue.setVisibility(View.GONE);
        textViewItemCount.setText("0 sản phẩm");
        if (getActivity() instanceof com.example.kibo.MainActivity) {
            ((com.example.kibo.MainActivity) getActivity()).updateCartBadge(0);
        }
    }
    
    private void showCartItems() {
        // Show cart items and hide empty state
        layoutEmptyCart.setVisibility(View.GONE);
        recyclerViewCartItems.setVisibility(View.VISIBLE);
        buttonContinue.setVisibility(View.VISIBLE);
        
        // Update item count
        int totalItems = cartItems.size();
        textViewItemCount.setText(totalItems + " sản phẩm");

        // Update badge on bottom navigation
        if (getActivity() instanceof com.example.kibo.MainActivity) {
            ((com.example.kibo.MainActivity) getActivity()).updateCartBadge(totalItems);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload cart items when returning to this fragment
        loadCartItems();
    }
}
