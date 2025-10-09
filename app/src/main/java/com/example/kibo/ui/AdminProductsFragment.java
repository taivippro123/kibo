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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kibo.R;
import com.example.kibo.AdminProductFormActivity;
import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.ProductResponse;
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

        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupData() {
        productList = new ArrayList<>();
        adapter = new AdminProductAdapter(productList, this::onEditClick, this::onDeleteClick);
        rvProducts.setAdapter(adapter);
        loadProductsFromApi();
    }

    private void createSampleData() { }

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
    }

    private void openProductForm() {
        Intent intent = new Intent(getContext(), AdminProductFormActivity.class);
        startActivity(intent);
    }

    private void onEditClick(Product product) {
        Intent intent = new Intent(getContext(), AdminProductFormActivity.class);
        intent.putExtra("product_id", product.getProductId());
        intent.putExtra("is_edit", true);
        startActivity(intent);
    }

    private void onDeleteClick(Product product) {
        // TODO: Hiển thị dialog xác nhận xóa
        // showDeleteConfirmDialog(product);
    }

    private void filterProducts(String query) {
        List<Product> filteredList = new ArrayList<>();
        for (Product product : productList) {
            if (product.getProductName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }
        adapter.filterList(filteredList);
    }

    // Load products from API and bind to adapter
    private void loadProductsFromApi() {
        ApiService apiService = ApiClient.getApiService();
        apiService.getAllProducts().enqueue(new retrofit2.Callback<ProductResponse>() {
            @Override
            public void onResponse(retrofit2.Call<ProductResponse> call, retrofit2.Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> data = response.body().getData();
                    if (data != null) {
                        productList.clear();
                        productList.addAll(data);
                        adapter.filterList(new ArrayList<>(productList));
                    }
                } else {
                    Toast.makeText(getContext(), "Tải sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ProductResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng khi tải sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }
}