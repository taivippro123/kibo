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
import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.ApiResponse;
import com.example.kibo.models.ProductResponse;
import com.example.kibo.models.ProductImage;
import com.bumptech.glide.Glide;
import android.app.ProgressDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminProductFormActivity extends AppCompatActivity {

    private EditText etName, etBrief, etFull, etPrice, etQuantity, etConnection, etLayout, etKeycap, etSwitch, etBattery, etOs, etLed, etScreen, etWidth, etLength, etHeight, etDescription;
    private android.widget.AutoCompleteTextView actvCategory;
    private Button btnPickImages, btnCancel, btnSave;
    private java.util.List<android.net.Uri> selectedImageUris = new java.util.ArrayList<>();
    private android.widget.LinearLayout layoutPreviewImages;

    private boolean isEditMode = false;
    private int productId = -1;
    
    // Thêm các field mới cho ảnh hiện tại
    private java.util.List<ProductImage> currentProductImages = new java.util.ArrayList<>();
    private boolean hasLoadedCurrentImages = false;
    private Product currentProduct = null;

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
        etBrief = findViewById(R.id.et_brief);
        etFull = findViewById(R.id.et_full);
        etPrice = findViewById(R.id.et_price);
        etQuantity = findViewById(R.id.et_quantity);
        etConnection = findViewById(R.id.et_connection);
        etLayout = findViewById(R.id.et_layout);
        etKeycap = findViewById(R.id.et_keycap);
        etSwitch = findViewById(R.id.et_switch);
        etBattery = findViewById(R.id.et_battery);
        etOs = findViewById(R.id.et_os);
        etLed = findViewById(R.id.et_led);
        etScreen = findViewById(R.id.et_screen);
        etWidth = findViewById(R.id.et_width);
        etLength = findViewById(R.id.et_length);
        etHeight = findViewById(R.id.et_height);
        etDescription = findViewById(R.id.et_description);
        actvCategory = findViewById(R.id.actv_category);
        btnPickImages = findViewById(R.id.btn_pick_images);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
        // Dùng getIdentifier để tránh lỗi R.id chưa sinh ra ở một số trạng thái cache
        int previewId = getResources().getIdentifier("layout_preview_images", "id", getPackageName());
        layoutPreviewImages = findViewById(previewId);
    }

    private void setupData() {
        isEditMode = getIntent().getBooleanExtra("is_edit", false);
        productId = getIntent().getIntExtra("product_id", -1);

        if (isEditMode) {
            loadProductData();
        }

        setupCategoryDropdown();
    }

    private void loadProductData() {
        // TODO: Load dữ liệu sản phẩm từ API
        if (productId == -1) {
            Toast.makeText(this, "Không tìm thấy ID sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Hiển thị loading
        Toast.makeText(this, "Đang tải thông tin sản phẩm...", Toast.LENGTH_SHORT).show();

        ApiService apiService = ApiClient.getApiService();
        apiService.getProductById(productId).enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Product> products = response.body().getData();
                    if (!products.isEmpty()) {
                        Product product = products.get(0);
                        populateFormWithProductData(product);
                    } else {
                        Toast.makeText(AdminProductFormActivity.this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(AdminProductFormActivity.this, "Lỗi khi tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Toast.makeText(AdminProductFormActivity.this, "Lỗi mạng khi tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    private void populateFormWithProductData(Product product) {
        // Lưu reference
        currentProduct = product;
        
        // Điền thông tin cơ bản
        etName.setText(product.getProductName());
        etBrief.setText(product.getBriefDescription());
        etFull.setText(product.getFullDescription());
        etPrice.setText(String.valueOf((int) product.getPrice()));
        etQuantity.setText(String.valueOf(product.getQuantity()));
        
        // Điền thông tin kỹ thuật
        etConnection.setText(product.getConnection());
        etLayout.setText(product.getLayout());
        etKeycap.setText(product.getKeycap());
        etSwitch.setText(product.getSwitchType());
        etBattery.setText(product.getBattery());
        etOs.setText(product.getOs());
        etLed.setText(product.getLed());
        etScreen.setText(product.getScreen());
        etWidth.setText(String.valueOf(product.getWidth()));
        etLength.setText(String.valueOf(product.getLength()));
        etHeight.setText(String.valueOf(product.getHeight()));

        // Set category (cần load categories trước)
        setSelectedCategory(product.getCategoryId(), product.getCategoryName());

        // Load và hiển thị ảnh hiện tại của sản phẩm
        loadCurrentProductImages(productId);
    }
    private void setSelectedCategory(int categoryId, String categoryName) {
        // Tìm category trong danh sách đã load và set
        for (int i = 0; i < categoryList.size(); i++) {
            com.example.kibo.models.Category category = categoryList.get(i);
            if (category.getCategoryId() == categoryId) {
                actvCategory.setText(category.getCategoryName(), false);
                selectedCategoryId = categoryId;
                break;
            }
        }
    }

    private void loadCurrentProductImages(int productId) {
        if (hasLoadedCurrentImages) return;
        
        ApiService apiService = ApiClient.getApiService();
        apiService.getProductImages(productId).enqueue(new Callback<List<ProductImage>>() {
            @Override
            public void onResponse(Call<List<ProductImage>> call, Response<List<ProductImage>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    currentProductImages.clear();
                    currentProductImages.addAll(response.body());
                    renderImagePreviews(); // Hiển thị ảnh hiện tại
                    hasLoadedCurrentImages = true;
                } else {
                    // Nếu không có ảnh từ API, hiển thị ảnh chính từ Product.imageUrl
                    loadMainProductImage();
                }
            }
            
            @Override
            public void onFailure(Call<List<ProductImage>> call, Throwable t) {
                // Fallback: hiển thị ảnh chính từ Product.imageUrl
                loadMainProductImage();
            }
        });
    }

    private void loadMainProductImage() {
        if (currentProduct != null && currentProduct.getImageUrl() != null && !currentProduct.getImageUrl().isEmpty()) {
            // Tạo một ProductImage giả để hiển thị ảnh chính
            ProductImage mainImage = new ProductImage();
            mainImage.setImageUrl(currentProduct.getImageUrl());
            mainImage.setPrimary(true);
            mainImage.setSortOrder(0);
            
            currentProductImages.clear();
            currentProductImages.add(mainImage);
            renderImagePreviews(); // Hiển thị ảnh chính
        }
        hasLoadedCurrentImages = true;
    }

    private void loadImageFromUrl(String imageUrl, android.widget.ImageView imageView) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_image_placeholder);
            return;
        }
        
        // Backend trả về full URL Cloudinary, không cần xử lý thêm
        // Ví dụ: "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/product1.jpg"
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(imageView);
    }


    // Helper method để tương thích với API 24 - TỐI ƯU HÓA
    private byte[] readAllBytesCompat(java.io.InputStream inputStream) throws IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[524288]; // Tăng buffer lên 512KB để đọc cực nhanh
        
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        
        return buffer.toByteArray();
    }
    
    // Helper method để đọc file tương thích với API 24
    private byte[] readFileCompat(File file) throws IOException {
        java.io.FileInputStream fis = new java.io.FileInputStream(file);
        byte[] bytes = readAllBytesCompat(fis);
        fis.close();
        return bytes;
    }
    
    // Compress ảnh để giảm file size
    private byte[] compressImage(byte[] originalBytes) {
        try {
            android.graphics.Bitmap originalBitmap = android.graphics.BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.length);
            
            // Resize ảnh nếu quá lớn (max 1920x1080) - Giữ nguyên kích thước để đảm bảo độ nét
            int maxWidth = 1920;
            int maxHeight = 1080;
            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();
            
            if (width > maxWidth || height > maxHeight) {
                float scale = Math.min((float) maxWidth / width, (float) maxHeight / height);
                int newWidth = (int) (width * scale);
                int newHeight = (int) (height * scale);
                
                originalBitmap = android.graphics.Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
            }
            
            // Compress với quality 80% - Giữ nguyên quality để đảm bảo độ nét
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            originalBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream);
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            // Nếu compress fail, return ảnh gốc
            return originalBytes;
        }
    }
    
    // Download ảnh từ URL về file tạm
    private File downloadImageToTemp(String imageUrl) {
        try {
            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build();
            
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(imageUrl)
                    .build();
            
            okhttp3.Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                // Tạo file tạm
                File tempFile = File.createTempFile("temp_image_", ".jpg", getCacheDir());
                
                // Ghi dữ liệu vào file
                java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile);
                fos.write(response.body().bytes());
                fos.close();
                response.close();
                
                return tempFile;
            }
            response.close();
        } catch (Exception e) {
            // ignore download error
        }
        return null;
    }

    private java.util.List<com.example.kibo.models.Category> categoryList = new java.util.ArrayList<>();
    private Integer selectedCategoryId = null;

    private void setupCategoryDropdown() {
        ApiClient.getApiService().getCategories().enqueue(new Callback<com.example.kibo.models.CategoryResponse>() {
            @Override
            public void onResponse(Call<com.example.kibo.models.CategoryResponse> call, Response<com.example.kibo.models.CategoryResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    categoryList.clear();
                    categoryList.addAll(response.body().getData());
                    java.util.List<String> names = new java.util.ArrayList<>();
                    for (com.example.kibo.models.Category c : categoryList) names.add(c.getCategoryName());
                    android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                            AdminProductFormActivity.this,
                            R.layout.item_dropdown_text,
                            names
                    );
                    actvCategory.setAdapter(adapter);
                    // Hiển thị dropdown khi focus/click
                    actvCategory.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) actvCategory.showDropDown(); });
                    actvCategory.setOnClickListener(v -> actvCategory.showDropDown());
                    // Chọn item và lưu id
                    actvCategory.setOnItemClickListener((parent, view, position, id) -> {
                        selectedCategoryId = categoryList.get(position).getCategoryId();
                    });
                    // Prefill: nếu có dữ liệu, chọn item đầu để hiện tên
                    // Nếu là edit mode, load product data sau khi categories đã sẵn sàng
                    if (isEditMode && productId != -1) {
                        loadProductData();
                    }
                    else if (!names.isEmpty()) {
                        actvCategory.setText(names.get(0), false);
                        selectedCategoryId = categoryList.get(0).getCategoryId();
                    }
                }
            }

            @Override
            public void onFailure(Call<com.example.kibo.models.CategoryResponse> call, Throwable t) {
                Toast.makeText(AdminProductFormActivity.this, "Lỗi khi tải danh mục", Toast.LENGTH_SHORT).show();
            }
        });
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
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang tạo sản phẩm...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        sendCreateRequest(progressDialog);
    }
    
    private void sendCreateRequest(ProgressDialog progressDialog) {
        ApiService api = ApiClient.getApiService();

        RequestBody productname = RequestBody.create(MediaType.parse("text/plain"), etName.getText().toString().trim());
        RequestBody brief = RequestBody.create(MediaType.parse("text/plain"), etBrief.getText().toString().trim());
        RequestBody full = RequestBody.create(MediaType.parse("text/plain"), etFull.getText().toString().trim());
        RequestBody price = RequestBody.create(MediaType.parse("text/plain"), etPrice.getText().toString().trim());
        RequestBody categoryid = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(selectedCategoryId != null ? selectedCategoryId : 0));
        RequestBody connection = RequestBody.create(MediaType.parse("text/plain"), etConnection.getText().toString().trim());
        RequestBody layout = RequestBody.create(MediaType.parse("text/plain"), etLayout.getText().toString().trim());
        RequestBody keycap = RequestBody.create(MediaType.parse("text/plain"), etKeycap.getText().toString().trim());
        RequestBody switchType = RequestBody.create(MediaType.parse("text/plain"), etSwitch.getText().toString().trim());
        RequestBody battery = RequestBody.create(MediaType.parse("text/plain"), etBattery.getText().toString().trim());
        RequestBody os = RequestBody.create(MediaType.parse("text/plain"), etOs.getText().toString().trim());
        RequestBody led = RequestBody.create(MediaType.parse("text/plain"), etLed.getText().toString().trim());
        RequestBody screen = RequestBody.create(MediaType.parse("text/plain"), etScreen.getText().toString().trim());
        RequestBody width = RequestBody.create(MediaType.parse("text/plain"), etWidth.getText().toString().trim());
        RequestBody length = RequestBody.create(MediaType.parse("text/plain"), etLength.getText().toString().trim());
        RequestBody height = RequestBody.create(MediaType.parse("text/plain"), etHeight.getText().toString().trim());
        RequestBody quantity = RequestBody.create(MediaType.parse("text/plain"), etQuantity.getText().toString().trim().isEmpty()?"0":etQuantity.getText().toString().trim());

        // Tạo danh sách ảnh với compression
        List<MultipartBody.Part> images = new ArrayList<>();
        for (int i = 0; i < selectedImageUris.size(); i++) {
            try {
                java.io.InputStream is = getContentResolver().openInputStream(selectedImageUris.get(i));
                byte[] bytes = readAllBytesCompat(is);
                is.close();
                
                // Compress ảnh để giảm file size
                byte[] compressedBytes = compressImage(bytes);
                
                okhttp3.RequestBody rb = okhttp3.RequestBody.create(okhttp3.MediaType.parse("image/jpeg"), compressedBytes);
                MultipartBody.Part part = MultipartBody.Part.createFormData("ImageFiles", "image_" + i + ".jpg", rb);
                images.add(part);
            } catch (Exception e) {
                // ignore image read error
            }
        }

        api.createProduct(productname, brief, full, price, categoryid, connection, layout, keycap, switchType, battery, os, led, screen, width, length, height, quantity, images)
                .enqueue(new Callback<ApiResponse<String>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                        progressDialog.dismiss();
                        if (response.isSuccessful()) {
                            Toast.makeText(AdminProductFormActivity.this, "Tạo sản phẩm thành công", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AdminProductFormActivity.this, "Tạo sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                        progressDialog.dismiss();
                        Toast.makeText(AdminProductFormActivity.this, "Lỗi mạng khi tạo sản phẩm: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateProduct() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang cập nhật sản phẩm...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        sendUpdateRequest(progressDialog);
    }
    
    private void sendUpdateRequest(ProgressDialog progressDialog) {
        // Chạy việc download ảnh cũ trong background thread
        new Thread(() -> {
            List<MultipartBody.Part> imageFiles = new ArrayList<>();
            
            // Bước 1: Download ảnh cũ còn lại
            for (int i = 0; i < currentProductImages.size(); i++) {
                ProductImage productImage = currentProductImages.get(i);
                try {
                    File tempFile = downloadImageToTemp(productImage.getImageUrl());
                    if (tempFile != null) {
                        byte[] bytes = readFileCompat(tempFile);
                        byte[] compressedBytes = compressImage(bytes);
                        
                        okhttp3.RequestBody rb = okhttp3.RequestBody.create(okhttp3.MediaType.parse("image/jpeg"), compressedBytes);
                        MultipartBody.Part part = MultipartBody.Part.createFormData("ImageFiles", "existing_image_" + i + ".jpg", rb);
                        imageFiles.add(part);
                        
                        tempFile.delete();
                    }
                } catch (Exception e) {
                    // ignore error
                }
            }
            
            // Bước 2: Xử lý ảnh mới
            for (int i = 0; i < selectedImageUris.size(); i++) {
                try {
                    java.io.InputStream is = getContentResolver().openInputStream(selectedImageUris.get(i));
                    byte[] bytes = readAllBytesCompat(is);
                    is.close();
                    
                    byte[] compressedBytes = compressImage(bytes);
                    
                    okhttp3.RequestBody rb = okhttp3.RequestBody.create(okhttp3.MediaType.parse("image/jpeg"), compressedBytes);
                    MultipartBody.Part part = MultipartBody.Part.createFormData("ImageFiles", "new_image_" + i + ".jpg", rb);
                    imageFiles.add(part);
                } catch (Exception e) {
                    // ignore error
                }
            }
            
            // Chạy API call trên main thread
            runOnUiThread(() -> {
                if (imageFiles.isEmpty()) {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Vui lòng chọn ít nhất 1 ảnh để cập nhật", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                ApiService api = ApiClient.getApiService();
                
                RequestBody productid = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(productId));
                RequestBody productname = RequestBody.create(MediaType.parse("text/plain"), etName.getText().toString().trim());
                RequestBody brief = RequestBody.create(MediaType.parse("text/plain"), etBrief.getText().toString().trim());
                RequestBody full = RequestBody.create(MediaType.parse("text/plain"), etFull.getText().toString().trim());
                RequestBody price = RequestBody.create(MediaType.parse("text/plain"), etPrice.getText().toString().trim());
                RequestBody categoryid = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(selectedCategoryId != null ? selectedCategoryId : 0));
                RequestBody connection = RequestBody.create(MediaType.parse("text/plain"), etConnection.getText().toString().trim());
                RequestBody layout = RequestBody.create(MediaType.parse("text/plain"), etLayout.getText().toString().trim());
                RequestBody keycap = RequestBody.create(MediaType.parse("text/plain"), etKeycap.getText().toString().trim());
                RequestBody switchType = RequestBody.create(MediaType.parse("text/plain"), etSwitch.getText().toString().trim());
                RequestBody battery = RequestBody.create(MediaType.parse("text/plain"), etBattery.getText().toString().trim());
                RequestBody os = RequestBody.create(MediaType.parse("text/plain"), etOs.getText().toString().trim());
                RequestBody led = RequestBody.create(MediaType.parse("text/plain"), etLed.getText().toString().trim());
                RequestBody screen = RequestBody.create(MediaType.parse("text/plain"), etScreen.getText().toString().trim());
                RequestBody width = RequestBody.create(MediaType.parse("text/plain"), etWidth.getText().toString().trim());
                RequestBody length = RequestBody.create(MediaType.parse("text/plain"), etLength.getText().toString().trim());
                RequestBody height = RequestBody.create(MediaType.parse("text/plain"), etHeight.getText().toString().trim());
                RequestBody quantity = RequestBody.create(MediaType.parse("text/plain"), etQuantity.getText().toString().trim().isEmpty()?"0":etQuantity.getText().toString().trim());
                RequestBody primaryImageIndex = RequestBody.create(MediaType.parse("text/plain"), "0");

                api.updateProduct(productId, productid, productname, brief, full, price, categoryid, 
                                 connection, layout, keycap, switchType, battery, os, led, screen, 
                                 width, length, height, quantity, imageFiles, primaryImageIndex)
                        .enqueue(new Callback<ApiResponse<String>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                                progressDialog.dismiss();
                                if (response.isSuccessful()) {
                                    Toast.makeText(AdminProductFormActivity.this, "Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(AdminProductFormActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                                progressDialog.dismiss();
                                Toast.makeText(AdminProductFormActivity.this, "Lỗi mạng khi cập nhật: " + t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            });
        }).start();
    }

    private void pickImages() {
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(android.content.Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(android.content.Intent.createChooser(intent, "Chọn ảnh"), 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            selectedImageUris.clear();
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    android.net.Uri uri = data.getClipData().getItemAt(i).getUri();
                    selectedImageUris.add(uri);
                }
            } else if (data.getData() != null) {
                selectedImageUris.add(data.getData());
            }
            renderImagePreviews();
        }
    }

    private void renderImagePreviews() {
        layoutPreviewImages.removeAllViews();
        
        final float scale = getResources().getDisplayMetrics().density;
        int size = (int) (84 * scale);
        int margin = (int) (8 * scale);
        
        // 1. Hiển thị ảnh hiện tại (nếu có) - CÓ THỂ XÓA ĐƯỢC
        for (int i = 0; i < currentProductImages.size(); i++) {
            ProductImage productImage = currentProductImages.get(i);
            
            android.view.View item = getLayoutInflater().inflate(R.layout.item_image_preview, layoutPreviewImages, false);
            android.widget.ImageView iv = item.findViewById(R.id.img_preview);
            android.widget.ImageButton btnRemove = item.findViewById(R.id.btn_remove);
            
            // Load ảnh hiện tại
            loadImageFromUrl(productImage.getImageUrl(), iv);
            
            final int imageIndex = i;
            btnRemove.setOnClickListener(v -> {
                currentProductImages.remove(imageIndex);
                renderImagePreviews(); // Re-render
            });
            
            android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(size, size);
            lp.rightMargin = margin;
            item.setLayoutParams(lp);
            layoutPreviewImages.addView(item);
        }
        
        // 2. Hiển thị ảnh mới được chọn
        for (int i = 0; i < selectedImageUris.size(); i++) {
            android.view.View item = getLayoutInflater().inflate(R.layout.item_image_preview, layoutPreviewImages, false);
            android.widget.ImageView iv = item.findViewById(R.id.img_preview);
            android.widget.ImageButton btnRemove = item.findViewById(R.id.btn_remove);
            iv.setImageURI(selectedImageUris.get(i));
            
            final int index = i;
            btnRemove.setOnClickListener(v -> {
                selectedImageUris.remove(index);
                renderImagePreviews(); // Re-render
            });
            
            android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(size, size);
            lp.rightMargin = margin;
            item.setLayoutParams(lp);
            layoutPreviewImages.addView(item);
        }
    }
}