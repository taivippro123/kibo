package com.example.kibo.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kibo.R;
import com.example.kibo.adapters.ProductPopupAdapter;
import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.Category;
import com.example.kibo.models.Product;
import com.example.kibo.models.ProductResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryProductsDialog extends DialogFragment {

    private static final String ARG_CATEGORY = "category";
    
    private Category category;
    private ApiService apiService;
    private ProductPopupAdapter adapter;
    private List<Product> products;
    
    // Views
    private TextView tvCategoryName;
    private RecyclerView recyclerProducts;
    private ProgressBar progressLoading;
    private LinearLayout layoutEmpty;
    private ImageButton btnClose;
    
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public static CategoryProductsDialog newInstance(Category category) {
        CategoryProductsDialog dialog = new CategoryProductsDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CATEGORY, category);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set style để dialog rộng tối đa
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar);
        if (getArguments() != null) {
            category = (Category) getArguments().getSerializable(ARG_CATEGORY);
        }
        apiService = ApiClient.getApiService();
        products = new ArrayList<>();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_category_products, null);
        initViews(view);
        setupRecyclerView();
        loadProducts();
        
        builder.setView(view);
        
        android.app.AlertDialog dialog = builder.create();
        
        // Xóa background mặc định để không có viền vuông
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        // Đặt dialog rộng tối đa
        dialog.getWindow().setLayout(android.view.WindowManager.LayoutParams.MATCH_PARENT, 
                                   android.view.WindowManager.LayoutParams.WRAP_CONTENT);
        
        return dialog;
    }


    private void initViews(View view) {
        tvCategoryName = view.findViewById(R.id.tv_category_name);
        recyclerProducts = view.findViewById(R.id.recycler_products);
        progressLoading = view.findViewById(R.id.progress_loading);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        btnClose = view.findViewById(R.id.btn_close);

        // Set category name
        if (category != null) {
            tvCategoryName.setText("Sản phẩm - " + category.getCategoryName());
        }

        // Close button
        btnClose.setOnClickListener(v -> dismiss());
    }

    private void setupRecyclerView() {
        adapter = new ProductPopupAdapter(getContext(), products);
        adapter.setOnProductClickListener(product -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
            dismiss(); // Close dialog when product is clicked
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerProducts.setLayoutManager(layoutManager);
        recyclerProducts.setAdapter(adapter);
        
        // Thêm spacing giữa các items
        recyclerProducts.addItemDecoration(new androidx.recyclerview.widget.DividerItemDecoration(getContext(), layoutManager.getOrientation()) {
            @Override
            public void getItemOffsets(android.graphics.Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.bottom = 12; // 12dp spacing giữa các items
            }
        });
    }

    private void loadProducts() {
        if (category == null) return;

        showLoading(true);
        
        // Lấy tất cả sản phẩm của danh mục này
        Call<ProductResponse> call = apiService.getProductsByCategory(
            category.getCategoryId(), 
            1, 
            100 // Lấy tối đa 100 sản phẩm
        );

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> productList = response.body().getData();
                    if (productList != null && !productList.isEmpty()) {
                        products.clear();
                        products.addAll(productList);
                        adapter.notifyDataSetChanged();
                        showEmptyState(false);
                    } else {
                        showEmptyState(true);
                    }
                } else {
                    showEmptyState(true);
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                showLoading(false);
                showEmptyState(true);
            }
        });
    }

    private void showLoading(boolean show) {
        progressLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerProducts.setVisibility(show ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
    }

    private void showEmptyState(boolean show) {
        layoutEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerProducts.setVisibility(show ? View.GONE : View.VISIBLE);
        progressLoading.setVisibility(View.GONE);
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }
}
