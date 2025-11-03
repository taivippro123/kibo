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
import com.example.kibo.models.CartRequest;
import com.example.kibo.models.Cart;
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

        // Ensure we have a valid active cart before loading items
        ensureActiveCartAndThen(this::loadCartItems);

        return view;
    }

    private interface VoidCallback { void run(); }

    /**
     * Ensure there is an active cart with status=1 for current user.
     * If current active cart is completed (status=2) or none exists, create a new one.
     */
    private void ensureActiveCartAndThen(VoidCallback next) {
        if (!sessionManager.isLoggedIn()) { showEmptyState(); return; }
        int userId = sessionManager.getUserId();
        Log.d(TAG, "ensureActiveCartAndThen: checking carts for userId=" + userId);
        apiService.getCarts(userId).enqueue(new Callback<com.example.kibo.models.PaginationResponse<com.example.kibo.models.Cart>>() {
            @Override
            public void onResponse(Call<com.example.kibo.models.PaginationResponse<com.example.kibo.models.Cart>> call, Response<com.example.kibo.models.PaginationResponse<com.example.kibo.models.Cart>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    java.util.List<com.example.kibo.models.Cart> carts = response.body().getData();
                    Log.d(TAG, "Fetched " + carts.size() + " carts for user=" + userId);
                    com.example.kibo.models.Cart activeCart = null;
                    for (com.example.kibo.models.Cart c : carts) {
                        Log.d(TAG, "Cart id=" + c.getCartId() + ", status=" + c.getStatus() + " (" + c.getStatusName() + ")");
                        if (c.getStatus() == 1) { activeCart = c; break; }
                    }

                    // If we found an active cart (status=1), use it
                    if (activeCart != null) {
                        Log.d(TAG, "Using existing active cart (status=1), id=" + activeCart.getCartId());
                        sessionManager.setActiveCartId(activeCart.getCartId());
                        next.run();
                        return;
                    }

                    // If only completed carts exist (status=2) or none -> create a new cart
                    Log.d(TAG, "No active cart found. Creating a new cart with status=1.");
                    createNewActiveCart(userId, next);
                } else {
                    // Fallback: try to create a new cart
                    Log.w(TAG, "getCarts failed (" + (response != null ? response.code() : -1) + "), creating new cart.");
                    createNewActiveCart(userId, next);
                }
            }

            @Override
            public void onFailure(Call<com.example.kibo.models.PaginationResponse<com.example.kibo.models.Cart>> call, Throwable t) {
                if (!isAdded()) return;
                // Network error; attempt to proceed with any stored active cart
                Log.e(TAG, "getCarts error: " + t.getMessage());
                if (sessionManager.hasActiveCart()) {
                    Log.w(TAG, "Using stored active cart id=" + sessionManager.getActiveCartId());
                    next.run();
                } else {
                    showEmptyState();
                }
            }
        });
    }

    private void createNewActiveCart(int userId, VoidCallback next) {
        // Try creating with status=1 first; some backends may expect 3 → fallback
        attemptCreateCart(userId, 1, new VoidCallback() {
            @Override public void run() {
                // Verify by refetching carts; pick first with status!=2
                apiService.getCarts(userId).enqueue(new Callback<com.example.kibo.models.PaginationResponse<com.example.kibo.models.Cart>>() {
                    @Override public void onResponse(Call<com.example.kibo.models.PaginationResponse<com.example.kibo.models.Cart>> call, Response<com.example.kibo.models.PaginationResponse<com.example.kibo.models.Cart>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            com.example.kibo.models.Cart chosen = null;
                            for (com.example.kibo.models.Cart c : response.body().getData()) {
                                if (c.getStatus() != 2) { chosen = c; break; }
                            }
                            if (chosen != null) {
                                Log.d(TAG, "Verified active cart id=" + chosen.getCartId() + ", status=" + chosen.getStatus() + " (" + chosen.getStatusName() + ")");
                                sessionManager.setActiveCartId(chosen.getCartId());
                                next.run();
                            } else {
                                Log.w(TAG, "No non-completed cart after create status=1 → retry with status=3");
                                attemptCreateCart(userId, 3, () -> {
                                    // Fetch again and pick non-completed
                                    apiService.getCarts(userId).enqueue(new Callback<com.example.kibo.models.PaginationResponse<com.example.kibo.models.Cart>>() {
                                        @Override public void onResponse(Call<com.example.kibo.models.PaginationResponse<com.example.kibo.models.Cart>> call2, Response<com.example.kibo.models.PaginationResponse<com.example.kibo.models.Cart>> resp2) {
                                            if (!isAdded()) return;
                                            if (resp2.isSuccessful() && resp2.body() != null && resp2.body().getData() != null) {
                                                com.example.kibo.models.Cart chosen2 = null;
                                                for (com.example.kibo.models.Cart c2 : resp2.body().getData()) {
                                                    if (c2.getStatus() != 2) { chosen2 = c2; break; }
                                                }
                                                if (chosen2 != null) {
                                                    Log.d(TAG, "Verified active cart (retry) id=" + chosen2.getCartId());
                                                    sessionManager.setActiveCartId(chosen2.getCartId());
                                                    next.run();
                                                } else {
                                                    Log.e(TAG, "Still no active cart after retry");
                                                    showEmptyState();
                                                }
                                            } else {
                                                Log.e(TAG, "Refetch carts after retry failed");
                                                showEmptyState();
                                            }
                                        }
                                        @Override public void onFailure(Call<com.example.kibo.models.PaginationResponse<com.example.kibo.models.Cart>> call2, Throwable t2) {
                                            if (!isAdded()) return;
                                            Log.e(TAG, "Refetch carts error after retry: " + t2.getMessage());
                                            showEmptyState();
                                        }
                                    });
                                });
                            }
                        } else {
                            Log.e(TAG, "Refetch carts after create failed");
                            showEmptyState();
                        }
                    }
                    @Override public void onFailure(Call<com.example.kibo.models.PaginationResponse<com.example.kibo.models.Cart>> call, Throwable t) {
                        if (!isAdded()) return;
                        Log.e(TAG, "Refetch carts error: " + t.getMessage());
                        showEmptyState();
                    }
                });
            }
        });
    }

    private void attemptCreateCart(int userId, int status, VoidCallback onSuccess) {
        CartRequest req = new CartRequest(userId, status);
        Log.d(TAG, "Attempt creating cart with status=" + status);
        apiService.createCart(req).enqueue(new Callback<Cart>() {
            @Override public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "CreateCart success (status=" + status + ") id=" + response.body().getCartId());
                    onSuccess.run();
                } else {
                    Log.e(TAG, "CreateCart failed (status=" + status + ") code=" + (response != null ? response.code() : -1));
                    showEmptyState();
                }
            }
            @Override public void onFailure(Call<Cart> call, Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "CreateCart error (status=" + status + "): " + t.getMessage());
                showEmptyState();
            }
        });
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
            ensureActiveCartAndThen(this::loadCartItems);
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
        // After returning (e.g., post-payment), re-sync carts from server
        ensureActiveCartAndThen(this::loadCartItems);
    }
}
