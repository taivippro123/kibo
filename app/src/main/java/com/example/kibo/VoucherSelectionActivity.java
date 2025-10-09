package com.example.kibo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kibo.adapters.VoucherAdapter;
import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.Voucher;
import com.example.kibo.models.VoucherResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoucherSelectionActivity extends AppCompatActivity {
    
    private ApiService apiService;
    private ImageButton buttonBack;
    private TextView textViewTitle;
    private RecyclerView recyclerViewVouchers;
    private VoucherAdapter voucherAdapter;
    private List<Voucher> vouchers = new ArrayList<>();
    private double orderValue;
    private Voucher selectedVoucher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher_selection);
        
        // Get order value from intent
        orderValue = getIntent().getDoubleExtra("order_value", 0);
        
        // Initialize API service
        apiService = ApiClient.getApiService();
        
        // Initialize views
        initViews();
        
        // Setup click listeners
        setupClickListeners();
        
        // Setup recycler view
        setupRecyclerView();
        
        // Load vouchers
        loadVouchers();
    }
    
    private void initViews() {
        buttonBack = findViewById(R.id.button_back);
        textViewTitle = findViewById(R.id.text_view_title);
        recyclerViewVouchers = findViewById(R.id.recycler_view_vouchers);
        
        textViewTitle.setText("Chọn mã giảm giá");
    }
    
    private void setupClickListeners() {
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    
    private void setupRecyclerView() {
        recyclerViewVouchers.setLayoutManager(new LinearLayoutManager(this));
        voucherAdapter = new VoucherAdapter(vouchers, orderValue, new VoucherAdapter.OnVoucherClickListener() {
            @Override
            public void onVoucherClick(Voucher voucher) {
                selectedVoucher = voucher;
                // Return selected voucher to previous activity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_voucher", voucher);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
        recyclerViewVouchers.setAdapter(voucherAdapter);
    }
    
    private void loadVouchers() {
        // Only get active vouchers
        apiService.getVouchers(true).enqueue(new Callback<VoucherResponse>() {
            @Override
            public void onResponse(Call<VoucherResponse> call, Response<VoucherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Voucher> data = response.body().getVouchers();
                    vouchers.clear();
                    vouchers.addAll(data);
                    voucherAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(VoucherSelectionActivity.this, "Không thể tải danh sách voucher", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<VoucherResponse> call, Throwable t) {
                Toast.makeText(VoucherSelectionActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
