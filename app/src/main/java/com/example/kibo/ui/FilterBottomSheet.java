package com.example.kibo.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.kibo.R;
import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.Product;
import com.example.kibo.models.ProductResponse;
import com.example.kibo.models.CategoryResponse;
import com.example.kibo.models.Category;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FilterBottomSheet extends BottomSheetDialogFragment {

    private ApiService apiService;
    private OnFilterAppliedListener listener;
    private Spinner spinnerCategory;
    private List<Category> categories = new ArrayList<>();

    public interface OnFilterAppliedListener {
        void onFilterApplied(List<Product> filteredProducts);
    }

    public void setOnFilterAppliedListener(OnFilterAppliedListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottomsheet_filter, container, false);
        apiService = ApiClient.getApiService();

        RadioGroup priceGroup = view.findViewById(R.id.price_group);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        Button btnApply = view.findViewById(R.id.btn_apply);

        loadCategories(); // 🔹 Gọi API lấy danh mục khi mở filter

        btnApply.setOnClickListener(v -> {
            int selectedId = priceGroup.getCheckedRadioButtonId();
            Double minPrice = null;
            Double maxPrice = null;

            if (selectedId == R.id.rb_under1m) {
                maxPrice = 1000000.0;
            } else if (selectedId == R.id.rb_1to2m) {
                minPrice = 1000000.0;
                maxPrice = 2000000.0;
            } else if (selectedId == R.id.rb_above2m) {
                minPrice = 2000000.0;
            }

            int selectedPosition = spinnerCategory.getSelectedItemPosition();
            Integer categoryId = selectedPosition > 0 ? categories.get(selectedPosition - 1).getCategoryId() : null;

            filterProducts(categoryId, minPrice, maxPrice);
        });

        return view;
    }

    private void loadCategories() {
        apiService.getCategories().enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(Call<CategoryResponse> call, Response<CategoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body().getData();

                    List<String> names = new ArrayList<>();
                    names.add("Tất cả");
                    for (Category c : categories) {
                        names.add(c.getCategoryName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_item, names);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterProducts(Integer categoryId, Double minPrice, Double maxPrice) {
        apiService.getProductsFiltered(categoryId, minPrice, maxPrice)
                .enqueue(new Callback<ProductResponse>() {
                    @Override
                    public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            List<Product> products = response.body().getData();

                            if (listener != null) {
                                listener.onFilterApplied(products);
                            }

                            Toast.makeText(getContext(),
                                    "Tìm thấy " + products.size() + " sản phẩm", Toast.LENGTH_SHORT).show();
                            dismiss();
                        } else {
                            Toast.makeText(getContext(), "Không có sản phẩm phù hợp", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ProductResponse> call, Throwable t) {
                        Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
