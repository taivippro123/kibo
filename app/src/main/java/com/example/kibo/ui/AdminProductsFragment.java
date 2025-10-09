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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kibo.R;
import com.example.kibo.AdminProductFormActivity;
import com.example.kibo.adapters.AdminProductAdapter;
import com.example.kibo.models.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class AdminProductsFragment extends Fragment {
    private RecyclerView rvProducts;
    private EditText etSearch;
    private ImageButton btnSearch;
    private FloatingActionButton fabAdd;
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
        btnSearch = view.findViewById(R.id.btn_search);
        fabAdd = view.findViewById(R.id.fab_add_product);

        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupData() {
        productList = new ArrayList<>();
        createSampleData();

        adapter = new AdminProductAdapter(productList, this::onEditClick, this::onDeleteClick);
        rvProducts.setAdapter(adapter);
    }

    private void createSampleData() {
        // Tạo Product objects với dữ liệu mẫu
        Product product1 = new Product();
        product1.setProductId(1);
        product1.setProductName("Bàn phím cơ Kibo Pro");
        product1.setPrice(1290000);
        product1.setBriefDescription("Bàn phím cơ cao cấp với switch Cherry MX");
        product1.setCategoryName("Bàn phím cơ");
        product1.setImageUrl("https://example.com/kibo-pro.jpg");
        productList.add(product1);

        Product product2 = new Product();
        product2.setProductId(2);
        product2.setProductName("Bàn phím cơ Kibo RGB");
        product2.setPrice(1590000);
        product2.setBriefDescription("Bàn phím cơ với đèn LED RGB đa màu");
        product2.setCategoryName("Bàn phím cơ");
        product2.setImageUrl("https://example.com/kibo-rgb.jpg");
        productList.add(product2);

        Product product3 = new Product();
        product3.setProductId(3);
        product3.setProductName("Bàn phím cơ Kibo Wireless");
        product3.setPrice(1890000);
        product3.setBriefDescription("Bàn phím cơ không dây tiện lợi");
        product3.setCategoryName("Bàn phím cơ");
        product3.setImageUrl("https://example.com/kibo-wireless.jpg");
        productList.add(product3);
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> openProductForm());

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

    // TODO: Implement API call
    private void loadProductsFromApi() {
        // Call API để lấy danh sách sản phẩm
        // ApiService apiService = ApiClient.getApiService();
        // Call<ProductResponse> call = apiService.getAdminProducts(1, 50);
        // ...
    }
}