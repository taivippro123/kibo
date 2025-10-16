package com.example.kibo.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kibo.R;
import com.example.kibo.models.ChatMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {
    
    private Context context;
    private List<ChatMessage> messages;
    private int currentUserId;
    private boolean shopPerspective;
    
    public ChatMessageAdapter(Context context, List<ChatMessage> messages, int currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.shopPerspective = false;
    }

    public ChatMessageAdapter(Context context, List<ChatMessage> messages, int currentUserId, boolean shopPerspective) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.shopPerspective = shopPerspective;
    }
    
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        
        // Determine side based on perspective
        boolean isFromUser;
        if (shopPerspective) {
            // In admin/shop perspective, show shop messages on the "user" side variable for reuse
            isFromUser = message.isFromShop();
        } else {
            isFromUser = message.getSenderId() != null && message.getSenderId() == currentUserId;
        }
        
        // Debug log
        System.out.println("Binding message at position " + position + 
                          " - SenderId: " + message.getSenderId() + 
                          " - CurrentUserId: " + currentUserId + 
                          " - IsFromUser: " + isFromUser +
                          " - Message: " + message.getMessage());
        
        if (shopPerspective) {
            if (isFromUser) {
                // In admin view, messages from shop (admin) should appear on the right (user layout)
                holder.layoutUserMessage.setVisibility(View.VISIBLE);
                holder.layoutShopMessage.setVisibility(View.GONE);
                holder.tvUserMessage.setText(message.getMessage());
                holder.tvUserTime.setText(formatTime(message.getSentAt()));
            } else {
                // Customer messages should appear on the left (shop layout)
                holder.layoutShopMessage.setVisibility(View.VISIBLE);
                holder.layoutUserMessage.setVisibility(View.GONE);
                holder.tvShopMessage.setText(message.getMessage());
                holder.tvShopTime.setText(formatTime(message.getSentAt()));
            }
        } else {
            if (isFromUser) {
                holder.layoutUserMessage.setVisibility(View.VISIBLE);
                holder.layoutShopMessage.setVisibility(View.GONE);
                holder.tvUserMessage.setText(message.getMessage());
                holder.tvUserTime.setText(formatTime(message.getSentAt()));
            } else {
                holder.layoutShopMessage.setVisibility(View.VISIBLE);
                holder.layoutUserMessage.setVisibility(View.GONE);
                holder.tvShopMessage.setText(message.getMessage());
                holder.tvShopTime.setText(formatTime(message.getSentAt()));
            }
        }
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    public void updateMessages(List<ChatMessage> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }
    
    public void addMessage(ChatMessage message) {
        System.out.println("Adding message to adapter: " + message.getMessage() + " - SenderId: " + message.getSenderId());
        this.messages.add(message);
        notifyItemInserted(messages.size() - 1);
        System.out.println("Total messages in adapter: " + messages.size());
    }
    
    private String formatTime(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return "";
        }
        
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(timeString);
            
            if (date != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return timeFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        return timeString;
    }
    
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutUserMessage;
        LinearLayout layoutShopMessage;
        TextView tvUserMessage;
        TextView tvUserTime;
        TextView tvUserName;
        TextView tvShopMessage;
        TextView tvShopTime;
        TextView tvShopName;
        
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            
            layoutUserMessage = itemView.findViewById(R.id.layout_user_message);
            layoutShopMessage = itemView.findViewById(R.id.layout_shop_message);
            tvUserMessage = itemView.findViewById(R.id.tv_user_message);
            tvUserTime = itemView.findViewById(R.id.tv_user_time);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvShopMessage = itemView.findViewById(R.id.tv_shop_message);
            tvShopTime = itemView.findViewById(R.id.tv_shop_time);
            tvShopName = itemView.findViewById(R.id.tv_shop_name);
        }
    }
}
