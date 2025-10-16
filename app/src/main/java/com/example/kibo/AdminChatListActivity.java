package com.example.kibo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kibo.adapters.AdminConversationAdapter;
import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.ConversationResponse;
import com.example.kibo.models.PaginationResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminChatListActivity extends AppCompatActivity implements AdminConversationAdapter.OnConversationClickListener {

    private Toolbar toolbar;
    private RecyclerView rvAdminConversations;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    
    private AdminConversationAdapter adapter;
    private List<ConversationResponse> conversations;
    private ApiService apiService;
    
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat_list);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        
        apiService = ApiClient.getApiServiceWithAuth(this);
        conversations = new ArrayList<>();
        adapter = new AdminConversationAdapter(this, conversations);
        adapter.setOnConversationClickListener(this);
        rvAdminConversations.setAdapter(adapter);
        
        loadConversations();
        setupAutoRefresh();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar_admin_chat);
        rvAdminConversations = findViewById(R.id.rv_admin_conversations);
        progressBar = findViewById(R.id.progress_bar_admin_chat);
        layoutEmpty = findViewById(R.id.layout_empty_admin_chat);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        rvAdminConversations.setLayoutManager(new LinearLayoutManager(this));
        rvAdminConversations.setHasFixedSize(true);
    }

    private void loadConversations() {
        if (isLoading) return;
        
        showLoading(true);
        isLoading = true;
        
        System.out.println("Loading admin conversations...");
        
        // Debug: Check if API service is using authenticated client
        System.out.println("API Service: " + (apiService != null ? "NOT NULL" : "NULL"));
        
        Call<PaginationResponse<ConversationResponse>> call = apiService.getAllConversations(1, 20);
        call.enqueue(new Callback<PaginationResponse<ConversationResponse>>() {
            @Override
            public void onResponse(Call<PaginationResponse<ConversationResponse>> call, Response<PaginationResponse<ConversationResponse>> response) {
                showLoading(false);
                isLoading = false;
                
                System.out.println("Load conversations response code: " + response.code());
                System.out.println("Load conversations response body: " + response.body());
                System.out.println("Response headers: " + response.headers());
                System.out.println("Response message: " + response.message());
                
                if (response.isSuccessful() && response.body() != null) {
                    conversations.clear();
                    conversations.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                    
                    if (conversations.isEmpty()) {
                        showEmptyState(true);
                    } else {
                        showEmptyState(false);
                    }
                    
                    System.out.println("Loaded " + conversations.size() + " conversations");
                } else {
                    String errorMsg = "Không thể tải danh sách cuộc trò chuyện. Mã lỗi: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(AdminChatListActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    showEmptyState(true);
                }
            }

            @Override
            public void onFailure(Call<PaginationResponse<ConversationResponse>> call, Throwable t) {
                showLoading(false);
                isLoading = false;
                System.out.println("Load conversations failed: " + t.getMessage());
                Toast.makeText(AdminChatListActivity.this, "Lỗi tải danh sách: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyState(true);
            }
        });
    }

    private void setupAutoRefresh() {
        refreshHandler = new Handler();
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isLoading) {
                    loadConversations();
                }
                // Refresh every 30 seconds
                refreshHandler.postDelayed(this, 30000);
            }
        };
        refreshHandler.postDelayed(refreshRunnable, 30000);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvAdminConversations.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        layoutEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        rvAdminConversations.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onConversationClick(ConversationResponse conversation) {
        Intent intent = new Intent(this, AdminChatDetailActivity.class);
        intent.putExtra("conversation_id", conversation.getConversationid());
        intent.putExtra("customer_id", conversation.getCustomerid());
        intent.putExtra("customer_name", conversation.getCustomerName());
        intent.putExtra("customer_email", "ID: " + conversation.getCustomerid());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh conversations when returning to this activity
        loadConversations();
    }
}
