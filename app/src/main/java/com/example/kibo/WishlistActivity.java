package com.example.kibo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kibo.adapter.WishlistAdapter;
import com.example.kibo.models.Product;
import java.util.ArrayList;
import java.util.List;

public class WishlistActivity extends AppCompatActivity implements WishlistAdapter.OnWishlistItemClickListener {

    private RecyclerView recyclerWishlist;
    private WishlistAdapter wishlistAdapter;
    private List<Product> wishlistItems;
    private List<Product> filteredWishlistItems;

    // UI components
    private EditText edtSearch;
    private TextView tabAkko, tabAula, tabAsus;
    private ImageView btnFilter, btnRating, btnPrice, btnPromo;

    private String currentCategory = "AKKO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        initViews();
        setupRecyclerView();
        setupTabNavigation();
        setupFilterButtons();
        loadWishlistData();
    }

    private void initViews() {
        recyclerWishlist = findViewById(R.id.recycler_wishlist);
        edtSearch = findViewById(R.id.edt_search);
        tabAkko = findViewById(R.id.tab_akko);
        tabAula = findViewById(R.id.tab_aula);
        tabAsus = findViewById(R.id.tab_asus);
        btnFilter = findViewById(R.id.btn_filter);
        btnRating = findViewById(R.id.btn_rating);
        btnPrice = findViewById(R.id.btn_price);
        btnPromo = findViewById(R.id.btn_promo);

        // Setup search functionality
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupRecyclerView() {
        wishlistItems = new ArrayList<>();
        filteredWishlistItems = new ArrayList<>();

        wishlistAdapter = new WishlistAdapter(this, filteredWishlistItems);
        wishlistAdapter.setOnWishlistItemClickListener(this);

        recyclerWishlist.setLayoutManager(new LinearLayoutManager(this));
        recyclerWishlist.setAdapter(wishlistAdapter);
    }

    private void setupTabNavigation() {
        tabAkko.setOnClickListener(v -> selectTab("AKKO", tabAkko));
        tabAula.setOnClickListener(v -> selectTab("AULA", tabAula));
        tabAsus.setOnClickListener(v -> selectTab("ASUS", tabAsus));
    }

    private void setupFilterButtons() {
        btnFilter.setOnClickListener(v -> showFilterDialog());
        btnRating.setOnClickListener(v -> filterByRating());
        btnPrice.setOnClickListener(v -> showPriceFilter());
        btnPromo.setOnClickListener(v -> filterByPromo());
    }

    private void selectTab(String category, TextView selectedTab) {
        // Reset all tabs
        resetTabStyles();

        // Set selected tab style
        selectedTab.setTextColor(getResources().getColor(R.color.primary_color));
        selectedTab.setBackground(getResources().getDrawable(R.drawable.bg_tab_selected));

        currentCategory = category;
        filterByCategory();
    }

    private void resetTabStyles() {
        int grayColor = getResources().getColor(R.color.gray_medium);

        tabAkko.setTextColor(grayColor);
        tabAula.setTextColor(grayColor);
        tabAsus.setTextColor(grayColor);

        tabAkko.setBackground(null);
        tabAula.setBackground(null);
        tabAsus.setBackground(null);
    }

    private void loadWishlistData() {
        // TODO: Load wishlist data from database or API
        // For demo purposes, create some sample data
        createSampleData();
        filterByCategory();
    }

    private void createSampleData() {
        wishlistItems.clear();

        // Sample products for AKKO
        Product product1 = new Product();
        product1.setProductName("Leobog Hi86 TM HI8602");
        product1.setBriefDescription("Trắng đen/ Nimbus V3 switch");
        product1.setPrice(2242000);
        product1.setCategoryName("AKKO");
        wishlistItems.add(product1);

        Product product2 = new Product();
        product2.setProductName("Leobog Hi86 TM HI8602");
        product2.setBriefDescription("Trắng đen/ Nimbus V3 switch");
        product2.setPrice(2242000);
        product2.setCategoryName("AKKO");
        wishlistItems.add(product2);

        Product product3 = new Product();
        product3.setProductName("Leobog Hi86 TM HI8602");
        product3.setBriefDescription("Trắng đen/ Nimbus V3 switch");
        product3.setPrice(2242000);
        product3.setCategoryName("AKKO");
        wishlistItems.add(product3);

        Product product4 = new Product();
        product4.setProductName("Leobog Hi86 TM HI8602");
        product4.setBriefDescription("Trắng đen/ Nimbus V3 switch");
        product4.setPrice(2242000);
        product4.setCategoryName("AKKO");
        wishlistItems.add(product4);

        Product product5 = new Product();
        product5.setProductName("Leobog Hi86 TM HI8602");
        product5.setBriefDescription("Trắng đen/ Nimbus V3 switch");
        product5.setPrice(2242000);
        product5.setCategoryName("AKKO");
        wishlistItems.add(product5);

        // Add sample products for other categories
        Product aulaProduct = new Product();
        aulaProduct.setProductName("AULA F87 Mechanical Keyboard");
        aulaProduct.setBriefDescription("RGB Backlight/ Blue Switch");
        aulaProduct.setPrice(1890000);
        aulaProduct.setCategoryName("AULA");
        wishlistItems.add(aulaProduct);

        Product asusProduct = new Product();
        asusProduct.setProductName("ASUS ROG Strix Scope RX");
        asusProduct.setBriefDescription("Gaming Keyboard/ Cherry MX");
        asusProduct.setPrice(3150000);
        asusProduct.setCategoryName("ASUS");
        wishlistItems.add(asusProduct);
    }

    private void filterByCategory() {
        filteredWishlistItems.clear();
        for (Product product : wishlistItems) {
            if (product.getCategoryName() != null && product.getCategoryName().equals(currentCategory)) {
                filteredWishlistItems.add(product);
            }
        }
        wishlistAdapter.notifyDataSetChanged();
    }

    private void filterProducts(String query) {
        filteredWishlistItems.clear();

        if (query.isEmpty()) {
            filterByCategory();
        } else {
            for (Product product : wishlistItems) {
                if (product.getCategoryName() != null && product.getCategoryName().equals(currentCategory) &&
                        product.getProductName().toLowerCase().contains(query.toLowerCase())) {
                    filteredWishlistItems.add(product);
                }
            }
        }
        wishlistAdapter.notifyDataSetChanged();
    }

    private void showFilterDialog() {
        Toast.makeText(this, "Filter dialog - Coming soon", Toast.LENGTH_SHORT).show();
    }

    private void filterByRating() {
        Toast.makeText(this, "Filter by rating - Coming soon", Toast.LENGTH_SHORT).show();
    }

    private void showPriceFilter() {
        Toast.makeText(this, "Price filter - Coming soon", Toast.LENGTH_SHORT).show();
    }

    private void filterByPromo() {
        Toast.makeText(this, "Filter by promo - Coming soon", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(Product product) {
        // Navigate to product detail
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getProductId());
        startActivity(intent);
    }

    @Override
    public void onRemoveFromWishlist(Product product) {
        // Remove from wishlist
        int position = filteredWishlistItems.indexOf(product);
        if (position != -1) {
            filteredWishlistItems.remove(position);
            wishlistItems.remove(product);
            wishlistAdapter.notifyItemRemoved(position);
            Toast.makeText(this, "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
        }
    }
}
