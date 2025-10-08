package com.example.kibo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kibo.R;
import com.example.kibo.models.CartItem;

import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder> {
    
    public interface OnQuantityClickListener {
        void onQuantityClick(CartItem item);
        void onRemoveClick(CartItem item);
    }
    
    private List<CartItem> cartItems;
    private OnQuantityClickListener onQuantityClickListener;
    private boolean actionsEnabled = true;
    
    public CartItemAdapter(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }
    
    public void setOnQuantityClickListener(OnQuantityClickListener listener) {
        this.onQuantityClickListener = listener;
    }
    
    public void setActionsEnabled(boolean enabled) {
        this.actionsEnabled = enabled;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_product, parent, false);
        return new CartItemViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CartItemViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        holder.bind(cartItem);
    }
    
    @Override
    public int getItemCount() {
        return cartItems.size();
    }
    
    public void updateCartItems(List<CartItem> newCartItems) {
        this.cartItems = newCartItems;
        notifyDataSetChanged();
    }
    
    class CartItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewProduct;
        private TextView textViewProductName;
        private TextView textViewProductPrice;
        private TextView textViewQuantity;
        private TextView textViewQuantityInline;
        
        public CartItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.image_view_product);
            textViewProductName = itemView.findViewById(R.id.text_view_product_name);
            textViewProductPrice = itemView.findViewById(R.id.text_view_product_price);
            textViewQuantity = itemView.findViewById(R.id.text_view_quantity);
            textViewQuantityInline = itemView.findViewById(R.id.text_view_quantity_inline);
            
            itemView.findViewById(R.id.text_view_quantity).setOnClickListener(v -> {
                if (onQuantityClickListener != null) {
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        onQuantityClickListener.onQuantityClick(cartItems.get(pos));
                    }
                }
            });
            itemView.findViewById(R.id.text_view_remove).setOnClickListener(v -> {
                if (onQuantityClickListener != null) {
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        onQuantityClickListener.onRemoveClick(cartItems.get(pos));
                    }
                }
            });
        }
        
        public void bind(CartItem cartItem) {
            textViewProductName.setText(cartItem.getProductName());
            textViewProductPrice.setText(cartItem.getFormattedPrice());
            textViewQuantity.setText(String.valueOf(cartItem.getQuantity()));
            
            // Load product image
            if (cartItem.getImageUrl() != null && !cartItem.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(cartItem.getImageUrl())
                    .placeholder(R.drawable.kibo_logo)
                    .error(R.drawable.kibo_logo)
                    .centerCrop()
                    .into(imageViewProduct);
            } else {
                imageViewProduct.setImageResource(R.drawable.kibo_logo);
            }
            
            // Toggle actions visibility and clickability
            View removeView = itemView.findViewById(R.id.text_view_remove);
            View actionsContainer = itemView.findViewById(R.id.layout_actions_container);
            if (removeView != null) {
                removeView.setVisibility(actionsEnabled ? View.VISIBLE : View.GONE);
            }
            if (actionsContainer != null) {
                actionsContainer.setVisibility(actionsEnabled ? View.VISIBLE : View.GONE);
            }
            textViewQuantity.setClickable(actionsEnabled);
            textViewQuantity.setVisibility(actionsEnabled ? View.VISIBLE : View.GONE);
            
            // Show inline xN when actions are disabled (read-only screen)
            if (textViewQuantityInline != null) {
                if (actionsEnabled) {
                    textViewQuantityInline.setVisibility(View.GONE);
                } else {
                    textViewQuantityInline.setVisibility(View.VISIBLE);
                    textViewQuantityInline.setText("x" + cartItem.getQuantity());
                }
            }
        }
    }
}
