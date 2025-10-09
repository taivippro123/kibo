package com.example.kibo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.kibo.R;
import com.example.kibo.models.Product;

public class AdminProductFormActivity extends AppCompatActivity {

    private EditText etName, etBrand, etPrice, etDiscount, etStock, etSpecs, etDescription;
    private Spinner spCategory;
    private Button btnPickImages, btnCancel, btnSave;

    private boolean isEditMode = false;
    private int productId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product_form);

        setupViews();
        setupData();
        setupListeners();
    }

    private void setupViews() {
        etName = findViewById(R.id.et_name);
        etBrand = findViewById(R.id.et_brand);
        etPrice = findViewById(R.id.et_price);
        etDiscount = findViewById(R.id.et_discount);
        etStock = findViewById(R.id.et_stock);
        etSpecs = findViewById(R.id.et_specs);
        etDescription = findViewById(R.id.et_description);
        spCategory = findViewById(R.id.sp_category);
        btnPickImages = findViewById(R.id.btn_pick_images);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
    }

    private void setupData() {
        isEditMode = getIntent().getBooleanExtra("is_edit", false);
        productId = getIntent().getIntExtra("product_id", -1);

        if (isEditMode) {
            loadProductData();
        }

        setupCategorySpinner();
    }

    private void loadProductData() {
        // TODO: Load dữ liệu sản phẩm từ API
        // Tạm thời dùng dữ liệu mẫu
        etName.setText("Bàn phím cơ Kibo Pro");
        etPrice.setText("1290000");
    }

    private void setupCategorySpinner() {
        // TODO: Setup category spinner với dữ liệu từ API
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProduct());
        btnPickImages.setOnClickListener(v -> pickImages());
    }

    private void saveProduct() {
        if (validateInput()) {
            if (isEditMode) {
                updateProduct();
            } else {
                createProduct();
            }
        }
    }

    private boolean validateInput() {
        if (etName.getText().toString().trim().isEmpty()) {
            etName.setError("Vui lòng nhập tên sản phẩm");
            return false;
        }
        if (etPrice.getText().toString().trim().isEmpty()) {
            etPrice.setError("Vui lòng nhập giá sản phẩm");
            return false;
        }
        return true;
    }

    private void createProduct() {
        // Tạo Product object với setters
        Product newProduct = new Product();
        newProduct.setProductName(etName.getText().toString().trim());
        newProduct.setPrice(Double.parseDouble(etPrice.getText().toString().trim()));
        newProduct.setBriefDescription(etDescription.getText().toString().trim());
        // ... set các field khác

        // TODO: Call API để tạo sản phẩm mới
        Toast.makeText(this, "Đã tạo sản phẩm thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void updateProduct() {
        // TODO: Call API để cập nhật sản phẩm
        Toast.makeText(this, "Đã cập nhật sản phẩm thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void pickImages() {
        // TODO: Implement image picker
        Toast.makeText(this, "Chức năng chọn ảnh sẽ được implement", Toast.LENGTH_SHORT).show();
    }
}