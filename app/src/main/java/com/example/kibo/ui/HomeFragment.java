package com.example.kibo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.kibo.api.ApiClient;
import com.example.kibo.models.Product;
import com.example.kibo.models.ProductResponse;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private static final long AUTO_SCROLL_DELAY = 3000; // 3 seconds
    
    private ProductAdapter productAdapter;
    private RecyclerView productsRecycler;
    private ViewPager2 bannerViewPager;
    private Handler autoScrollHandler;
    private Runnable autoScrollRunnable;

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

        // Wishlist button click
        ImageButton btnWishlist = root.findViewById(R.id.btn_wishlist);
        btnWishlist.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), WishlistActivity.class);
            startActivity(intent);
        });

        // Notification button click
        ImageButton btnNotification = root.findViewById(R.id.btn_notification);
        btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), NotificationActivity.class);
            startActivity(intent);
        });

        // Load products from API
        loadProducts();

        return root;
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
        Log.d(TAG, "Starting to load products...");
        // Try without pagination first
        ApiClient.getApiService().getAllProducts().enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                Log.d(TAG, "Response received. Code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    ProductResponse productResponse = response.body();
                    Log.d(TAG, "Response body is not null");
                    
                    List<Product> products = productResponse.getData();
                    
                    if (products != null) {
                        Log.d(TAG, "Products list size: " + products.size());
                        
                        if (!products.isEmpty()) {
                            productAdapter.updateProducts(products);
                            Log.d(TAG, "Successfully loaded " + products.size() + " products");
                            
                            // Log first product details
                            Product firstProduct = products.get(0);
                            Log.d(TAG, "First product: " + firstProduct.getProductName() + " - " + firstProduct.getFormattedPrice());
                        } else {
                            Log.w(TAG, "Products list is empty");
                            Toast.makeText(requireContext(), "Không có sản phẩm nào", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Products list is null");
                        Toast.makeText(requireContext(), "Lỗi: Danh sách sản phẩm null", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Response not successful: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e(TAG, "Error body: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Cannot read error body", e);
                    }
                    Toast.makeText(requireContext(), "Không thể tải sản phẩm (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Log.e(TAG, "Failed to load products", t);
                Log.e(TAG, "Error message: " + t.getMessage());
                Log.e(TAG, "Request URL: " + call.request().url());
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
