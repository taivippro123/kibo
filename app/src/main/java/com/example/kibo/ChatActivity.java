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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kibo.adapters.ChatMessageAdapter;
import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.ChatMessage;
import com.example.kibo.models.Conversation;
import com.example.kibo.models.PaginationResponse;
import com.example.kibo.models.SendChatMessageRequest;
import com.example.kibo.utils.SessionManager;
import com.example.kibo.realtime.SignalRManager;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvChatMessages;
    private EditText etMessageInput;
    private ImageButton btnSendMessage;
    private ProgressBar progressLoading;
    private LinearLayout typingIndicator;
    
    private ChatMessageAdapter messageAdapter;
    private List<ChatMessage> messages;
    private ApiService apiService;
    private SessionManager sessionManager;
    private SignalRManager signalRManager;
    
    private int conversationId = -1;
    private int currentUserId;
    private boolean isLoading = false;
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private String pendingMessage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupMessageInput();
        
        apiService = ApiClient.getApiServiceWithAuth(this);
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();
        
        messages = new ArrayList<>();
        messageAdapter = new ChatMessageAdapter(this, messages, currentUserId);
        rvChatMessages.setAdapter(messageAdapter);
        
        startConversation();
        setupAutoRefresh();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar_chat);
        rvChatMessages = findViewById(R.id.rv_chat_messages);
        etMessageInput = findViewById(R.id.et_message_input);
        btnSendMessage = findViewById(R.id.btn_send_message);
        progressLoading = findViewById(R.id.progress_loading);
        typingIndicator = findViewById(R.id.typing_indicator);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chat với Shop");
        }
        
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Auto scroll to bottom
        rvChatMessages.setLayoutManager(layoutManager);
    }

    private void setupMessageInput() {
        etMessageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnSendMessage.setEnabled(s.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSendMessage.setOnClickListener(v -> sendMessage());
    }

    private void startConversation() {
        showLoading(true);
        
        System.out.println("Starting conversation for user: " + currentUserId);
        
        // Debug: Check if token exists
        String token = sessionManager.getAccessToken();
        System.out.println("Access token exists: " + (token != null && !token.isEmpty()));
        if (token != null && !token.isEmpty()) {
            System.out.println("Token length: " + token.length());
        }
        
        Call<Conversation> call = apiService.startConversation();
        call.enqueue(new Callback<Conversation>() {
            @Override
            public void onResponse(Call<Conversation> call, Response<Conversation> response) {
                showLoading(false);
                
                System.out.println("Start conversation response code: " + response.code());
                System.out.println("Start conversation response body: " + response.body());
                
                if (response.isSuccessful() && response.body() != null) {
                    conversationId = response.body().getConversationId();
                    System.out.println("Conversation started with ID: " + conversationId);
                    loadMessages();

                    // Start SignalR after conversation is ready
                    startSignalR();
                    
                    // If there's a pending message, send it now
                    if (pendingMessage != null && !pendingMessage.isEmpty()) {
                        sendPendingMessage();
                    }
                } else {
                    String errorMsg = "Không thể kết nối chat. Mã lỗi: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(ChatActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    
                    // Restore message input if conversation failed
                    if (pendingMessage != null) {
                        etMessageInput.setText(pendingMessage);
                        pendingMessage = null;
                    }
                }
            }

            @Override
            public void onFailure(Call<Conversation> call, Throwable t) {
                showLoading(false);
                System.out.println("Start conversation failed: " + t.getMessage());
                Toast.makeText(ChatActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                
                // Restore message input if conversation failed
                if (pendingMessage != null) {
                    etMessageInput.setText(pendingMessage);
                    pendingMessage = null;
                }
            }
        });
    }

    private void loadMessages() {
        if (conversationId == -1 || isLoading) return;
        
        isLoading = true;
        
        Call<PaginationResponse<ChatMessage>> call = apiService.getMessagesByConversation(
            conversationId, 1, 50
        );
        
        call.enqueue(new Callback<PaginationResponse<ChatMessage>>() {
            @Override
            public void onResponse(Call<PaginationResponse<ChatMessage>> call, Response<PaginationResponse<ChatMessage>> response) {
                isLoading = false;
                
                if (response.isSuccessful() && response.body() != null) {
                    List<ChatMessage> newMessages = response.body().getData();
                    messages.clear();
                    messages.addAll(newMessages);
                    messageAdapter.notifyDataSetChanged();
                    
                    // Scroll to bottom
                    if (!messages.isEmpty()) {
                        rvChatMessages.scrollToPosition(messages.size() - 1);
                    }
                }
            }

            @Override
            public void onFailure(Call<PaginationResponse<ChatMessage>> call, Throwable t) {
                isLoading = false;
                Toast.makeText(ChatActivity.this, "Không thể tải tin nhắn: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String messageText = etMessageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tin nhắn", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable send button and clear input
        btnSendMessage.setEnabled(false);
        etMessageInput.setText("");

        // If conversation is not started, try to start it first
        if (conversationId == -1) {
            startConversation();
            // Store message to send later
            pendingMessage = messageText;
            return;
        }

        // Create message request
        SendChatMessageRequest request = new SendChatMessageRequest(
            conversationId, messageText, ""
        );
        request.setSenderId(currentUserId);

        // Log for debugging
        System.out.println("Sending message: " + messageText + " to conversation: " + conversationId);

        Call<ChatMessage> call = apiService.sendChatMessage(request);
        call.enqueue(new Callback<ChatMessage>() {
            @Override
            public void onResponse(Call<ChatMessage> call, Response<ChatMessage> response) {
                btnSendMessage.setEnabled(true);
                
                System.out.println("Send message response code: " + response.code());
                System.out.println("Send message response body: " + response.body());
                
                if (response.isSuccessful() && response.body() != null) {
                    ChatMessage sentMessage = response.body();
                    sentMessage.setSenderId(currentUserId);
                    sentMessage.setFromShop(false);
                    sentMessage.setSenderName("Bạn");
                    
                    // Add message to adapter
                    messageAdapter.addMessage(sentMessage);
                    
                    // Scroll to bottom
                    rvChatMessages.post(() -> {
                        rvChatMessages.scrollToPosition(messages.size() - 1);
                    });
                    
                    Toast.makeText(ChatActivity.this, "Tin nhắn đã gửi", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = "Không thể gửi tin nhắn. Mã lỗi: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(ChatActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    
                    // Fallback: Add local message if API fails
                    addLocalMessage(messageText);
                }
            }

            @Override
            public void onFailure(Call<ChatMessage> call, Throwable t) {
                btnSendMessage.setEnabled(true);
                System.out.println("Send message failed: " + t.getMessage());
                Toast.makeText(ChatActivity.this, "Lỗi gửi tin nhắn: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                
                // Fallback: Add local message if API fails
                addLocalMessage(messageText);
            }
        });
    }

    private void sendPendingMessage() {
        if (pendingMessage == null || pendingMessage.isEmpty()) {
            return;
        }
        
        String messageToSend = pendingMessage;
        pendingMessage = null; // Clear pending message
        
        // Create message request
        SendChatMessageRequest request = new SendChatMessageRequest(
            conversationId, messageToSend, ""
        );
        request.setSenderId(currentUserId);

        System.out.println("Sending pending message: " + messageToSend + " to conversation: " + conversationId);

        Call<ChatMessage> call = apiService.sendChatMessage(request);
        call.enqueue(new Callback<ChatMessage>() {
            @Override
            public void onResponse(Call<ChatMessage> call, Response<ChatMessage> response) {
                System.out.println("Send pending message response code: " + response.code());
                System.out.println("Send pending message response body: " + response.body());
                
                if (response.isSuccessful() && response.body() != null) {
                    ChatMessage sentMessage = response.body();
                    sentMessage.setSenderId(currentUserId);
                    sentMessage.setFromShop(false);
                    sentMessage.setSenderName("Bạn");
                    
                    // Add message to adapter
                    messageAdapter.addMessage(sentMessage);
                    
                    // Scroll to bottom
                    rvChatMessages.post(() -> {
                        rvChatMessages.scrollToPosition(messages.size() - 1);
                    });
                    
                    Toast.makeText(ChatActivity.this, "Tin nhắn đã gửi", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = "Không thể gửi tin nhắn. Mã lỗi: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(ChatActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    
                    // Fallback: Add local message if API fails
                    addLocalMessage(messageToSend);
                }
            }

            @Override
            public void onFailure(Call<ChatMessage> call, Throwable t) {
                System.out.println("Send pending message failed: " + t.getMessage());
                Toast.makeText(ChatActivity.this, "Lỗi gửi tin nhắn: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                
                // Fallback: Add local message if API fails
                addLocalMessage(messageToSend);
            }
        });
    }

    private void setupAutoRefresh() {
        refreshHandler = new Handler();
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                // Only poll when SignalR is not connected
                if (conversationId != -1 && !isLoading && (signalRManager == null || !signalRManager.isConnected())) {
                    loadMessages();
                }
                refreshHandler.postDelayed(this, 5000); // Refresh every 5 seconds
            }
        };
        refreshHandler.postDelayed(refreshRunnable, 5000);
    }

    private void startSignalR() {
        if (conversationId == -1) return;
        if (signalRManager != null && signalRManager.isConnected()) return;

        // Build base URL from ApiClient base (strip trailing /api/)
        String base = "https://kibo-cbpk.onrender.com"; // ApiClient BASE without /api
        signalRManager = new SignalRManager(this, base);
        signalRManager.setMessageListener(new SignalRManager.MessageListener() {
            @Override
            public void onReceiveMessage(ChatMessage msg) {
                runOnUiThread(() -> {
                    messageAdapter.addMessage(msg);
                    rvChatMessages.scrollToPosition(messages.size() - 1);
                });
            }

            @Override
            public void onConnected() {
                // stop polling
            }

            @Override
            public void onDisconnected(Throwable error) {
                // keep polling fallback
            }
        });
        signalRManager.start(conversationId);
    }

    private void showLoading(boolean show) {
        progressLoading.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void addLocalMessage(String messageText) {
        ChatMessage localMessage = new ChatMessage();
        localMessage.setMessage(messageText);
        localMessage.setSenderId(currentUserId);
        localMessage.setFromShop(false);
        localMessage.setSenderName("Bạn");
        localMessage.setSentAt(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date()));
        
        messageAdapter.addMessage(localMessage);
        
        rvChatMessages.post(() -> {
            rvChatMessages.scrollToPosition(messages.size() - 1);
        });
        
        Toast.makeText(this, "Tin nhắn đã gửi", Toast.LENGTH_SHORT).show();
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
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.postDelayed(refreshRunnable, 1000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
        if (signalRManager != null) {
            signalRManager.stop(conversationId);
        }
    }
}
