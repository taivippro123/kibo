package com.example.kibo.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.kibo.R;
import com.example.kibo.adapters.AdminCategoryAdapter;
import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.dialogs.CategoryFormDialog;
import com.example.kibo.dialogs.CategoryProductsDialog;
import com.example.kibo.models.ApiResponse;
import com.example.kibo.models.Category;
import com.example.kibo.models.CategoryResponse;
import com.example.kibo.models.ProductResponse;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCategoryFragment extends Fragment {
    
    private RecyclerView rvCategories;
    private EditText etSearch;
    private ImageButton btnAdd;
    private AdminCategoryAdapter adapter;
    private List<Category> categoryList;
    private SwipeRefreshLayout swipeRefreshLayout;
    
    // Counter để theo dõi số lượng product count requests đã hoàn thành
    private int completedProductCountRequests = 0;
    private int totalProductCountRequests = 0;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_categories, container, false);
        
        setupViews(view);
        setupData();
        setupListeners();
        
        return view;
    }
    
    private void setupViews(View view) {
        rvCategories = view.findViewById(R.id.rv_admin_categories);
        etSearch = view.findViewById(R.id.et_search_categories);
        btnAdd = view.findViewById(R.id.btn_add_category);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshCategories);
        
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
    }
    
    private void setupData() {
        categoryList = new ArrayList<>();
        
        adapter = new AdminCategoryAdapter(
            categoryList,
            this::onEditClick,
            this::onDeleteClick
        );
        
        // Thêm click listener cho tên danh mục
        adapter.setOnCategoryClickListener(this::onCategoryClick);
        rvCategories.setAdapter(adapter);
        
        loadCategories();
    }
    
    private void setupListeners() {
        btnAdd.setOnClickListener(v -> openCategoryForm());
        
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCategories(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Setup pull-to-refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Reload categories
            loadCategories();
        });
    }
    
    private void openCategoryForm() {
        CategoryFormDialog dialog = CategoryFormDialog.newInstance();
        dialog.setOnCategorySavedListener(this::refreshData);
        dialog.show(getChildFragmentManager(), "CategoryFormDialog");
    }
    
    private void onEditClick(Category category) {
        CategoryFormDialog dialog = CategoryFormDialog.newInstance(category);
        dialog.setOnCategorySavedListener(this::refreshData);
        dialog.show(getChildFragmentManager(), "CategoryFormDialog");
    }
    
    private void onDeleteClick(Category category) {
        showDeleteConfirmDialog(category);
    }
    
    private void onCategoryClick(Category category) {
        // Mở dialog hiển thị sản phẩm của danh mục
        CategoryProductsDialog dialog = CategoryProductsDialog.newInstance(category);
        dialog.setOnProductClickListener(product -> {
            // Nhảy qua tab "Sản phẩm" và hiện tên sản phẩm trong ô tìm kiếm
            if (getActivity() instanceof com.example.kibo.AdminManagementActivity) {
                com.example.kibo.AdminManagementActivity adminActivity = (com.example.kibo.AdminManagementActivity) getActivity();
                adminActivity.navigateToProductsWithSearch(product.getProductName());
            }
        });
        dialog.show(getChildFragmentManager(), "CategoryProductsDialog");
    }
    
    private void showDeleteConfirmDialog(Category category) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Xóa danh mục")
                .setMessage("Bạn có chắc muốn xóa danh mục '" + category.getCategoryName() + "'?\n\nHành động này không thể hoàn tác!")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> deleteCategory(category))
                .show();
    }
    
    private void deleteCategory(Category category) {
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(getContext());
        progressDialog.setMessage("Đang xóa danh mục...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        ApiService apiService = ApiClient.getApiService();
        apiService.deleteCategory(category.getCategoryId()).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Xóa danh mục thành công", Toast.LENGTH_SHORT).show();
                    refreshData(); // Refresh danh sách
                } else {
                    Toast.makeText(getContext(), "Xóa danh mục thất bại", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Lỗi mạng khi xóa danh mục: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void filterCategories(String query) {
        List<Category> filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            filteredList.addAll(categoryList);
        } else {
            for (Category category : categoryList) {
                if (category.getCategoryName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(category);
                }
            }
        }
        adapter.filterList(filteredList);
    }
    
    private void loadCategories() {
        ApiService apiService = ApiClient.getApiService();
        apiService.getCategories().enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(Call<CategoryResponse> call, Response<CategoryResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    categoryList.clear();
                    categoryList.addAll(response.body().getData());
                    adapter.updateList(new ArrayList<>(categoryList));
                    
                    // Reset counter cho product counts
                    completedProductCountRequests = 0;
                    totalProductCountRequests = categoryList.size();
                    
                    // Load product count cho mỗi category
                    if (totalProductCountRequests > 0) {
                        loadProductCounts();
                    } else {
                        // Nếu không có category nào, dừng refresh ngay
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    
                    Toast.makeText(getContext(), "Đã tải " + categoryList.size() + " danh mục", Toast.LENGTH_SHORT).show();
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "Tải danh mục thất bại", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "Lỗi mạng khi tải danh mục", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadProductCounts() {
        ApiService apiService = ApiClient.getApiService();
        
        for (Category category : categoryList) {
            // Gọi API getProductsByCategory với categoryId để lấy totalItems
            // Sử dụng page=1, pageSize=1 để chỉ lấy metadata (totalItems)
            apiService.getProductsByCategory(category.getCategoryId(), 1, 1).enqueue(new Callback<ProductResponse>() {
                @Override
                public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Sử dụng totalItems thay vì đếm array size
                        int productCount = response.body().getTotalItems();
                        // Cập nhật product count trong adapter
                        adapter.updateProductCount(category.getCategoryId(), productCount);
                    } else {
                        // Nếu không có sản phẩm hoặc lỗi, set count = 0
                        adapter.updateProductCount(category.getCategoryId(), 0);
                    }
                    
                    // Kiểm tra xem đã load xong tất cả product counts chưa
                    checkAllProductCountsLoaded();
                }
                
                @Override
                public void onFailure(Call<ProductResponse> call, Throwable t) {
                    // Nếu lỗi mạng, set count = 0
                    adapter.updateProductCount(category.getCategoryId(), 0);
                    
                    // Kiểm tra xem đã load xong tất cả product counts chưa
                    checkAllProductCountsLoaded();
                }
            });
        }
    }
    
    // Method để kiểm tra xem đã load xong tất cả product counts chưa
    private void checkAllProductCountsLoaded() {
        completedProductCountRequests++;
        if (completedProductCountRequests >= totalProductCountRequests) {
            // Dừng refresh animation khi đã load xong tất cả product counts
            swipeRefreshLayout.setRefreshing(false);
            completedProductCountRequests = 0; // Reset cho lần refresh tiếp theo
            totalProductCountRequests = 0;
        }
    }
    
    public void refreshData() {
        loadCategories();
    }
}
