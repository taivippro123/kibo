package com.example.kibo.ui;

import android.content.Intent;
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
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.kibo.R;
import com.example.kibo.AdminProductFormActivity;
import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.ProductResponse;
import com.example.kibo.models.CategoryResponse;
import com.example.kibo.models.Category;
import com.example.kibo.adapters.AdminProductAdapter;
import com.example.kibo.models.Product;

import java.util.ArrayList;
import java.util.List;

public class AdminProductsFragment extends Fragment {
    private RecyclerView rvProducts;
    private EditText etSearch;
    private ImageButton btnAdd;
    private AdminProductAdapter adapter;
    private List<Product> productList;
    private List<Category> categoryList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isFragmentInitialized = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_products, container, false);

        setupViews(view);
        setupData();
        setupListeners();

        return view;
    }

    private void setupViews(View view) {
        rvProducts = view.findViewById(R.id.rv_admin_products);
        etSearch = view.findViewById(R.id.et_search_products);
        btnAdd = view.findViewById(R.id.btn_add_product);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshProducts);

        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupData() {
        productList = new ArrayList<>();
        categoryList = new ArrayList<>();

        // Khởi tạo adapter với category list
        adapter = new AdminProductAdapter(productList, categoryList, this::onEditClick, this::onDeleteClick, this::onProductNameClick);
        rvProducts.setAdapter(adapter);

        // Load categories trước, sau đó load products
        loadCategories();
        isFragmentInitialized = true;
    }

    private void setupListeners() {
        btnAdd.setOnClickListener(v -> openProductForm());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup pull-to-refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Reload categories và products
            loadCategories();
        });
    }

    private void openProductForm() {
        Intent intent = new Intent(getContext(), AdminProductFormActivity.class);
        startActivityForResult(intent, 1001); // Request code 1001 for create
    }

    private void onEditClick(Product product) {
        Intent intent = new Intent(getContext(), AdminProductFormActivity.class);
        intent.putExtra("product_id", product.getProductId());
        intent.putExtra("is_edit", true);
        startActivityForResult(intent, 1002); // Request code 1002 for update
    }

    private void onDeleteClick(Product product) {
        showDeleteConfirmDialog(product);
    }
    
    private void showDeleteConfirmDialog(Product product) {
        if (getContext() == null || !isAdded()) {
            return;
        }
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc muốn xóa sản phẩm '" + product.getProductName() + "'?\n\nHành động này không thể hoàn tác!")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> deleteProduct(product))
                .show();
    }
    
    private void deleteProduct(Product product) {
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(getContext());
        progressDialog.setMessage("Đang xóa sản phẩm...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        ApiService apiService = ApiClient.getApiService();
        apiService.deleteProduct(product.getProductId()).enqueue(new retrofit2.Callback<com.example.kibo.models.ApiResponse<String>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.kibo.models.ApiResponse<String>> call, retrofit2.Response<com.example.kibo.models.ApiResponse<String>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Xóa sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    // Refresh danh sách sản phẩm
                    loadProductsFromApi();
                } else {
                    Toast.makeText(getContext(), "Xóa sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.kibo.models.ApiResponse<String>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Lỗi mạng khi xóa sản phẩm: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void onProductNameClick(Product product) {
        // Hiển thị dialog chi tiết sản phẩm
        com.example.kibo.dialogs.ProductDetailDialog dialog = 
            com.example.kibo.dialogs.ProductDetailDialog.newInstance(product.getProductId());
        dialog.show(getChildFragmentManager(), "ProductDetailDialog");
    }

    private void filterProducts(String query) {
        List<Product> filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            filteredList.addAll(productList);
        } else {
            for (Product product : productList) {
                if (product.getProductName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(product);
                }
            }
        }
        adapter.filterList(filteredList);
    }

    // Load categories từ API
    private void loadCategories() {
        ApiService apiService = ApiClient.getApiService();
        apiService.getCategories().enqueue(new retrofit2.Callback<CategoryResponse>() {
            @Override
            public void onResponse(retrofit2.Call<CategoryResponse> call, retrofit2.Response<CategoryResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    categoryList.clear();
                    categoryList.addAll(response.body().getData());

                    // Update adapter với category list mới
                    adapter.updateCategoryList(categoryList);

                    // Load products sau khi có categories
                    loadProductsFromApi();

                    Toast.makeText(getContext(), "Đã tải " + categoryList.size() + " danh mục", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Tải danh mục thất bại", Toast.LENGTH_SHORT).show();
                    // Vẫn load products dù categories thất bại
                    loadProductsFromApi();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<CategoryResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng khi tải danh mục", Toast.LENGTH_SHORT).show();
                // Vẫn load products dù categories thất bại
                loadProductsFromApi();
            }
        });
    }

    // Load products từ API và bind to adapter
    private void loadProductsFromApi() {
        productList.clear(); // Clear list trước khi load
        loadAllProductsRecursive(1, 100); // Bắt đầu từ page 1, pageSize = 100
    }
    
    // Load tất cả products bằng cách gọi nhiều pages cho đến khi hết
    private void loadAllProductsRecursive(int pageNumber, int pageSize) {
        ApiService apiService = ApiClient.getApiService();
        apiService.getProducts(pageNumber, pageSize).enqueue(new retrofit2.Callback<ProductResponse>() {
            @Override
            public void onResponse(retrofit2.Call<ProductResponse> call, retrofit2.Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProductResponse productResponse = response.body();
                    List<Product> data = productResponse.getData();
                    
                    if (data != null && !data.isEmpty()) {
                        // Thêm products từ page hiện tại vào list
                        productList.addAll(data);
                        
                        // Kiểm tra xem còn page tiếp theo không
                        if (productResponse.isHasNextPage() && productResponse.getCurrentPage() < productResponse.getTotalPages()) {
                            // Nếu còn page tiếp theo, tiếp tục load
                            loadAllProductsRecursive(pageNumber + 1, pageSize);
                        } else {
                            // Đã load hết tất cả pages, cập nhật UI
                            onAllProductsLoaded();
                        }
                    } else {
                        // Không có data, cập nhật UI
                        onAllProductsLoaded();
                    }
                } else {
                    // Nếu page đầu tiên fail thì báo lỗi, nếu page sau fail thì dừng và hiển thị data đã load
                    if (pageNumber == 1) {
                        if (getView() != null && swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Tải sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Đã load được một số pages, hiển thị data đã có
                        onAllProductsLoaded();
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ProductResponse> call, Throwable t) {
                // Nếu page đầu tiên fail thì báo lỗi, nếu page sau fail thì dừng và hiển thị data đã load
                if (pageNumber == 1) {
                    if (getView() != null && swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Lỗi mạng khi tải sản phẩm", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                } else {
                    // Đã load được một số pages, hiển thị data đã có
                    onAllProductsLoaded();
                }
            }
        });
    }
    
    // Method được gọi khi đã load xong tất cả products (hoặc dừng giữa chừng)
    private void onAllProductsLoaded() {
        // Dừng refresh animation nếu view đã được tạo
        if (getView() != null && swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
        
        // Kiểm tra nếu có query trong search box thì filter
        if (etSearch != null) {
            String currentQuery = etSearch.getText().toString().trim();
            if (!currentQuery.isEmpty()) {
                filterProducts(currentQuery);
            } else {
                if (adapter != null && productList != null) {
                    adapter.filterList(new ArrayList<>(productList));
                }
            }
        } else {
            if (adapter != null && productList != null) {
                adapter.filterList(new ArrayList<>(productList));
            }
        }

        if (getContext() != null && productList != null) {
            Toast.makeText(getContext(), "Đã tải " + productList.size() + " sản phẩm", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Không tự động reload trong onResume để tránh crash
        // Chỉ reload khi thực sự cần thiết (ví dụ: sau khi quay lại từ form)
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Nếu create hoặc update thành công, refresh danh sách sản phẩm
        if ((requestCode == 1001 || requestCode == 1002) && resultCode == android.app.Activity.RESULT_OK) {
            // Refresh danh sách sản phẩm sau khi create/update thành công
            if (getContext() != null && isAdded()) {
                refreshProducts();
            }
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Đánh dấu fragment đã pause để tránh các operations không cần thiết
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cleanup khi view bị destroy
    }

    // Method để refresh toàn bộ data (có thể gọi từ bên ngoài)
    public void refreshData() {
        loadCategories();
    }

    // Method để chỉ refresh products (nhanh hơn)
    public void refreshProducts() {
        loadProductsFromApi();
    }

    // Method để set tên sản phẩm vào ô tìm kiếm
    public void setSearchQuery(String productName) {
        if (etSearch != null && productName != null) {
            etSearch.setText(productName);
            
            // Luôn filter ngay lập tức với data hiện có
            if (productList != null && !productList.isEmpty()) {
                filterProducts(productName);
            }
            
            // Nếu chưa có data, load lại
            if (productList == null || productList.isEmpty()) {
                loadProductsFromApiWithFilter(productName);
            }
        }
    }
    
    // Load products với filter sau khi load xong
    private void loadProductsFromApiWithFilter(String filterQuery) {
        productList.clear(); // Clear list trước khi load
        loadAllProductsRecursiveWithFilter(1, 100, filterQuery);
    }
    
    // Load tất cả products với filter bằng cách gọi nhiều pages
    private void loadAllProductsRecursiveWithFilter(int pageNumber, int pageSize, final String filterQuery) {
        ApiService apiService = ApiClient.getApiService();
        apiService.getProducts(pageNumber, pageSize).enqueue(new retrofit2.Callback<ProductResponse>() {
            @Override
            public void onResponse(retrofit2.Call<ProductResponse> call, retrofit2.Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProductResponse productResponse = response.body();
                    List<Product> data = productResponse.getData();
                    
                    if (data != null && !data.isEmpty()) {
                        // Thêm products từ page hiện tại vào list
                        productList.addAll(data);
                        
                        // Kiểm tra xem còn page tiếp theo không
                        if (productResponse.isHasNextPage() && productResponse.getCurrentPage() < productResponse.getTotalPages()) {
                            // Nếu còn page tiếp theo, tiếp tục load
                            loadAllProductsRecursiveWithFilter(pageNumber + 1, pageSize, filterQuery);
                        } else {
                            // Đã load hết tất cả pages, filter và cập nhật UI
                            filterProducts(filterQuery);
                        }
                    } else {
                        // Không có data, filter và cập nhật UI
                        filterProducts(filterQuery);
                    }
                } else {
                    // Nếu có data đã load, vẫn filter
                    if (productList != null && !productList.isEmpty()) {
                        filterProducts(filterQuery);
                    }
                    if (getView() != null && swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ProductResponse> call, Throwable t) {
                // Nếu có data đã load, vẫn filter
                if (productList != null && !productList.isEmpty()) {
                    filterProducts(filterQuery);
                }
                if (getView() != null && swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }
}