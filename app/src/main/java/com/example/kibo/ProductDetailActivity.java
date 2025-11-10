package com.example.kibo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.Product;
import com.example.kibo.models.ProductResponse;
import com.example.kibo.models.ProductImage;
import com.example.kibo.models.Cart;
import com.example.kibo.models.CartRequest;
import com.example.kibo.models.CartItemRequest;
import com.example.kibo.models.ApiResponse;
import com.example.kibo.models.AddToWishlistRequest;
import com.example.kibo.models.WishlistResponse;
import com.example.kibo.utils.SessionManager;
import com.example.kibo.adapters.ProductImageAdapter;
import androidx.viewpager2.widget.ViewPager2;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {
    private static final String TAG = "ProductDetailActivity";
    public static final String EXTRA_PRODUCT_ID = "product_id";

    private ViewPager2 viewPagerImages;
    private LinearLayout layoutIndicators;
    private TextView tvImageCounter;
    private ProductImageAdapter imageAdapter;
    private List<ProductImage> productImages = new ArrayList<>();

    private TextView tvProductName;
    private TextView tvPrice;
    private TextView tvBriefDescription;
    private TextView tvFullDescription;
    private TextView tvConnection;
    private TextView tvLayout;
    private TextView tvKeycap;
    private TextView tvSwitch;
    private TextView tvBattery;
    private TextView tvOs;
    private TextView tvLed;
    private TextView tvScreen;
    private TextView tvCategoryName;
    private TextView tvDimensions;
    private ImageButton btnBack;
    private LinearLayout btnChat;
    private Button btnBuy;
    private FrameLayout loadingLayout;
    private ImageButton btnAddToWishlist;

    // Quantity controls
    private Button btnDecrease;
    private Button btnIncrease;
    private TextView tvQuantity;
    private TextView tvTotalPrice;
    private int currentQuantity = 1;
    private double unitPrice = 0.0;

    private ApiService apiService;
    private SessionManager sessionManager;
    private int currentProductId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Initialize services
        apiService = ApiClient.getApiService();
        sessionManager = new SessionManager(this);

        initViews();

        currentProductId = getIntent().getIntExtra(EXTRA_PRODUCT_ID, -1);

        if (currentProductId != -1) {
            loadProductDetail(currentProductId);
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnBack.setOnClickListener(v -> finish());

        btnChat.setOnClickListener(v -> {
            Toast.makeText(this, "Mở chat với shop", Toast.LENGTH_SHORT).show();
            // TODO: Implement chat logic
        });

        btnBuy.setOnClickListener(v -> {
            buyNow();
        });
    }

    private void initViews() {
        viewPagerImages = findViewById(R.id.view_pager_images);
        layoutIndicators = findViewById(R.id.layout_indicators);
        tvImageCounter = findViewById(R.id.tv_image_counter);
        tvProductName = findViewById(R.id.tv_product_name);
        tvPrice = findViewById(R.id.tv_product_price);
        tvBriefDescription = findViewById(R.id.tv_brief_description);
        tvFullDescription = findViewById(R.id.tv_full_description);
        tvConnection = findViewById(R.id.tv_connection);
        tvLayout = findViewById(R.id.tv_layout);
        tvKeycap = findViewById(R.id.tv_keycap);
        tvSwitch = findViewById(R.id.tv_switch);
        tvBattery = findViewById(R.id.tv_battery);
        tvOs = findViewById(R.id.tv_os);
        tvLed = findViewById(R.id.tv_led);
        tvScreen = findViewById(R.id.tv_screen);
        tvCategoryName = findViewById(R.id.tv_category_name);
        tvDimensions = findViewById(R.id.tv_dimensions);
        btnBack = findViewById(R.id.btn_back);
        btnChat = findViewById(R.id.btn_chat);
        btnBuy = findViewById(R.id.btn_buy);
        loadingLayout = findViewById(R.id.loading_layout);
        btnAddToWishlist = findViewById(R.id.btn_add_to_wishlist);

        // Initialize quantity controls
        btnDecrease = findViewById(R.id.button_decrease);
        btnIncrease = findViewById(R.id.button_increase);
        tvQuantity = findViewById(R.id.text_quantity);
        tvTotalPrice = findViewById(R.id.tv_total_price);

        // Setup quantity button listeners
        setupQuantityControls();

        // Setup wishlist button listener
        setupWishlistButton();

        // Setup image adapter
        imageAdapter = new ProductImageAdapter();
        viewPagerImages.setAdapter(imageAdapter);

        // Setup page change listener for indicators and counter
        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicators(position);
                updateImageCounter(position);
            }
        });

        // Setup image click listener
        imageAdapter.setOnImageClickListener((position, image) -> {
            showFullscreenImages(position);
        });
    }

    private void setupQuantityControls() {
        btnDecrease.setOnClickListener(v -> {
            if (currentQuantity > 1) {
                currentQuantity--;
                updateQuantityDisplay();
                updateTotalPrice();
            }
        });

        btnIncrease.setOnClickListener(v -> {
            currentQuantity++;
            updateQuantityDisplay();
            updateTotalPrice();
        });
    }

    private void setupWishlistButton() {
        btnAddToWishlist.setOnClickListener(v -> {
            if (!sessionManager.isLoggedIn()) {
                Toast.makeText(this, "Vui lòng đăng nhập để thêm vào wishlist", Toast.LENGTH_SHORT).show();
                return;
            }
            addToWishlist();
        });
    }

    private void addToWishlist() {
        int userId = sessionManager.getUserId();
        if (userId == -1 || currentProductId == -1) {
            Toast.makeText(this, "Lỗi: Không thể thêm vào wishlist", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        // Create request with product ID array
        int[] productIds = new int[] { currentProductId };
        AddToWishlistRequest request = new AddToWishlistRequest(userId, productIds);

        apiService.addToWishlist(request).enqueue(new Callback<List<com.example.kibo.models.WishlistResponse>>() {
            @Override
            public void onResponse(Call<List<com.example.kibo.models.WishlistResponse>> call,
                    Response<List<com.example.kibo.models.WishlistResponse>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Toast.makeText(ProductDetailActivity.this,
                            "Đã thêm vào wishlist!", Toast.LENGTH_SHORT).show();
                    // Update icon to filled heart
                    btnAddToWishlist.setImageResource(R.drawable.ic_heart_filled);
                } else {
                    Toast.makeText(ProductDetailActivity.this,
                            "Không thể thêm vào wishlist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<com.example.kibo.models.WishlistResponse>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Failed to add to wishlist", t);
                Toast.makeText(ProductDetailActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateQuantityDisplay() {
        tvQuantity.setText(String.valueOf(currentQuantity));
    }

    private void updateTotalPrice() {
        if (unitPrice > 0) {
            double total = unitPrice * currentQuantity;
            String formattedPrice = String.format("%,.0fđ", total);
            tvTotalPrice.setText(formattedPrice);
        }
    }

    private void loadProductDetail(int productId) {
        Log.d(TAG, "Loading product detail for ID: " + productId);
        showLoading(true);

        // Try new endpoint first
        ApiClient.getApiService().getProductDetail(productId).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    Product product = response.body();
                    displayProductDetail(product);
                    loadProductImages(productId);
                } else {
                    // Fallback to old endpoint
                    loadProductDetailFallback(productId);
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                Log.e(TAG, "Failed to load product detail with new endpoint", t);
                // Fallback to old endpoint
                loadProductDetailFallback(productId);
            }
        });
    }

    private void loadProductDetailFallback(int productId) {
        Log.d(TAG, "Using fallback endpoint for product ID: " + productId);
        showLoading(true);

        ApiClient.getApiService().getProductById(productId).enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ProductResponse productResponse = response.body();

                    if (productResponse.getData() != null && !productResponse.getData().isEmpty()) {
                        Product product = productResponse.getData().get(0);
                        displayProductDetail(product);
                        loadProductImages(productId);
                    } else {
                        Toast.makeText(ProductDetailActivity.this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT)
                                .show();
                        finish();
                    }
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Failed to load product detail", t);
                Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadProductImages(int productId) {
        apiService.getProductImages(productId).enqueue(new Callback<List<ProductImage>>() {
            @Override
            public void onResponse(Call<List<ProductImage>> call, Response<List<ProductImage>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    productImages.clear();
                    productImages.addAll(response.body());
                    imageAdapter.setImages(productImages);
                    setupIndicators();
                    updateImageCounter(0);
                } else {
                    Log.w(TAG, "No product images found");
                }
            }

            @Override
            public void onFailure(Call<List<ProductImage>> call, Throwable t) {
                Log.e(TAG, "Failed to load product images", t);
            }
        });
    }

    private void setupIndicators() {
        layoutIndicators.removeAllViews();

        if (productImages.size() <= 1) {
            layoutIndicators.setVisibility(View.GONE);
            return;
        }

        layoutIndicators.setVisibility(View.VISIBLE);

        for (int i = 0; i < productImages.size(); i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) (8 * getResources().getDisplayMetrics().density),
                    (int) (8 * getResources().getDisplayMetrics().density));
            params.setMargins(4, 0, 4, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == 0 ? R.drawable.indicator_dot_active : R.drawable.indicator_dot_inactive);
            layoutIndicators.addView(dot);
        }
    }

    private void updateIndicators(int position) {
        for (int i = 0; i < layoutIndicators.getChildCount(); i++) {
            View dot = layoutIndicators.getChildAt(i);
            dot.setBackgroundResource(
                    i == position ? R.drawable.indicator_dot_active : R.drawable.indicator_dot_inactive);
        }
    }

    private void updateImageCounter(int position) {
        if (productImages.isEmpty()) {
            tvImageCounter.setVisibility(View.GONE);
        } else {
            tvImageCounter.setVisibility(View.VISIBLE);
            tvImageCounter.setText((position + 1) + "/" + productImages.size());
        }
    }

    private void showFullscreenImages(int position) {
        FullscreenImageDialog dialog = FullscreenImageDialog.newInstance(
                new ArrayList<>(productImages),
                position);
        dialog.show(getSupportFragmentManager(), "fullscreen_image");
    }

    private void displayProductDetail(Product product) {
        tvProductName.setText(product.getProductName());
        tvPrice.setText(product.getFormattedPrice());
        tvBriefDescription.setText(product.getBriefDescription());
        tvFullDescription.setText(product.getFullDescription());

        // Store unit price for quantity calculations
        unitPrice = product.getPrice();

        // Initialize quantity and total price
        currentQuantity = 1;
        updateQuantityDisplay();
        updateTotalPrice();

        // Display category name
        tvCategoryName.setText(product.getCategoryName() != null ? product.getCategoryName() : "N/A");

        // Display specifications
        tvConnection.setText(product.getConnection() != null ? product.getConnection() : "N/A");
        tvLayout.setText(product.getLayout() != null ? product.getLayout() : "N/A");
        tvKeycap.setText(product.getKeycap() != null ? product.getKeycap() : "N/A");
        tvSwitch.setText(product.getSwitchDetailType() != null ? product.getSwitchDetailType() : "N/A");
        tvBattery.setText(product.getBattery() != null ? product.getBattery() : "Không");
        tvOs.setText(product.getOs() != null ? product.getOs() : "N/A");
        tvLed.setText(product.getLed() != null ? product.getLed() : "Không");
        tvScreen.setText(product.getScreen() != null ? product.getScreen() : "Không");

        // Display dimensions in format: length x width x height
        String dimensions = formatDimensions(product.getLength(), product.getWidth(), product.getHeight());
        tvDimensions.setText(dimensions);
    }

    private String formatDimensions(int length, int width, int height) {
        if (length > 0 && width > 0 && height > 0) {
            return String.format("%d x %d x %d cm", length, width, height);
        } else if (length > 0 && width > 0) {
            return String.format("%d x %d cm", length, width);
        } else {
            return "N/A";
        }
    }

    private void showLoading(boolean show) {
        loadingLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void buyNow() {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để mua hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button and show loading
        btnBuy.setEnabled(false);
        btnBuy.setText("Đang xử lý...");
        showLoading(true);

        // Get user ID
        int userId = sessionManager.getUserId();

        // Step 1: Check if user already has an active cart (status=1)
        // If yes, use it. If no, create a new one.
        ensureActiveCartAndAddProduct(userId);
    }

    private void ensureActiveCartAndAddProduct(int userId) {
        // Check for existing active cart first
        apiService.getCarts(userId, 100).enqueue(new Callback<com.example.kibo.models.PaginationResponse<Cart>>() {
            @Override
            public void onResponse(Call<com.example.kibo.models.PaginationResponse<Cart>> call,
                    Response<com.example.kibo.models.PaginationResponse<Cart>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    java.util.List<Cart> carts = response.body().getData();
                    Log.d(TAG, "Checking for active cart. Found " + carts.size() + " carts");

                    Cart activeCart = null;
                    for (Cart c : carts) {
                        if (c.getStatus() == 1) { // status=1 means active cart
                            activeCart = c;
                            break;
                        }
                    }

                    if (activeCart != null) {
                        // Use existing active cart
                        int cartId = activeCart.getCartId();
                        Log.d(TAG, "Using existing active cart ID: " + cartId);
                        sessionManager.setActiveCartId(cartId);
                        addProductToCart(cartId);
                    } else {
                        // No active cart found - create a new one
                        Log.d(TAG, "No active cart found. Creating new cart.");
                        createCartAndAddProduct(userId);
                    }
                } else {
                    // API call failed - try to create new cart
                    Log.w(TAG, "getCarts failed, creating new cart");
                    createCartAndAddProduct(userId);
                }
            }

            @Override
            public void onFailure(Call<com.example.kibo.models.PaginationResponse<Cart>> call, Throwable t) {
                Log.e(TAG, "Error checking carts: " + t.getMessage());
                // On error, try to create new cart
                createCartAndAddProduct(userId);
            }
        });
    }

    private void createCartAndAddProduct(int userId) {
        // Create new cart with status=1 (active)
        CartRequest cartRequest = new CartRequest(userId, 1); // status = 1 (active/checked out)
        Call<Cart> createCartCall = apiService.createCart(cartRequest);
        createCartCall.enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Cart cart = response.body();
                    int cartId = cart.getCartId();

                    Log.d(TAG, "Cart created successfully with ID: " + cartId);

                    // Save cart ID to SessionManager
                    sessionManager.setActiveCartId(cartId);

                    // Step 2: Add product to cart
                    addProductToCart(cartId);
                } else {
                    showLoading(false);
                    btnBuy.setEnabled(true);
                    btnBuy.setText("MUA NGAY");
                    Toast.makeText(ProductDetailActivity.this, "Không thể tạo giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                showLoading(false);
                btnBuy.setEnabled(true);
                btnBuy.setText("MUA NGAY");
                Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addProductToCart(int cartId) {
        CartItemRequest cartItemRequest = new CartItemRequest(cartId, currentProductId, currentQuantity);
        Call<ApiResponse<String>> addToCartCall = apiService.addToCart(cartItemRequest);
        addToCartCall.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                showLoading(false);
                btnBuy.setEnabled(true);
                btnBuy.setText("MUA NGAY");

                if (response.isSuccessful()) {
                    Toast.makeText(ProductDetailActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    // Stay on current page, don't navigate to cart
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Không thể thêm vào giỏ hàng", Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                showLoading(false);
                btnBuy.setEnabled(true);
                btnBuy.setText("MUA NGAY");
                Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
