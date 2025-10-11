package com.example.kibo.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.kibo.R;
import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.ApiResponse;
import com.example.kibo.models.Category;
import com.example.kibo.models.CategoryRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryFormDialog extends DialogFragment {
    
    private EditText etCategoryName;
    private Button btnSave, btnCancel;
    private int categoryId = -1;
    private String categoryName = "";
    private boolean isEditMode = false;
    private OnCategorySavedListener listener;
    
    public interface OnCategorySavedListener {
        void onCategorySaved();
    }
    
    public static CategoryFormDialog newInstance() {
        return new CategoryFormDialog();
    }
    
    public static CategoryFormDialog newInstance(Category category) {
        CategoryFormDialog dialog = new CategoryFormDialog();
        Bundle args = new Bundle();
        args.putInt("category_id", category.getCategoryId());
        args.putString("category_name", category.getCategoryName());
        dialog.setArguments(args);
        return dialog;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getInt("category_id", -1);
            categoryName = getArguments().getString("category_name", "");
            isEditMode = categoryId != -1 && !categoryName.isEmpty();
        }
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_category_form, container, false);
        
        setupViews(view);
        setupData();
        setupListeners();
        
        return view;
    }
    
    private void setupViews(View view) {
        etCategoryName = view.findViewById(R.id.et_category_name);
        btnSave = view.findViewById(R.id.btn_save_category);
        btnCancel = view.findViewById(R.id.btn_cancel_category);
    }
    
    private void setupData() {
        if (isEditMode && !categoryName.isEmpty()) {
            etCategoryName.setText(categoryName);
            getDialog().setTitle("Chỉnh sửa danh mục");
        } else {
            getDialog().setTitle("Thêm danh mục mới");
        }
    }
    
    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveCategory());
        btnCancel.setOnClickListener(v -> dismiss());
    }
    
    private void saveCategory() {
        String categoryName = etCategoryName.getText().toString().trim();
        
        if (categoryName.isEmpty()) {
            etCategoryName.setError("Vui lòng nhập tên danh mục");
            return;
        }
        
        if (isEditMode) {
            updateCategory(categoryName);
        } else {
            createCategory(categoryName);
        }
    }
    
    private void createCategory(String categoryName) {
        CategoryRequest request = new CategoryRequest(categoryName);
        ApiService apiService = ApiClient.getApiService();
        
        apiService.createCategory(request).enqueue(new Callback<ApiResponse<Category>>() {
            @Override
            public void onResponse(Call<ApiResponse<Category>> call, Response<ApiResponse<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Tạo danh mục thành công", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onCategorySaved();
                    }
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Tạo danh mục thất bại", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Category>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng khi tạo danh mục: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void updateCategory(String categoryName) {
        CategoryRequest request = new CategoryRequest(categoryName);
        ApiService apiService = ApiClient.getApiService();
        
        apiService.updateCategory(categoryId, request).enqueue(new Callback<ApiResponse<Category>>() {
            @Override
            public void onResponse(Call<ApiResponse<Category>> call, Response<ApiResponse<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Cập nhật danh mục thành công", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onCategorySaved();
                    }
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Cập nhật danh mục thất bại", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Category>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng khi cập nhật danh mục: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    public void setOnCategorySavedListener(OnCategorySavedListener listener) {
        this.listener = listener;
    }
}
