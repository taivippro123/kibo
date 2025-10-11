package com.example.kibo.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.kibo.R;
import com.example.kibo.adapters.ProductImagePagerAdapter;
import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.Product;
import com.example.kibo.models.ProductImage;
import com.example.kibo.models.ProductResponse;
import com.example.kibo.models.Category;
import com.example.kibo.models.CategoryResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailDialog extends DialogFragment {

    private static final String ARG_PRODUCT_ID = "product_id";
    private int productId;
    private Product product;
    private List<Category> categoryList;

    // Views
    private ViewPager2 viewPagerImages;
    private LinearLayout layoutDots;
    private ProductImagePagerAdapter imageAdapter;
    private List<String> imageUrls;
    private TextView tvProductName, tvPrice, tvCategory, tvStock;
    private TextView tvBriefDescription, tvFullDescription;
    private TextView tvConnection, tvLayout, tvSwitch, tvKeycap, tvLed, tvDimensions;
    private ImageButton btnClose;

    public static ProductDetailDialog newInstance(int productId) {
        ProductDetailDialog dialog = new ProductDetailDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_PRODUCT_ID, productId);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getInt(ARG_PRODUCT_ID);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_product_detail, null);
        setupViews(view);
        
        builder.setView(view);
        
        // Load categories first, then product data
        loadCategories();
        
        AlertDialog dialog = builder.create();
        
        // Xóa background mặc định để không có viền vuông
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        // Đặt dialog rộng tối đa
        dialog.getWindow().setLayout(android.view.WindowManager.LayoutParams.MATCH_PARENT, 
                                   android.view.WindowManager.LayoutParams.WRAP_CONTENT);
        
        return dialog;
    }

    private void setupViews(View view) {
        viewPagerImages = view.findViewById(R.id.viewpager_images);
        layoutDots = view.findViewById(R.id.layout_dots);
        tvProductName = view.findViewById(R.id.tv_product_name);
        tvPrice = view.findViewById(R.id.tv_price);
        tvCategory = view.findViewById(R.id.tv_category);
        tvStock = view.findViewById(R.id.tv_stock);
        tvBriefDescription = view.findViewById(R.id.tv_brief_description);
        tvFullDescription = view.findViewById(R.id.tv_full_description);
        tvConnection = view.findViewById(R.id.tv_connection);
        tvLayout = view.findViewById(R.id.tv_layout);
        tvSwitch = view.findViewById(R.id.tv_switch);
        tvKeycap = view.findViewById(R.id.tv_keycap);
        tvLed = view.findViewById(R.id.tv_led);
        tvDimensions = view.findViewById(R.id.tv_dimensions);
        btnClose = view.findViewById(R.id.btn_close);

        // Khởi tạo danh sách ảnh
        imageUrls = new ArrayList<>();
        imageAdapter = new ProductImagePagerAdapter(imageUrls);
        viewPagerImages.setAdapter(imageAdapter);

        // Setup ViewPager2 page change listener để cập nhật dots
        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDotsIndicator(position);
            }
        });

        // Tối ưu ViewPager2 để tránh mờ ảnh
        viewPagerImages.setOffscreenPageLimit(1); // Preload 1 ảnh trước/sau
        viewPagerImages.setUserInputEnabled(true); // Cho phép swipe

        btnClose.setOnClickListener(v -> dismiss());
    }

    private void loadCategories() {
        categoryList = new ArrayList<>();
        ApiService apiService = ApiClient.getApiService();
        apiService.getCategories().enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(Call<CategoryResponse> call, Response<CategoryResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    categoryList.clear();
                    categoryList.addAll(response.body().getData());
                }
                // Load product data sau khi có categories (hoặc không có)
                loadProductData();
            }

            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                // Load product data dù categories thất bại
                loadProductData();
            }
        });
    }

    private void loadProductData() {
        ApiService apiService = ApiClient.getApiService();
        
        // Load thông tin sản phẩm
        apiService.getProductById(productId).enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Product> products = response.body().getData();
                    if (!products.isEmpty()) {
                        product = products.get(0);
                        populateViews();
                        
                        // Load ảnh sản phẩm
                        loadProductImages();
                    } else {
                        Toast.makeText(getContext(), "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi khi tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng khi tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
    }

    private void loadProductImages() {
        ApiService apiService = ApiClient.getApiService();
        apiService.getProductImages(productId).enqueue(new Callback<List<ProductImage>>() {
            @Override
            public void onResponse(Call<List<ProductImage>> call, Response<List<ProductImage>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // Load ảnh từ API
                    imageUrls.clear();
                    for (ProductImage productImage : response.body()) {
                        imageUrls.add(productImage.getImageUrl());
                    }
                } else {
                    // Fallback: sử dụng ảnh chính từ Product
                    imageUrls.clear();
                    if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                        imageUrls.add(product.getImageUrl());
                    }
                }
                
                // Cập nhật adapter và dots
                imageAdapter.notifyDataSetChanged();
                setupDotsIndicator();
                
                // Ẩn ViewPager nếu chỉ có 1 ảnh hoặc không có ảnh
                if (imageUrls.size() <= 1) {
                    layoutDots.setVisibility(View.GONE);
                } else {
                    layoutDots.setVisibility(View.VISIBLE);
                }
                
                // Force refresh ViewPager2 để đảm bảo ảnh load đúng
                viewPagerImages.post(() -> {
                    if (imageAdapter.getItemCount() > 0) {
                        imageAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onFailure(Call<List<ProductImage>> call, Throwable t) {
                // Fallback: sử dụng ảnh chính từ Product
                imageUrls.clear();
                if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                    imageUrls.add(product.getImageUrl());
                }
                
                imageAdapter.notifyDataSetChanged();
                setupDotsIndicator();
                layoutDots.setVisibility(View.GONE);
            }
        });
    }

    private void populateViews() {
        if (product == null) return;

        // Thông tin cơ bản
        tvProductName.setText(product.getProductName());
        tvPrice.setText(product.getFormattedPrice());
        tvCategory.setText(getCategoryName(product.getCategoryId()));
        
        // Hiển thị tồn kho với màu sắc
        int stock = product.getQuantity();
        tvStock.setText(String.valueOf(stock));
        
        // Thêm background màu cho tồn kho
        if (stock <= 0) {
            // Đỏ: Hết hàng
            tvStock.setBackgroundResource(R.drawable.stock_red_background);
            tvStock.setTextColor(getResources().getColor(android.R.color.white));
        } else if (stock < 10) {
            // Cam: Sắp hết hàng (1-9)
            tvStock.setBackgroundResource(R.drawable.stock_yellow_background);
            tvStock.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            // Xanh: Còn nhiều hàng (>=10)
            tvStock.setBackgroundResource(R.drawable.stock_green_background);
            tvStock.setTextColor(getResources().getColor(android.R.color.white));
        }

        // Mô tả
        tvBriefDescription.setText(product.getBriefDescription() != null ? product.getBriefDescription() : "Chưa có mô tả");
        tvFullDescription.setText(product.getFullDescription() != null ? product.getFullDescription() : "Chưa có mô tả đầy đủ");

        // Thông tin kỹ thuật
        tvConnection.setText(product.getConnection() != null ? product.getConnection() : "N/A");
        tvLayout.setText(product.getLayout() != null ? product.getLayout() : "N/A");
        tvSwitch.setText(product.getSwitchType() != null ? product.getSwitchType() : "N/A");
        tvKeycap.setText(product.getKeycap() != null ? product.getKeycap() : "N/A");
        tvLed.setText(product.getLed() != null ? product.getLed() : "N/A");
        
        // Kích thước
        String dimensions = "N/A";
        if (product.getWidth() > 0 && product.getLength() > 0 && product.getHeight() > 0) {
            dimensions = String.format("%d × %d × %d mm", 
                product.getWidth(), product.getLength(), product.getHeight());
        }
        tvDimensions.setText(dimensions);
    }

    private void setupDotsIndicator() {
        layoutDots.removeAllViews();
        
        if (imageUrls.size() <= 1) {
            layoutDots.setVisibility(View.GONE);
            return;
        }
        
        layoutDots.setVisibility(View.VISIBLE);
        
        for (int i = 0; i < imageUrls.size(); i++) {
            View dot = new View(getContext());
            dot.setBackgroundResource(R.drawable.dot_indicator);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (int) (8 * getResources().getDisplayMetrics().density),
                (int) (8 * getResources().getDisplayMetrics().density)
            );
            params.setMargins(4, 0, 4, 0);
            dot.setLayoutParams(params);
            
            layoutDots.addView(dot);
        }
        
        // Highlight dot đầu tiên
        updateDotsIndicator(0);
    }

    private void updateDotsIndicator(int selectedPosition) {
        for (int i = 0; i < layoutDots.getChildCount(); i++) {
            View dot = layoutDots.getChildAt(i);
            if (i == selectedPosition) {
                dot.setAlpha(1.0f); // Active dot
            } else {
                dot.setAlpha(0.3f); // Inactive dot
            }
        }
    }

    private String getCategoryName(int categoryId) {
        if (categoryList != null) {
            for (Category category : categoryList) {
                if (category.getCategoryId() == categoryId) {
                    return category.getCategoryName();
                }
            }
        }
        return "Chưa phân loại";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear Glide cache khi dialog đóng để tránh memory leak
        if (getContext() != null) {
            Glide.get(getContext()).clearMemory();
        }
    }
}
