package com.example.kibo.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kibo.R;
import com.example.kibo.models.Product;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

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

        // For demo, show original price as 6% more than current price
        double originalPrice = product.getPrice() * 1.06;
        if (originalPrice > product.getPrice()) {
            holder.tvOriginalPrice.setVisibility(View.VISIBLE);
            holder.tvDiscount.setVisibility(View.VISIBLE);

            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            String formattedOriginalPrice = formatter.format(originalPrice) + "đ";
            holder.tvOriginalPrice.setText(formattedOriginalPrice);
            holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            // Calculate discount percentage
            double discountPercent = ((originalPrice - product.getPrice()) / originalPrice) * 100;
            holder.tvDiscount.setText("-" + Math.round(discountPercent) + "%");
        } else {
            holder.tvOriginalPrice.setVisibility(View.GONE);
            holder.tvDiscount.setVisibility(View.GONE);
        }

        // Load product image (you'll need to implement image loading, e.g., with Glide
        // or Picasso)
        // Glide.with(context).load(product.getImageUrl()).into(holder.imgProduct);

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
        TextView tvOriginalPrice;
        TextView tvDiscount;
        ImageView btnRemoveWishlist;

        public WishlistViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductDescription = itemView.findViewById(R.id.tv_product_description);
            tvCurrentPrice = itemView.findViewById(R.id.tv_current_price);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvDiscount = itemView.findViewById(R.id.tv_discount);
            btnRemoveWishlist = itemView.findViewById(R.id.btn_remove_wishlist);
        }
    }
}