package com.example.kibo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kibo.adapter.WishlistAdapter;
import com.example.kibo.models.Product;
import com.example.kibo.models.WishlistResponse;
import com.example.kibo.models.AddToWishlistRequest;
import com.example.kibo.models.ApiResponse;
import com.example.kibo.models.Category;
import com.example.kibo.models.CategoryResponse;
import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WishlistActivity extends AppCompatActivity implements WishlistAdapter.OnWishlistItemClickListener {

    private static final String TAG = "WishlistActivity";

    private RecyclerView recyclerWishlist;
    private WishlistAdapter wishlistAdapter;
    private List<Product> wishlistItems;
    private List<Product> filteredWishlistItems;

    // UI components
    private EditText edtSearch;
    private LinearLayout tabsContainer;
    private ImageView btnFilter, btnPrice;

    private String currentCategory = "ALL"; // Changed to show all products by default
    private List<TextView> categoryTabs = new ArrayList<>();

    // API and Session
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        // Initialize API and Session
        sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiServiceWithAuth(this);

        initViews();
        setupRecyclerView();
        setupFilterButtons();
        loadCategories(); // Load categories from API first
        loadWishlistData();
    }

    private void initViews() {
        recyclerWishlist = findViewById(R.id.recycler_wishlist);
        edtSearch = findViewById(R.id.edt_search);
        tabsContainer = findViewById(R.id.tabs_container);
        btnFilter = findViewById(R.id.btn_filter);
        btnPrice = findViewById(R.id.btn_price);

        // Setup search functionality
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupRecyclerView() {
        wishlistItems = new ArrayList<>();
        filteredWishlistItems = new ArrayList<>();

        wishlistAdapter = new WishlistAdapter(this, filteredWishlistItems);
        wishlistAdapter.setOnWishlistItemClickListener(this);

        recyclerWishlist.setLayoutManager(new LinearLayoutManager(this));
        recyclerWishlist.setAdapter(wishlistAdapter);
    }

    private void loadCategories() {
        // Call API to get categories
        Call<CategoryResponse> call = apiService.getCategories();
        call.enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(Call<CategoryResponse> call, Response<CategoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CategoryResponse categoryResponse = response.body();
                    List<Category> categories = categoryResponse.getData();

                    Log.d(TAG, "Categories loaded: " + categories.size() + " items");

                    // Setup tabs with loaded categories
                    setupCategoryTabs(categories);
                } else {
                    Log.e(TAG, "Failed to load categories: " + response.code());
                    // Setup with default "All" tab only
                    setupCategoryTabs(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                Log.e(TAG, "Error loading categories", t);
                // Setup with default "All" tab only
                setupCategoryTabs(new ArrayList<>());
            }
        });
    }

    private void setupCategoryTabs(List<Category> categories) {
        tabsContainer.removeAllViews();
        categoryTabs.clear();

        // Add "Tất cả" tab first
        TextView tabAll = createTabView("Tất cả", "ALL", true);
        tabsContainer.addView(tabAll);
        categoryTabs.add(tabAll);

        // Add tabs for each category from API
        for (Category category : categories) {
            TextView tabView = createTabView(category.getCategoryName(), category.getCategoryName(), false);
            tabsContainer.addView(tabView);
            categoryTabs.add(tabView);
        }
    }

    private TextView createTabView(String displayText, final String categoryName, boolean isSelected) {
        TextView tabView = new TextView(this);
        tabView.setText(displayText);
        tabView.setTextSize(14);
        tabView.setPadding(
                (int) (8 * getResources().getDisplayMetrics().density),
                (int) (8 * getResources().getDisplayMetrics().density),
                (int) (8 * getResources().getDisplayMetrics().density),
                (int) (8 * getResources().getDisplayMetrics().density));

        // Set layout params with margin
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins((int) (16 * getResources().getDisplayMetrics().density), 0, 0, 0);
        tabView.setLayoutParams(params);

        // Set initial style
        if (isSelected) {
            tabView.setTextColor(getResources().getColor(R.color.primary_color));
            tabView.setBackground(getResources().getDrawable(R.drawable.bg_tab_selected));
        } else {
            tabView.setTextColor(getResources().getColor(R.color.gray_medium));
        }

        // Set click listener
        tabView.setOnClickListener(v -> selectTab(categoryName, tabView));

        return tabView;
    }

    private void setupTabNavigation() {
        // This method is no longer needed as tabs are created dynamically
    }

    private void setupFilterButtons() {
        btnFilter.setOnClickListener(v -> showFilterDialog());
        btnPrice.setOnClickListener(v -> showPriceFilter());
    }

    private void selectTab(String category, TextView selectedTab) {
        // Reset all tabs
        resetTabStyles();

        // Set selected tab style
        selectedTab.setTextColor(getResources().getColor(R.color.primary_color));
        selectedTab.setBackground(getResources().getDrawable(R.drawable.bg_tab_selected));

        currentCategory = category;
        filterByCategory();
    }

    private void resetTabStyles() {
        int grayColor = getResources().getColor(R.color.gray_medium);

        // Reset all category tabs
        for (TextView tab : categoryTabs) {
            tab.setTextColor(grayColor);
            tab.setBackground(null);
        }
    }

    private void loadWishlistData() {
        int userId = sessionManager.getUserId();

        if (userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem wishlist", Toast.LENGTH_SHORT).show();
            // Navigate to login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Call API to get wishlist
        Call<List<WishlistResponse>> call = apiService.getWishlist(userId);
        call.enqueue(new Callback<List<WishlistResponse>>() {
            @Override
            public void onResponse(Call<List<WishlistResponse>> call, Response<List<WishlistResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<WishlistResponse> wishlistResponses = response.body();
                    Log.d(TAG, "Wishlist loaded: " + wishlistResponses.size() + " items");

                    // Load product details for each wishlist item
                    loadProductDetailsFromWishlist(wishlistResponses);
                } else {
                    Log.e(TAG, "Failed to load wishlist: " + response.code());
                    Toast.makeText(WishlistActivity.this, "Không thể tải danh sách yêu thích", Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<List<WishlistResponse>> call, Throwable t) {
                Log.e(TAG, "Error loading wishlist", t);
                Toast.makeText(WishlistActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProductDetailsFromWishlist(List<WishlistResponse> wishlistResponses) {
        wishlistItems.clear();

        if (wishlistResponses.isEmpty()) {
            Log.d(TAG, "Wishlist is empty");
            filterByCategory();
            Toast.makeText(this, "Danh sách yêu thích trống", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Loading " + wishlistResponses.size() + " products from wishlist");

        // Load product details for each product ID in wishlist
        for (WishlistResponse wishlistItem : wishlistResponses) {
            int productId = wishlistItem.getProductId();

            Log.d(TAG, "Fetching product details for ID: " + productId);

            Call<Product> call = apiService.getProductDetail(productId);
            call.enqueue(new Callback<Product>() {
                @Override
                public void onResponse(Call<Product> call, Response<Product> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Product product = response.body();
                        wishlistItems.add(product);
                        Log.d(TAG, "Product loaded: " + product.getProductName() +
                                " (Category: " + product.getCategoryName() + ")");

                        // Update UI after each product is loaded
                        filterByCategory();
                        Log.d(TAG, "Current wishlist size: " + wishlistItems.size() +
                                ", Filtered size: " + filteredWishlistItems.size());
                    } else {
                        Log.e(TAG, "Failed to load product " + productId + ": " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Product> call, Throwable t) {
                    Log.e(TAG, "Error loading product " + productId, t);
                }
            });
        }
    }

    private void filterByCategory() {
        filteredWishlistItems.clear();

        if ("ALL".equals(currentCategory)) {
            // Show all products
            filteredWishlistItems.addAll(wishlistItems);
        } else {
            // Filter by specific category
            for (Product product : wishlistItems) {
                if (product.getCategoryName() != null && product.getCategoryName().equals(currentCategory)) {
                    filteredWishlistItems.add(product);
                }
            }
        }

        wishlistAdapter.notifyDataSetChanged();
    }

    private void filterProducts(String query) {
        filteredWishlistItems.clear();

        if (query.isEmpty()) {
            filterByCategory();
        } else {
            for (Product product : wishlistItems) {
                boolean matchesCategory = "ALL".equals(currentCategory) ||
                        (product.getCategoryName() != null && product.getCategoryName().equals(currentCategory));

                if (matchesCategory && product.getProductName().toLowerCase().contains(query.toLowerCase())) {
                    filteredWishlistItems.add(product);
                }
            }
        }
        wishlistAdapter.notifyDataSetChanged();
    }

    private void showFilterDialog() {
        Toast.makeText(this, "Filter dialog - Coming soon", Toast.LENGTH_SHORT).show();
    }

    private void showPriceFilter() {
        Toast.makeText(this, "Price filter - Coming soon", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(Product product) {
        // Navigate to product detail
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getProductId());
        startActivity(intent);
    }

    @Override
    public void onRemoveFromWishlist(Product product) {
        int userId = sessionManager.getUserId();
        int productId = product.getProductId();

        Log.d(TAG, "Removing product from wishlist - UserId: " + userId + ", ProductId: " + productId);

        // Call API with query parameters (DELETE method)
        Call<ApiResponse<String>> call = apiService.removeFromWishlist(userId, productId);
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response successful: " + response.isSuccessful());

                // Check for 204 No Content (successful deletion with no body)
                if (response.code() == 204 || response.isSuccessful()) {
                    Toast.makeText(WishlistActivity.this, "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Product removed from wishlist: " + product.getProductName());
                    
                    // Reload wishlist from server
                    loadWishlistData();
                } else {
                    Log.e(TAG, "Failed to remove from wishlist - Code: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(WishlistActivity.this, "Không thể xóa khỏi wishlist (code: " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Log.e(TAG, "Error removing from wishlist", t);
                Toast.makeText(WishlistActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
