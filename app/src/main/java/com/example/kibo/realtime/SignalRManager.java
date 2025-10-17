package com.example.kibo.realtime;

import android.content.Context;
import android.util.Log;

import com.example.kibo.utils.SessionManager;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.microsoft.signalr.TransportEnum;
import com.example.kibo.models.ChatMessage;
import com.google.gson.annotations.SerializedName;

public class SignalRManager {
    public interface MessageListener {
        void onReceiveMessage(ChatMessage message);
        void onConversationUpdated(int conversationId, String lastMessage, String sentAt, int senderId);
        void onConnected();
        void onDisconnected(Throwable error);
    }

    private final Context context;
    private final String baseUrl;
    private final SessionManager sessionManager;
    private HubConnection hubConnection;
    private MessageListener messageListener;

    public SignalRManager(Context context, String baseUrl) {
        this.context = context.getApplicationContext();
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.sessionManager = new SessionManager(context);
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    public void start(int conversationId) {
        String token = sessionManager.getAccessToken();
        if (token == null || token.isEmpty()) {
            Log.w("SignalR", "No access token; cannot start hub");
            return;
        }

        String hubUrl = baseUrl + "/hubs/chat?access_token=" + token;
        hubConnection = HubConnectionBuilder.create(hubUrl)
                .withTransport(TransportEnum.WEBSOCKETS)
                .build();

        hubConnection.on("ReceiveMessage", (msg) -> {
            Log.d("SignalR", "Received message: " + msg);
            if (msg != null) {
                Log.d("SignalR", "Message details - ConversationId: " + msg.getConversationId() + 
                      ", MessageId: " + msg.getChatMessageId() + 
                      ", SenderId: " + msg.getSenderId() + 
                      ", Message: " + msg.getMessage());
            }
            if (messageListener != null) {
                messageListener.onReceiveMessage(msg);
            }
        }, ChatMessage.class);

        hubConnection.on("ConversationUpdated", (rawPayload) -> {
            Log.d("SignalR", "Received ConversationUpdated raw: " + rawPayload);
            Log.d("SignalR", "Raw payload type: " + (rawPayload != null ? rawPayload.getClass().getName() : "null"));
            
            // Try to parse as ConversationUpdatedPayload
            ConversationUpdatedPayload payload = null;
            try {
                if (rawPayload instanceof ConversationUpdatedPayload) {
                    payload = (ConversationUpdatedPayload) rawPayload;
                } else if (rawPayload instanceof com.google.gson.internal.LinkedTreeMap) {
                    // Parse LinkedTreeMap to ConversationUpdatedPayload
                    com.google.gson.internal.LinkedTreeMap<String, Object> map = (com.google.gson.internal.LinkedTreeMap<String, Object>) rawPayload;
                    payload = new ConversationUpdatedPayload();
                    
                    // Parse conversationId (might be Double)
                    Object conversationIdObj = map.get("conversationId");
                    if (conversationIdObj instanceof Double) {
                        payload.conversationId = ((Double) conversationIdObj).intValue();
                    } else if (conversationIdObj instanceof Integer) {
                        payload.conversationId = (Integer) conversationIdObj;
                    }
                    
                    // Parse lastMessage
                    Object lastMessageObj = map.get("lastMessage");
                    payload.lastMessage = lastMessageObj != null ? lastMessageObj.toString() : null;
                    
                    // Parse sentAt
                    Object sentAtObj = map.get("sentAt");
                    payload.sentAt = sentAtObj != null ? sentAtObj.toString() : null;
                    
                    // Parse senderId (might be Double)
                    Object senderIdObj = map.get("senderId");
                    if (senderIdObj instanceof Double) {
                        payload.senderId = ((Double) senderIdObj).intValue();
                    } else if (senderIdObj instanceof Integer) {
                        payload.senderId = (Integer) senderIdObj;
                    }
                    
                    Log.d("SignalR", "Successfully parsed LinkedTreeMap to ConversationUpdatedPayload");
                } else {
                    Log.w("SignalR", "Raw payload is not ConversationUpdatedPayload or LinkedTreeMap type!");
                    return;
                }
            } catch (Exception e) {
                Log.e("SignalR", "Error parsing ConversationUpdated payload: " + e.getMessage());
                return;
            }
            
            if (payload != null) {
                Log.d("SignalR", "ConversationUpdated details - ConversationId: " + payload.conversationId + 
                      ", LastMessage: " + payload.lastMessage + 
                      ", SentAt: " + payload.sentAt + 
                      ", SenderId: " + payload.senderId);
                Log.d("SignalR", "ConversationUpdated toString: " + payload.toString());
                
                if (messageListener != null) {
                    messageListener.onConversationUpdated(payload.conversationId, payload.lastMessage, payload.sentAt, payload.senderId);
                }
            }
        }, Object.class);

        hubConnection.start().subscribe(() -> {
            Log.d("SignalR", "Hub connection started successfully");
            if (messageListener != null) messageListener.onConnected();
            if (conversationId > 0) {
                hubConnection.send("JoinConversationGroup", conversationId);
                Log.d("SignalR", "Joined conversation group: " + conversationId);
            } else {
                Log.d("SignalR", "Started without joining specific conversation group");
            }
        }, error -> {
            Log.e("SignalR", "Hub connection failed: " + error.getMessage());
            if (messageListener != null) messageListener.onDisconnected(error);
        });
    }

    public boolean isConnected() {
        return hubConnection != null && hubConnection.getConnectionState() == HubConnectionState.CONNECTED;
    }

    public void stop(int conversationId) {
        if (hubConnection != null) {
            try {
                hubConnection.send("LeaveConversationGroup", conversationId);
            } catch (Exception ignored) {}
            hubConnection.stop().subscribe(() -> {}, throwable -> {});
        }
    }

    static class ConversationUpdatedPayload {
        @SerializedName("ConversationId")
        public int conversationId;
        @SerializedName("LastMessage")
        public String lastMessage;
        @SerializedName("SentAt")
        public String sentAt;
        @SerializedName("SenderId")
        public int senderId;
        
        @Override
        public String toString() {
            return "ConversationUpdatedPayload{" +
                    "conversationId=" + conversationId +
                    ", lastMessage='" + lastMessage + '\'' +
                    ", sentAt='" + sentAt + '\'' +
                    ", senderId=" + senderId +
                    '}';
        }
    }
}


