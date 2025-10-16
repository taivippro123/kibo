package com.example.kibo.adapters;

import android.content.Context;
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
        private TextView tvCustomerEmail;
        private TextView tvLastMessage;
        private TextView tvLastMessageTime;
        private TextView tvUnreadCount;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCustomerAvatar = itemView.findViewById(R.id.iv_customer_avatar);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvCustomerEmail = itemView.findViewById(R.id.tv_customer_email);
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
            tvCustomerEmail.setText("ID: " + (conversation.getCustomerid() != null ? 
                conversation.getCustomerid() : "N/A"));

            // Set last message from lastMessages list
            String lastMessageText = "Chưa có tin nhắn nào";
            if (conversation.getLastMessages() != null && !conversation.getLastMessages().isEmpty()) {
                // Get the last message from the list
                var lastMessage = conversation.getLastMessages().get(conversation.getLastMessages().size() - 1);
                if (lastMessage != null && lastMessage.getMessage() != null) {
                    lastMessageText = lastMessage.getMessage();
                }
            }
            tvLastMessage.setText(lastMessageText);

            // Set last message time
            if (conversation.getLastMessageTime() != null) {
                tvLastMessageTime.setText(timeFormat.format(conversation.getLastMessageTime()));
            } else if (conversation.getCreatedat() != null) {
                tvLastMessageTime.setText(timeFormat.format(conversation.getCreatedat()));
            } else {
                tvLastMessageTime.setText("");
            }

            // Set unread count - using hasUnreadMessages flag
            if (conversation.isHasUnreadMessages()) {
                tvUnreadCount.setVisibility(View.VISIBLE);
                tvUnreadCount.setText("!"); // Show exclamation mark for unread
            } else {
                tvUnreadCount.setVisibility(View.GONE);
            }

            // Set avatar (you can customize this based on customer data)
            ivCustomerAvatar.setImageResource(R.drawable.ic_person);
        }
    }
}
