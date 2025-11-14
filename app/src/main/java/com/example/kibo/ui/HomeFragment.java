package com.example.kibo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.kibo.R;
import com.example.kibo.WishlistActivity;
import com.example.kibo.NotificationActivity;
import com.example.kibo.ProductDetailActivity;
import com.example.kibo.StoreMapActivity;
import com.example.kibo.api.ApiClient;
import com.example.kibo.models.Product;
import com.example.kibo.models.ProductResponse;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.kibo.ui.FilterBottomSheet;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private static final long AUTO_SCROLL_DELAY = 3000; // 3 seconds

    private ProductAdapter productAdapter;
    private RecyclerView productsRecycler;
    private ViewPager2 bannerViewPager;
    private Handler autoScrollHandler;
    private Runnable autoScrollRunnable;
    private EditText etSearch;
    private List<Product> allProducts;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private boolean isLoading = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Setup banner carousel
        bannerViewPager = root.findViewById(R.id.banner_viewpager);
        setupBannerCarousel();

        productsRecycler = root.findViewById(R.id.rv_products);
        productsRecycler.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        // Initialize adapter with empty list
        productAdapter = new ProductAdapter(new ArrayList<>());
        productsRecycler.setAdapter(productAdapter);

        // Setup search functionality
        etSearch = root.findViewById(R.id.et_search);
        setupSearch();

        // Wishlist button click
        ImageButton btnWishlist = root.findViewById(R.id.btn_wishlist);
        btnWishlist.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), WishlistActivity.class);
            startActivity(intent);
        });

        // Store Map button click (NEW - Google Maps)
        ImageButton btnStoreMap = root.findViewById(R.id.btn_store_map);
        if (btnStoreMap != null) {
            btnStoreMap.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), StoreMapActivity.class);
                startActivity(intent);
            });
        }

        ImageButton btnFilter = root.findViewById(R.id.btn_filter);
        btnFilter.setOnClickListener(v -> {
            FilterBottomSheet sheet = new FilterBottomSheet();
            sheet.setOnFilterAppliedListener(filtered -> productAdapter.updateProducts(filtered));
            sheet.show(getParentFragmentManager(), "filter");
        });


        // Load products
        allProducts = new ArrayList<>();
        loadProducts();

        return root;
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Debounce search to avoid too many filter operations
                searchHandler.removeCallbacksAndMessages(null);
                searchHandler.postDelayed(() -> filterProducts(s.toString()), 300);
            }
        });
    }

    private void filterProducts(String query) {
        if (allProducts == null || allProducts.isEmpty()) {
            return;
        }

        if (query == null || query.trim().isEmpty()) {
            // Show all products if search is empty
            productAdapter.updateProducts(allProducts);
            return;
        }

        // Filter products by name
        List<Product> filteredProducts = new ArrayList<>();
        String searchQuery = query.toLowerCase().trim();

        for (Product product : allProducts) {
            String productName = product.getProductName().toLowerCase();
            if (productName.contains(searchQuery)) {
                filteredProducts.add(product);
            }
        }

        productAdapter.updateProducts(filteredProducts);
        Log.d(TAG, "Filtered products: " + filteredProducts.size() + " matching '" + query + "'");
    }

    private void setupBannerCarousel() {
        // Banner images
        int[] bannerImages = {
                R.drawable.win60,
                R.drawable.f87,
                R.drawable.f65
        };

        BannerAdapter bannerAdapter = new BannerAdapter(bannerImages);
        bannerViewPager.setAdapter(bannerAdapter);

        // Setup auto-scroll
        autoScrollHandler = new Handler(Looper.getMainLooper());
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = bannerViewPager.getCurrentItem();
                int nextItem = (currentItem + 1) % bannerImages.length;
                bannerViewPager.setCurrentItem(nextItem, true);
                autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY);
            }
        };
        autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop auto-scroll when fragment is destroyed
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    private void loadProducts() {
        if (isLoading) return;
        
        isLoading = true;
        Log.d(TAG, "Loading products - PageSize: 30");
        
        ApiClient.getApiService().getProducts(1, 30).enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                isLoading = false;
                
                if (response.isSuccessful() && response.body() != null) {
                    ProductResponse productResponse = response.body();
                    List<Product> products = productResponse.getData();

                    if (products != null && !products.isEmpty()) {
                        // Store all products
                        allProducts = products;
                        productAdapter.updateProducts(allProducts);
                        
                        Log.d(TAG, "Loaded " + products.size() + " products");
                    } else {
                        allProducts = new ArrayList<>();
                        productAdapter.updateProducts(allProducts);
                        Toast.makeText(requireContext(), "Không có sản phẩm nào", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Response not successful: " + response.code());
                    Toast.makeText(requireContext(), "Không thể tải sản phẩm (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                isLoading = false;
                Log.e(TAG, "Failed to load products", t);
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private static class ProductAdapter extends RecyclerView.Adapter<ProductViewHolder> {
        private List<Product> products;

        public ProductAdapter(List<Product> products) {
            this.products = products;
        }

        public void updateProducts(List<Product> newProducts) {
            this.products = newProducts;
            notifyDataSetChanged();
        }

        @Override
        public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
            return new ProductViewHolder(item);
        }

        @Override
        public void onBindViewHolder(ProductViewHolder holder, int position) {
            Product product = products.get(position);
            holder.name.setText(product.getProductName());
            holder.price.setText(product.getFormattedPrice());

            // Load image with Glide
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.kibo_logo)
                        .error(R.drawable.kibo_logo)
                        .centerCrop()
                        .into(holder.image);
            } else {
                holder.image.setImageResource(R.drawable.kibo_logo);
            }

            // Click to view product detail
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), ProductDetailActivity.class);
                intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getProductId());
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return products.size();
        }
    }

    private static class ProductViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView name;
        final TextView price;

        ProductViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.img_product);
            name = itemView.findViewById(R.id.tv_name);
            price = itemView.findViewById(R.id.tv_price);
        }
    }

    // Banner adapter
    private static class BannerAdapter extends RecyclerView.Adapter<BannerViewHolder> {
        private final int[] bannerImages;

        public BannerAdapter(int[] bannerImages) {
            this.bannerImages = bannerImages;
        }

        @NonNull
        @Override
        public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
            return new BannerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
            holder.image.setImageResource(bannerImages[position]);
        }

        @Override
        public int getItemCount() {
            return bannerImages.length;
        }
    }

    private static class BannerViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;

        BannerViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.img_banner);
        }
    }

}
