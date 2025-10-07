package com.example.kibo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.kibo.api.ApiClient;
import com.example.kibo.models.Product;
import com.example.kibo.models.ProductResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {
    private static final String TAG = "ProductDetailActivity";
    public static final String EXTRA_PRODUCT_ID = "product_id";
    
    private ImageView imgProduct;
    private TextView tvProductName;
    private TextView tvPrice;
    private TextView tvBriefDescription;
    private TextView tvFullDescription;
    private TextView tvConnection;
    private TextView tvLayout;
    private TextView tvKeycap;
    private TextView tvSwitch;
    private TextView tvBattery;
    private TextView tvOs;
    private TextView tvLed;
    private TextView tvScreen;
    private ImageButton btnBack;
    private ImageButton btnAddToCart;
    private Button btnBuy;
    private View loadingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initViews();
        
        int productId = getIntent().getIntExtra(EXTRA_PRODUCT_ID, -1);
        
        if (productId != -1) {
            loadProductDetail(productId);
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnBack.setOnClickListener(v -> finish());
        
        btnAddToCart.setOnClickListener(v -> {
            Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            // TODO: Implement add to cart logic
        });
        
        btnBuy.setOnClickListener(v -> {
            Toast.makeText(this, "Chuyển đến thanh toán", Toast.LENGTH_SHORT).show();
            // TODO: Implement buy now logic
        });
    }

    private void initViews() {
        imgProduct = findViewById(R.id.img_product_detail);
        tvProductName = findViewById(R.id.tv_product_name);
        tvPrice = findViewById(R.id.tv_product_price);
        tvBriefDescription = findViewById(R.id.tv_brief_description);
        tvFullDescription = findViewById(R.id.tv_full_description);
        tvConnection = findViewById(R.id.tv_connection);
        tvLayout = findViewById(R.id.tv_layout);
        tvKeycap = findViewById(R.id.tv_keycap);
        tvSwitch = findViewById(R.id.tv_switch);
        tvBattery = findViewById(R.id.tv_battery);
        tvOs = findViewById(R.id.tv_os);
        tvLed = findViewById(R.id.tv_led);
        tvScreen = findViewById(R.id.tv_screen);
        btnBack = findViewById(R.id.btn_back);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);
        btnBuy = findViewById(R.id.btn_buy);
        loadingLayout = findViewById(R.id.loading_layout);
    }

    private void loadProductDetail(int productId) {
        Log.d(TAG, "Loading product detail for ID: " + productId);
        showLoading(true);
        
        ApiClient.getApiService().getProductById(productId).enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ProductResponse productResponse = response.body();
                    
                    if (productResponse.getData() != null && !productResponse.getData().isEmpty()) {
                        Product product = productResponse.getData().get(0);
                        displayProductDetail(product);
                    } else {
                        Toast.makeText(ProductDetailActivity.this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Failed to load product detail", t);
                Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayProductDetail(Product product) {
        tvProductName.setText(product.getProductName());
        tvPrice.setText(product.getFormattedPrice());
        tvBriefDescription.setText(product.getBriefDescription());
        tvFullDescription.setText(product.getFullDescription());
        
        // Load image
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(this)
                .load(product.getImageUrl())
                .placeholder(R.drawable.kibo_logo)
                .error(R.drawable.kibo_logo)
                .centerCrop()
                .into(imgProduct);
        } else {
            imgProduct.setImageResource(R.drawable.kibo_logo);
        }
        
        // Display specifications
        tvConnection.setText(product.getConnection() != null ? product.getConnection() : "N/A");
        tvLayout.setText(product.getLayout() != null ? product.getLayout() : "N/A");
        tvKeycap.setText(product.getKeycap() != null ? product.getKeycap() : "N/A");
        tvSwitch.setText(product.getSwitchType() != null ? product.getSwitchType() : "N/A");
        tvBattery.setText(product.getBattery() != null ? product.getBattery() : "Không");
        tvOs.setText(product.getOs() != null ? product.getOs() : "N/A");
        tvLed.setText(product.getLed() != null ? product.getLed() : "Không");
        tvScreen.setText(product.getScreen() != null ? product.getScreen() : "Không");
    }

    private void showLoading(boolean show) {
        loadingLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}

