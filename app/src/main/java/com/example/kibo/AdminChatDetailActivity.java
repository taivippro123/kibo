package com.example.kibo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kibo.adapters.ChatMessageAdapter;
import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.ChatMessage;
import com.example.kibo.models.PaginationResponse;
import com.example.kibo.models.SendChatMessageRequest;
import com.example.kibo.realtime.SignalRManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.widget.ImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminChatDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private LinearLayout layoutCustomerInfo;
    private ImageView ivCustomerAvatar;
    private TextView tvCustomerName;
    private TextView tvCustomerEmail;
    private TextView tvConversationStatus;
    private RecyclerView rvAdminMessages;
    private ProgressBar progressBar;
    private EditText etAdminMessageInput;
    private ImageButton btnAdminSendMessage;
    
    private ChatMessageAdapter messageAdapter;
    private List<ChatMessage> messages;
    private ApiService apiService;
    
    private int conversationId;
    private int customerId;
    private String customerName;
    private String customerEmail;
    private int currentAdminId;
    private SignalRManager signalRManager;
    
    private boolean isLoading = false;
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private SimpleDateFormat timeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat_detail);

        // Get conversation data from intent
        Intent intent = getIntent();
        conversationId = intent.getIntExtra("conversation_id", -1);
        customerId = intent.getIntExtra("customer_id", -1);
        customerName = intent.getStringExtra("customer_name");
        customerEmail = intent.getStringExtra("customer_email");
        
        // Get admin ID from session (assuming admin is logged in)
        // You might need to get this from SessionManager
        currentAdminId = 1; // This should be the actual admin ID from session
        
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupMessageInput();
        setupCustomerInfo();
        
        apiService = ApiClient.getApiServiceWithAuth(this);
        messages = new ArrayList<>();
        // Admin perspective: pass shopPerspective = true so messages from shop align right
        messageAdapter = new ChatMessageAdapter(this, messages, currentAdminId, true);
        rvAdminMessages.setAdapter(messageAdapter);
        
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        
        loadMessages();
        startSignalR();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar_admin_chat_detail);
        layoutCustomerInfo = findViewById(R.id.layout_customer_info);
        ivCustomerAvatar = findViewById(R.id.iv_customer_avatar_detail);
        tvCustomerName = findViewById(R.id.tv_customer_name_detail);
        tvCustomerEmail = findViewById(R.id.tv_customer_email_detail);
        tvConversationStatus = findViewById(R.id.tv_conversation_status);
        rvAdminMessages = findViewById(R.id.rv_admin_messages);
        progressBar = findViewById(R.id.progress_bar_admin_detail);
        etAdminMessageInput = findViewById(R.id.et_admin_message_input);
        btnAdminSendMessage = findViewById(R.id.btn_admin_send_message);
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
        rvAdminMessages.setLayoutManager(new LinearLayoutManager(this));
        rvAdminMessages.setHasFixedSize(true);
    }

    private void setupCustomerInfo() {
        if (customerName != null) {
            tvCustomerName.setText(customerName);
        }
        if (customerEmail != null) {
            tvCustomerEmail.setText(customerEmail);
        }
        
        // Set avatar (you can customize this)
        ivCustomerAvatar.setImageResource(R.drawable.ic_person);
        
        // Set status
        tvConversationStatus.setText("Đang hoạt động");
    }

    private void setupMessageInput() {
        btnAdminSendMessage.setOnClickListener(v -> sendMessage());
        
        // Enable/disable send button based on input
        etAdminMessageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnAdminSendMessage.setEnabled(!s.toString().trim().isEmpty());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadMessages() {
        if (conversationId == -1 || isLoading) return;
        
        isLoading = true;
        showLoading(true);
        
        System.out.println("Loading messages for conversation: " + conversationId);
        
        Call<PaginationResponse<ChatMessage>> call = apiService.getConversationMessages(
            conversationId, 1, 100
        );
        
        call.enqueue(new Callback<PaginationResponse<ChatMessage>>() {
            @Override
            public void onResponse(Call<PaginationResponse<ChatMessage>> call, Response<PaginationResponse<ChatMessage>> response) {
                showLoading(false);
                isLoading = false;
                
                System.out.println("Load messages response code: " + response.code());
                System.out.println("Load messages response body: " + response.body());
                
                if (response.isSuccessful() && response.body() != null) {
                    List<ChatMessage> newMessages = response.body().getData();
                    messages.clear();
                    messages.addAll(newMessages);
                    messageAdapter.notifyDataSetChanged();
                    
                    // Scroll to bottom
                    if (!messages.isEmpty()) {
                        rvAdminMessages.post(() -> {
                            rvAdminMessages.scrollToPosition(messages.size() - 1);
                        });
                    }
                    
                    System.out.println("Loaded " + messages.size() + " messages");
                } else {
                    String errorMsg = "Không thể tải tin nhắn. Mã lỗi: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(AdminChatDetailActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PaginationResponse<ChatMessage>> call, Throwable t) {
                showLoading(false);
                isLoading = false;
                System.out.println("Load messages failed: " + t.getMessage());
                Toast.makeText(AdminChatDetailActivity.this, "Lỗi tải tin nhắn: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String messageText = etAdminMessageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tin nhắn", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable send button and clear input
        btnAdminSendMessage.setEnabled(false);
        etAdminMessageInput.setText("");

        // Create message request
        SendChatMessageRequest request = new SendChatMessageRequest(
            conversationId, messageText, ""
        );
        request.setSenderId(currentAdminId); // Admin is the sender

        System.out.println("Admin sending message: " + messageText + " to conversation: " + conversationId);

        Call<ChatMessage> call = apiService.sendMessageToCustomer(conversationId, request);
        call.enqueue(new Callback<ChatMessage>() {
            @Override
            public void onResponse(Call<ChatMessage> call, Response<ChatMessage> response) {
                btnAdminSendMessage.setEnabled(true);
                
                System.out.println("Admin send message response code: " + response.code());
                System.out.println("Admin send message response body: " + response.body());
                
                if (response.isSuccessful() && response.body() != null) {
                    ChatMessage sentMessage = response.body();
                    sentMessage.setSenderId(currentAdminId);
                    sentMessage.setFromShop(true); // This is from shop/admin
                    sentMessage.setSenderName("Shop");
                    
                    // Add message to adapter
                    messageAdapter.addMessage(sentMessage);
                    
                    // Scroll to bottom
                    rvAdminMessages.post(() -> {
                        rvAdminMessages.scrollToPosition(messages.size() - 1);
                    });
                    
                    Toast.makeText(AdminChatDetailActivity.this, "Tin nhắn đã gửi", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = "Không thể gửi tin nhắn. Mã lỗi: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(AdminChatDetailActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    etAdminMessageInput.setText(messageText); // Restore message
                }
            }

            @Override
            public void onFailure(Call<ChatMessage> call, Throwable t) {
                btnAdminSendMessage.setEnabled(true);
                System.out.println("Admin send message failed: " + t.getMessage());
                Toast.makeText(AdminChatDetailActivity.this, "Lỗi gửi tin nhắn: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                etAdminMessageInput.setText(messageText); // Restore message
            }
        });
    }

    // Auto refresh disabled to improve UX
    private void setupAutoRefresh() { /* no-op */ }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvAdminMessages.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void startSignalR() {
        if (conversationId == -1) return;
        if (signalRManager != null && signalRManager.isConnected()) return;

        String base = "https://kibo-cbpk.onrender.com";
        signalRManager = new SignalRManager(this, base);
        signalRManager.setMessageListener(new SignalRManager.MessageListener() {
            @Override
            public void onReceiveMessage(ChatMessage msg) {
                runOnUiThread(() -> {
                    messageAdapter.addMessage(msg);
                    rvAdminMessages.scrollToPosition(messages.size() - 1);
                });
            }

            @Override
            public void onConnected() { }

            @Override
            public void onDisconnected(Throwable error) { }
        });
        signalRManager.start(conversationId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
        if (signalRManager != null) {
            signalRManager.stop(conversationId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Avoid reload to prevent flicker; rely on SignalR updates
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (signalRManager != null) {
            signalRManager.stop(conversationId);
        }
    }
}
