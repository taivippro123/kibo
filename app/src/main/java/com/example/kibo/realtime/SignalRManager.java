package com.example.kibo.realtime;

import android.content.Context;
import android.util.Log;

import com.example.kibo.utils.SessionManager;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.microsoft.signalr.TransportEnum;
import com.example.kibo.models.ChatMessage;

public class SignalRManager {
    public interface MessageListener {
        void onReceiveMessage(ChatMessage message);
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
            if (messageListener != null) {
                messageListener.onReceiveMessage(msg);
            }
        }, ChatMessage.class);

        hubConnection.start().subscribe(() -> {
            if (messageListener != null) messageListener.onConnected();
            hubConnection.send("JoinConversationGroup", conversationId);
        }, error -> {
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
}


