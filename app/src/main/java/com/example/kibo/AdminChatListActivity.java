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
import com.example.kibo.realtime.SignalRManager;

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
    private SignalRManager signalRManager;
    
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
        startSignalR();
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
        
        // Enable smooth animations for item changes
        rvAdminConversations.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator() {
            @Override
            public boolean canReuseUpdatedViewHolder(androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder) {
                return true;
            }
        });
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
                    
                    // Ensure all conversations start with no highlight (read state)
                    for (ConversationResponse conv : conversations) {
                        conv.setHasUnreadMessages(false);
                    }
                    
                    // Sort by latest activity (lastMessageTime fallback to createdat)
                    java.util.Collections.sort(conversations, (a, b) -> {
                        java.util.Date ta = a.getLastMessageTime() != null ? a.getLastMessageTime() : a.getCreatedat();
                        java.util.Date tb = b.getLastMessageTime() != null ? b.getLastMessageTime() : b.getCreatedat();
                        if (ta == null && tb == null) return 0;
                        if (ta == null) return 1; // nulls last
                        if (tb == null) return -1;
                        return tb.compareTo(ta); // desc
                    });
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
                // Chỉ refresh khi SignalR không kết nối và không đang loading
                if (!isLoading && (signalRManager == null || !signalRManager.isConnected())) {
                    System.out.println("SignalR not connected, refreshing conversations...");
                    loadConversations();
                } else if (signalRManager != null && signalRManager.isConnected()) {
                    System.out.println("SignalR connected, skipping auto-refresh");
                }
                // Refresh every 30 seconds
                refreshHandler.postDelayed(this, 30000);
            }
        };
        refreshHandler.postDelayed(refreshRunnable, 30000);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        // Don't hide RecyclerView to avoid flicker during reload
        // rvAdminConversations.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        layoutEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        rvAdminConversations.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onConversationClick(ConversationResponse conversation) {
        // Remove highlight when user clicks on conversation
        conversation.setHasUnreadMessages(false);
        
        // Find and update the conversation in the list
        int index = findConversationIndex(conversation.getConversationid());
        if (index >= 0) {
            conversations.set(index, conversation);
            adapter.notifyItemChanged(index);
        }
        
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
        if (signalRManager != null) {
            // No conversation context at list level; just stop
            signalRManager.stop(-1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Restart SignalR if needed, avoid reload to prevent flicker
        if (signalRManager == null || !signalRManager.isConnected()) {
            startSignalR();
        }
    }

    private void startSignalR() {
        if (signalRManager != null && signalRManager.isConnected()) {
            System.out.println("SignalR already connected, skipping start");
            return;
        }

        System.out.println("Starting SignalR connection...");
        String base = "https://kibo-cbpk.onrender.com";
        signalRManager = new SignalRManager(this, base);
        signalRManager.setMessageListener(new SignalRManager.MessageListener() {
            @Override
            public void onReceiveMessage(com.example.kibo.models.ChatMessage msg) {
                System.out.println("DEBUG: AdminChatListActivity received message: " + msg);
                if (msg != null) {
                    System.out.println("DEBUG: Message details - ConversationId: " + msg.getConversationId() + 
                                      ", MessageId: " + msg.getChatMessageId() + 
                                      ", SenderId: " + msg.getSenderId() + 
                                      ", Message: " + msg.getMessage());
                }
                runOnUiThread(() -> handleIncomingMessage(msg));
            }

            @Override
            public void onConversationUpdated(int conversationId, String lastMessage, String sentAt, int senderId) {
                System.out.println("DEBUG: AdminChatListActivity received ConversationUpdated - ConversationId: " + conversationId + 
                                  ", LastMessage: " + lastMessage + 
                                  ", SentAt: " + sentAt + 
                                  ", SenderId: " + senderId);
                runOnUiThread(() -> {
                    int index = findConversationIndex(conversationId);
                    if (index > 0) {
                        // Update and move in one smooth operation
                        ConversationResponse conv = conversations.get(index);
                        
                        // Update last message content
                        if (conv.getLastMessages() == null) {
                            conv.setLastMessages(new java.util.ArrayList<>());
                        }
                        conv.getLastMessages().clear();
                        com.example.kibo.models.ChatMessage preview = new com.example.kibo.models.ChatMessage();
                        preview.setMessage(lastMessage);
                        preview.setSentAt(sentAt);
                        conv.getLastMessages().add(preview);
                        
                        // Update timestamp
                        try {
                            java.util.Date now = new java.util.Date();
                            conv.setLastMessageTime(now);
                        } catch (Exception ignored) {}
                        
                        // Mark as new message for highlighting
                        conv.setHasUnreadMessages(true);
                        
                        // Move to top with smooth animation
                        conversations.remove(index);
                        conversations.add(0, conv);
                        
                        // Use smooth animation for moving item
                        adapter.notifyItemMoved(index, 0);
                        adapter.notifyItemChanged(0); // Update the moved item's content
                        
                        System.out.println("Moved conversation " + conversationId + " from position " + index + " to top with smooth animation and highlight");
                    } else if (index == 0) {
                        // If already at top, update content and force refresh
                        ConversationResponse conv = conversations.get(0);
                        System.out.println("Updating conversation " + conversationId + " at top with message: " + lastMessage);
                        
                        if (conv.getLastMessages() == null) {
                            conv.setLastMessages(new java.util.ArrayList<>());
                        }
                        conv.getLastMessages().clear();
                        com.example.kibo.models.ChatMessage preview = new com.example.kibo.models.ChatMessage();
                        preview.setMessage(lastMessage);
                        preview.setSentAt(sentAt);
                        conv.getLastMessages().add(preview);
                        try {
                            java.util.Date now = new java.util.Date();
                            conv.setLastMessageTime(now);
                        } catch (Exception ignored) {}
                        
                        // Mark as new message for highlighting
                        conv.setHasUnreadMessages(true);
                        
                        // Update only the top item with smooth animation
                        adapter.notifyItemChanged(0);
                        System.out.println("Updated conversation " + conversationId + " at top with smooth animation and highlight");
                    } else {
                        // If conversation not found, create a temporary conversation entry
                        System.out.println("Conversation " + conversationId + " not found - creating temporary entry");
                        
                        // Tạo conversation tạm thời với thông tin tin nhắn mới
                        ConversationResponse tempConv = new ConversationResponse();
                        tempConv.setConversationid(conversationId);
                        tempConv.setCustomerid(1); // Sẽ được cập nhật khi có thông tin chính xác
                        tempConv.setCustomerName("Khách hàng mới");
                        tempConv.setCreatedat(new java.util.Date());
                        tempConv.setLastMessageTime(new java.util.Date());
                        
                        // Set last message
                        if (tempConv.getLastMessages() == null) {
                            tempConv.setLastMessages(new java.util.ArrayList<>());
                        }
                        com.example.kibo.models.ChatMessage preview = new com.example.kibo.models.ChatMessage();
                        preview.setMessage(lastMessage);
                        preview.setSentAt(sentAt);
                        tempConv.getLastMessages().add(preview);
                        
                        // Mark as new message for highlighting
                        tempConv.setHasUnreadMessages(true);
                        
                        // Add to top of list with smooth animation
                        conversations.add(0, tempConv);
                        adapter.notifyItemInserted(0);
                        
                        System.out.println("Added new conversation " + conversationId + " to top with smooth animation and highlight");
                        
                        // Sau đó cập nhật conversation tạm thời với thông tin chính xác
                        if (shouldReloadForNewConversation(conversationId)) {
                            System.out.println("Updating temporary conversation with accurate data");
                            updateTemporaryConversation(conversationId);
                        }
                    }
                });
            }

            @Override
            public void onConnected() { 
                System.out.println("SignalR connected successfully");
            }

            @Override
            public void onDisconnected(Throwable error) { 
                System.out.println("SignalR disconnected: " + (error != null ? error.getMessage() : "Unknown error"));
            }
        });
        // Start without joining a single conversation group; the hub will still deliver only to groups.
        // Admin will be in no group by default; to receive all, backend would need an admin-wide broadcast.
        // Here we optimistically rely on per-conversation broadcasts received after opening detail; for list, we refresh if needed.
        signalRManager.start(-1);
    }

    private void handleIncomingMessage(com.example.kibo.models.ChatMessage msg) {
        if (msg == null) return;
        int conversationId = msg.getConversationId();
        int index = findConversationIndex(conversationId);
        if (index > 0) {
            // Update and move in one smooth operation
            ConversationResponse conv = conversations.get(index);
            
            // Update last message content
            if (conv.getLastMessages() == null) {
                conv.setLastMessages(new java.util.ArrayList<>());
            }
            conv.getLastMessages().clear();
            com.example.kibo.models.ChatMessage preview = new com.example.kibo.models.ChatMessage();
            preview.setMessage(msg.getMessage());
            preview.setSentAt(msg.getSentAt());
            conv.getLastMessages().add(preview);
            
            // Update timestamp
            try {
                java.util.Date now = new java.util.Date();
                conv.setLastMessageTime(now);
            } catch (Exception ignored) {}
            
            // Mark as new message for highlighting
            conv.setHasUnreadMessages(true);
            
            // Move to top with smooth animation
            conversations.remove(index);
            conversations.add(0, conv);
            
            // Use smooth animation for moving item
            adapter.notifyItemMoved(index, 0);
            adapter.notifyItemChanged(0); // Update the moved item's content
            
            System.out.println("Moved conversation " + conversationId + " from position " + index + " to top with smooth animation and highlight");
        } else if (index == 0) {
            // If already at top, update content and force refresh
            ConversationResponse conv = conversations.get(0);
            System.out.println("Updating conversation " + conversationId + " at top with message: " + msg.getMessage());
            
            if (conv.getLastMessages() == null) {
                conv.setLastMessages(new java.util.ArrayList<>());
            }
            conv.getLastMessages().clear();
            com.example.kibo.models.ChatMessage preview = new com.example.kibo.models.ChatMessage();
            preview.setMessage(msg.getMessage());
            preview.setSentAt(msg.getSentAt());
            conv.getLastMessages().add(preview);
            try {
                java.util.Date now = new java.util.Date();
                conv.setLastMessageTime(now);
            } catch (Exception ignored) {}
            
            // Mark as new message for highlighting
            conv.setHasUnreadMessages(true);
            
            // Update only the top item with smooth animation
            adapter.notifyItemChanged(0);
            System.out.println("Updated conversation " + conversationId + " at top with smooth animation and highlight");
        } else {
            // If conversation not found, create a temporary conversation entry
            System.out.println("Conversation " + conversationId + " not found - creating temporary entry");
            
            // Tạo conversation tạm thời với thông tin tin nhắn mới
            ConversationResponse tempConv = new ConversationResponse();
            tempConv.setConversationid(conversationId);
            tempConv.setCustomerid(1); // Sẽ được cập nhật khi có thông tin chính xác
            tempConv.setCustomerName("Khách hàng mới");
            tempConv.setCreatedat(new java.util.Date());
            tempConv.setLastMessageTime(new java.util.Date());
            
            // Set last message
            if (tempConv.getLastMessages() == null) {
                tempConv.setLastMessages(new java.util.ArrayList<>());
            }
            com.example.kibo.models.ChatMessage preview = new com.example.kibo.models.ChatMessage();
            preview.setMessage(msg.getMessage());
            preview.setSentAt(msg.getSentAt());
            tempConv.getLastMessages().add(preview);
            
            // Mark as new message for highlighting
            tempConv.setHasUnreadMessages(true);
            
            // Add to top of list with smooth animation
            conversations.add(0, tempConv);
            adapter.notifyItemInserted(0);
            
            System.out.println("Added new conversation " + conversationId + " to top with smooth animation and highlight");
            
            // Sau đó cập nhật conversation tạm thời với thông tin chính xác
            if (shouldReloadForNewConversation(conversationId)) {
                System.out.println("Updating temporary conversation with accurate data");
                updateTemporaryConversation(conversationId);
            }
        }
    }

    private int findConversationIndex(int conversationId) {
        System.out.println("Finding conversation ID: " + conversationId + " in " + conversations.size() + " conversations");
        for (int i = 0; i < conversations.size(); i++) {
            int id = conversations.get(i).getConversationid();
            System.out.println("  Checking conversation " + i + ": ID = " + id);
            if (id == conversationId) return i;
        }
        System.out.println("Conversation " + conversationId + " NOT FOUND - will create new conversation!");
        return -1;
    }

    private boolean shouldReloadForNewConversation(int conversationId) {
        // Chỉ reload nếu:
        // 1. Conversation ID hợp lệ (> 0)
        // 2. Chưa reload trong 3 giây gần đây (tránh reload liên tục)
        // 3. Không đang trong quá trình loading
        
        if (conversationId <= 0 || isLoading) {
            return false;
        }
        
        // Kiểm tra thời gian reload cuối cùng
        long currentTime = System.currentTimeMillis();
        if (lastReloadTime > 0 && (currentTime - lastReloadTime) < 3000) {
            System.out.println("Skipping reload - too soon since last reload");
            return false;
        }
        
        lastReloadTime = currentTime;
        return true;
    }

    private void updateTemporaryConversation(int conversationId) {
        // Gọi API để lấy thông tin chính xác của conversation và cập nhật conversation tạm thời
        Call<PaginationResponse<ConversationResponse>> call = apiService.getAllConversations(1, 100);
        call.enqueue(new Callback<PaginationResponse<ConversationResponse>>() {
            @Override
            public void onResponse(Call<PaginationResponse<ConversationResponse>> call, Response<PaginationResponse<ConversationResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ConversationResponse> allConversations = response.body().getData();
                    
                    // Tìm conversation với ID cần thiết
                    for (ConversationResponse conv : allConversations) {
                        if (conv.getConversationid() == conversationId) {
                            // Tìm và cập nhật conversation tạm thời trong danh sách
                            int index = findConversationIndex(conversationId);
                            if (index >= 0) {
                                conversations.set(index, conv);
                                adapter.notifyItemChanged(index);
                                System.out.println("Updated temporary conversation " + conversationId + " with accurate data and smooth animation");
                            }
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<PaginationResponse<ConversationResponse>> call, Throwable t) {
                System.out.println("Failed to update temporary conversation: " + t.getMessage());
            }
        });
    }

    private long lastReloadTime = 0;

}
