package com.example.kibo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.kibo.R;
import com.example.kibo.models.Product;
import java.util.List;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {
    private List<Product> wishlistItems;
    private Context context;
    private OnWishlistItemClickListener listener;

    public interface OnWishlistItemClickListener {
        void onItemClick(Product product);

        void onRemoveFromWishlist(Product product);
    }

    public WishlistAdapter(Context context, List<Product> wishlistItems) {
        this.context = context;
        this.wishlistItems = wishlistItems;
    }

    public void setOnWishlistItemClickListener(OnWishlistItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wishlist, parent, false);
        return new WishlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        Product product = wishlistItems.get(position);

        // Set product name
        holder.tvProductName.setText(product.getProductName());

        // Set product description using brief description
        holder.tvProductDescription.setText(product.getBriefDescription());

        // Format and set current price using built-in formatter
        holder.tvCurrentPrice.setText(product.getFormattedPrice());

        // Load product image with Glide
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.kibo_logo)
                    .error(R.drawable.kibo_logo)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.kibo_logo);
        }

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(product);
            }
        });

        holder.btnRemoveWishlist.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveFromWishlist(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wishlistItems.size();
    }

    public void updateWishlist(List<Product> newWishlistItems) {
        this.wishlistItems = newWishlistItems;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        wishlistItems.remove(position);
        notifyItemRemoved(position);
    }

    static class WishlistViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName;
        TextView tvProductDescription;
        TextView tvCurrentPrice;
        ImageView btnRemoveWishlist;

        public WishlistViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductDescription = itemView.findViewById(R.id.tv_product_description);
            tvCurrentPrice = itemView.findViewById(R.id.tv_current_price);
            btnRemoveWishlist = itemView.findViewById(R.id.btn_remove_wishlist);
        }
    }
}