package com.example.kibo.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kibo.R;
import com.example.kibo.models.ConversationResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.widget.ImageView;

public class AdminConversationAdapter extends RecyclerView.Adapter<AdminConversationAdapter.ConversationViewHolder> {

    private Context context;
    private List<ConversationResponse> conversations;
    private OnConversationClickListener listener;
    private SimpleDateFormat timeFormat;

    public interface OnConversationClickListener {
        void onConversationClick(ConversationResponse conversation);
    }

    public AdminConversationAdapter(Context context, List<ConversationResponse> conversations) {
        this.context = context;
        this.conversations = conversations;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    public void setOnConversationClickListener(OnConversationClickListener listener) {
        this.listener = listener;
    }

    public void updateConversations(List<ConversationResponse> newConversations) {
        this.conversations.clear();
        this.conversations.addAll(newConversations);
        notifyDataSetChanged();
    }

    public void addConversation(ConversationResponse conversation) {
        this.conversations.add(0, conversation);
        notifyItemInserted(0);
    }

    public void updateConversation(ConversationResponse updatedConversation) {
        for (int i = 0; i < conversations.size(); i++) {
            if (conversations.get(i).getConversationid() == updatedConversation.getConversationid()) {
                conversations.set(i, updatedConversation);
                notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            // Silent update without animation
            ConversationResponse conversation = conversations.get(position);
            holder.bind(conversation);
        }
    }

    public void updateConversationMessage(int conversationId, String lastMessage, String sentAt) {
        for (int i = 0; i < conversations.size(); i++) {
            if (conversations.get(i).getConversationid() == conversationId) {
                ConversationResponse conv = conversations.get(i);
                // Update last message smoothly
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

                notifyItemChanged(i);
                break;
            }
        }
    }

    // Add new conversation to top (like ChatMessageAdapter.addMessage)
    public void addConversationToTop(ConversationResponse conversation) {
        conversations.add(0, conversation);
        notifyItemInserted(0);
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        ConversationResponse conversation = conversations.get(position);
        holder.bind(conversation);
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCustomerAvatar;
        private TextView tvCustomerName;
        private TextView tvLastMessage;
        private TextView tvLastMessageTime;
        private TextView tvUnreadCount;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCustomerAvatar = itemView.findViewById(R.id.iv_customer_avatar);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvLastMessageTime = itemView.findViewById(R.id.tv_last_message_time);
            tvUnreadCount = itemView.findViewById(R.id.tv_unread_count);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onConversationClick(conversations.get(position));
                    }
                }
            });
        }

        public void bind(ConversationResponse conversation) {
            tvCustomerName.setText(conversation.getCustomerName() != null ? 
                conversation.getCustomerName() : "Khách hàng");

            // Compute latest preview from lastMessages by sentAt, fall back to conversation timestamps
            String lastMessageText = "Chưa có tin nhắn nào";
            Date latest = conversation.getLastMessageTime();
            if (conversation.getLastMessages() != null && !conversation.getLastMessages().isEmpty()) {
                Date bestTime = null;
                String bestText = null;
                for (com.example.kibo.models.ChatMessage m : conversation.getLastMessages()) {
                    if (m == null) continue;
                    String sent = m.getSentAt();
                    Date parsed = null;
                    if (sent != null) {
                        try {
                            parsed = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", java.util.Locale.getDefault()).parse(sent);
                        } catch (Exception ignore) {
                            try {
                                parsed = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).parse(sent);
                            } catch (Exception ignored) {}
                        }
                    }
                    if (parsed != null && (bestTime == null || parsed.after(bestTime))) {
                        bestTime = parsed;
                        bestText = m.getMessage();
                    }
                }
                if (bestText != null) lastMessageText = bestText;
                if (bestTime != null) latest = bestTime;
            }
            tvLastMessage.setText(lastMessageText);

            // Show latest time
            if (latest != null) {
                tvLastMessageTime.setText(timeFormat.format(latest));
            } else if (conversation.getCreatedat() != null) {
                tvLastMessageTime.setText(timeFormat.format(conversation.getCreatedat()));
            } else {
                tvLastMessageTime.setText("");
            }

            // Set highlighting for new messages
            if (conversation.isHasUnreadMessages()) {
                // Highlight new message with bold black text
                tvLastMessage.setTextColor(context.getResources().getColor(android.R.color.black));
                tvLastMessage.setTypeface(tvLastMessage.getTypeface(), android.graphics.Typeface.BOLD);
                
                // Highlight time with bold black text
                tvLastMessageTime.setTextColor(context.getResources().getColor(android.R.color.black));
                tvLastMessageTime.setTypeface(tvLastMessageTime.getTypeface(), android.graphics.Typeface.BOLD);
                
                // Show unread indicator - small circle without text
                tvUnreadCount.setVisibility(View.VISIBLE);
                tvUnreadCount.setText(""); // Empty text, just show the circle background
            } else {
                // Reset to original layout appearance
                tvLastMessage.setTextColor(context.getResources().getColor(R.color.text_secondary));
                tvLastMessage.setTypeface(null, android.graphics.Typeface.NORMAL);
                
                tvLastMessageTime.setTextColor(context.getResources().getColor(R.color.text_secondary));
                tvLastMessageTime.setTypeface(null, android.graphics.Typeface.NORMAL);
                
                tvUnreadCount.setVisibility(View.GONE);
            }

            // Set avatar (you can customize this based on customer data)
            ivCustomerAvatar.setImageResource(R.drawable.ic_person);
        }
    }
}
