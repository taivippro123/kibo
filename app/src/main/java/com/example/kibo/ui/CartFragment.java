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
import com.example.kibo.models.Cart;
import com.example.kibo.models.Product;
import com.example.kibo.models.ProductResponse;
import com.example.kibo.models.ProductImage;
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
    private TextView textViewTotalPrice;
    private Button buttonContinue;
    private Button buttonClearCart;

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

        // Check for active cart and load items (no automatic cart creation)
        checkActiveCartAndLoad();

        return view;
    }

    /**
     * Check if there is an active cart (status=1) for current user.
     * If found, use it. Otherwise, show empty state.
     * Cart will only be created when user adds product to cart from ProductDetailActivity.
     */
    private void checkActiveCartAndLoad() {
        if (!sessionManager.isLoggedIn()) {
            showEmptyState();
            return;
        }
        
        int userId = sessionManager.getUserId();
        Log.d(TAG, "checkActiveCartAndLoad: checking carts for userId=" + userId);
        
        apiService.getCarts(userId, 100).enqueue(new Callback<com.example.kibo.models.PaginationResponse<com.example.kibo.models.Cart>>() {
            @Override
            public void onResponse(Call<com.example.kibo.models.PaginationResponse<com.example.kibo.models.Cart>> call, 
                    Response<com.example.kibo.models.PaginationResponse<com.example.kibo.models.Cart>> response) {
                if (!isAdded()) return;
                
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    java.util.List<com.example.kibo.models.Cart> carts = response.body().getData();
                    Log.d(TAG, "Fetched " + carts.size() + " carts for user=" + userId);
                    
                    com.example.kibo.models.Cart activeCart = null;
                    for (com.example.kibo.models.Cart c : carts) {
                        Log.d(TAG, "Cart id=" + c.getCartId() + ", status=" + c.getStatus() + " (" + c.getStatusName() + ")");
                        if (c.getStatus() == 1) { 
                            activeCart = c; 
                            break; 
                        }
                    }

                    // If we found an active cart (status=1), use it
                    if (activeCart != null) {
                        Log.d(TAG, "Using existing active cart (status=1), id=" + activeCart.getCartId());
                        sessionManager.setActiveCartId(activeCart.getCartId());
                        loadCartItems();
                    } else {
                        // No active cart found - show empty state
                        // Cart will be created when user adds product to cart
                        Log.d(TAG, "No active cart found. Showing empty state.");
                        showEmptyState();
                    }
                } else {
                    // API call failed - check if we have stored active cart
                    Log.w(TAG, "getCarts failed (" + (response != null ? response.code() : -1) + ")");
                    if (sessionManager.hasActiveCart()) {
                        Log.w(TAG, "Using stored active cart id=" + sessionManager.getActiveCartId());
                        loadCartItems();
                    } else {
                        showEmptyState();
                    }
                }
            }

            @Override
            public void onFailure(Call<com.example.kibo.models.PaginationResponse<com.example.kibo.models.Cart>> call, Throwable t) {
                if (!isAdded()) return;
                // Network error; attempt to proceed with any stored active cart
                Log.e(TAG, "getCarts error: " + t.getMessage());
                if (sessionManager.hasActiveCart()) {
                    Log.w(TAG, "Using stored active cart id=" + sessionManager.getActiveCartId());
                    loadCartItems();
                } else {
                    showEmptyState();
                }
            }
        });
    }

    private void initViews(View view) {
        recyclerViewCartItems = view.findViewById(R.id.recycler_view_cart_items);
        layoutEmptyCart = view.findViewById(R.id.layout_empty_cart);
        textViewEmptyCart = view.findViewById(R.id.text_view_empty_cart);
        textViewItemCount = view.findViewById(R.id.text_view_item_count);
        textViewTotalPrice = view.findViewById(R.id.text_view_total_price);
        buttonContinue = view.findViewById(R.id.button_continue);
        buttonClearCart = view.findViewById(R.id.button_clear_cart);
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

        // Clear cart button click listener
        buttonClearCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmClearCart();
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
            if (q > 1)
                textQuantity.setText(String.valueOf(q - 1));
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
        if (!sessionManager.hasActiveCart())
            return;
        int cartId = sessionManager.getActiveCartId();
        UpdateQuantityRequest request = new UpdateQuantityRequest(cartId, productId, quantity);
        apiService.updateCartItemQuantity(request)
                .enqueue(new retrofit2.Callback<com.example.kibo.models.ApiResponse<String>>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.kibo.models.ApiResponse<String>> call,
                            retrofit2.Response<com.example.kibo.models.ApiResponse<String>> response) {
                        if (response.isSuccessful()) {
                            // reload cart items
                            loadCartItems();
                        } else {
                            Log.e(TAG, "Update quantity failed: " + response.code());
                            Toast.makeText(requireContext(), "Không thể cập nhật số lượng", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.kibo.models.ApiResponse<String>> call,
                            Throwable t) {
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
        if (!sessionManager.hasActiveCart())
            return;
        int cartId = sessionManager.getActiveCartId();
        com.example.kibo.models.RemoveCartItemRequest request = new com.example.kibo.models.RemoveCartItemRequest(
                cartId, item.getProductId());
        apiService.removeCartItem(request)
                .enqueue(new retrofit2.Callback<com.example.kibo.models.ApiResponse<String>>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.kibo.models.ApiResponse<String>> call,
                            retrofit2.Response<com.example.kibo.models.ApiResponse<String>> response) {
                        if (response.isSuccessful()) {
                            loadCartItems();
                        } else {
                            Log.e(TAG, "Remove item failed: " + response.code());
                            Toast.makeText(requireContext(), "Không thể xóa sản phẩm", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.kibo.models.ApiResponse<String>> call,
                            Throwable t) {
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
            // No active cart - show empty state
            // Cart will be created when user adds product to cart
            showEmptyState();
            return;
        }

        int cartId = sessionManager.getActiveCartId();
        Log.d(TAG, "Loading cart items for cart ID: " + cartId);

        Call<CartItemsResponse> call = apiService.getCartItems(cartId);
        call.enqueue(new Callback<CartItemsResponse>() {
            @Override
            public void onResponse(Call<CartItemsResponse> call, Response<CartItemsResponse> response) {
                if (!isAdded()) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    CartItemsResponse cartItemsResponse = response.body();
                    List<CartItem> items = cartItemsResponse.getData();

                    if (items == null || items.isEmpty()) {
                        Log.d(TAG, "Cart is empty");
                        showEmptyState();
                    } else {
                        // API already returns productName and price, use them directly
                        cartItems.clear();
                        cartItems.addAll(items);
                        
                        // Load imageUrl for items that don't have it (if needed)
                        loadMissingProductImages();
                        
                        // Update adapter and show cart items
                        cartItemAdapter.updateCartItems(cartItems);
                        showCartItems();
                        
                        Log.d(TAG, "Loaded " + items.size() + " cart items");
                    }
                } else {
                    Log.e(TAG, "Failed to load cart items: " + (response != null ? response.code() : -1));
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<CartItemsResponse> call, Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "Error loading cart items: " + t.getMessage());
                showEmptyState();
            }
        });
    }

    /**
     * Load product images for cart items that don't have imageUrl yet.
     * This is optional - if API already returns imageUrl, this won't be called.
     */
    private void loadMissingProductImages() {
        for (CartItem cartItem : cartItems) {
            // Only load image if it's missing
            if (cartItem.getImageUrl() == null || cartItem.getImageUrl().isEmpty()) {
                loadProductImage(cartItem);
            }
        }
    }

    private void loadProductImage(CartItem cartItem) {
        apiService.getProductImages(cartItem.getProductId()).enqueue(new Callback<java.util.List<ProductImage>>() {
            @Override
            public void onResponse(Call<java.util.List<ProductImage>> call, Response<java.util.List<ProductImage>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    java.util.List<ProductImage> images = response.body();
                    String chosenUrl = null;
                    for (ProductImage img : images) {
                        if (img.isPrimary()) { chosenUrl = img.getImageUrl(); break; }
                    }
                    if (chosenUrl == null) {
                        chosenUrl = images.get(0).getImageUrl();
                    }
                    cartItem.setImageUrl(chosenUrl);
                    cartItemAdapter.updateCartItems(cartItems);
                    Log.d(TAG, "Loaded image via ProductImages for productId=" + cartItem.getProductId());
                } else {
                    Log.w(TAG, "No product images for productId=" + cartItem.getProductId());
                }
            }

            @Override
            public void onFailure(Call<java.util.List<ProductImage>> call, Throwable t) {
                if (!isAdded()) return;
                Log.w(TAG, "Error loading product images: " + t.getMessage());
            }
        });
    }

    private void confirmClearCart() {
        if (!sessionManager.hasActiveCart()) {
            return;
        }
        
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Xóa tất cả sản phẩm")
                .setMessage("Bạn có chắc muốn xóa tất cả sản phẩm khỏi giỏ hàng?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) -> clearCart())
                .show();
    }

    private void clearCart() {
        if (!sessionManager.hasActiveCart()) {
            return;
        }
        
        int cartId = sessionManager.getActiveCartId();
        apiService.clearCart(cartId).enqueue(new Callback<com.example.kibo.models.ApiResponse<String>>() {
            @Override
            public void onResponse(Call<com.example.kibo.models.ApiResponse<String>> call,
                    Response<com.example.kibo.models.ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(requireContext(), "Đã xóa tất cả sản phẩm khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                    loadCartItems(); // Reload to show empty state
                } else {
                    Toast.makeText(requireContext(), "Không thể xóa giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.example.kibo.models.ApiResponse<String>> call, Throwable t) {
                Log.e(TAG, "Clear cart error: " + t.getMessage());
                Toast.makeText(requireContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateAndDisplayTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        textViewTotalPrice.setText(String.format("%,.0fđ", total));
    }

    private void showEmptyState() {
        // Show empty state and hide cart items
        layoutEmptyCart.setVisibility(View.VISIBLE);
        recyclerViewCartItems.setVisibility(View.GONE);
        buttonContinue.setVisibility(View.GONE);
        buttonClearCart.setVisibility(View.GONE);
        textViewItemCount.setText("0 sản phẩm");
        textViewTotalPrice.setText("0đ");
        if (getActivity() instanceof com.example.kibo.MainActivity) {
            ((com.example.kibo.MainActivity) getActivity()).updateCartBadge(0);
        }
    }

    private void showCartItems() {
        // Show cart items and hide empty state
        layoutEmptyCart.setVisibility(View.GONE);
        recyclerViewCartItems.setVisibility(View.VISIBLE);
        buttonContinue.setVisibility(View.VISIBLE);
        buttonClearCart.setVisibility(View.VISIBLE);

        // Update item count
        int totalItems = cartItems.size();
        textViewItemCount.setText(totalItems + " sản phẩm");

        // Calculate and display total price
        calculateAndDisplayTotal();

        // Update badge on bottom navigation
        if (getActivity() instanceof com.example.kibo.MainActivity) {
            ((com.example.kibo.MainActivity) getActivity()).updateCartBadge(totalItems);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // After returning (e.g., post-payment), re-check active cart from server
        checkActiveCartAndLoad();
    }
}
