package com.example.kibo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kibo.R;
import com.example.kibo.models.ProductImage;

import java.util.ArrayList;
import java.util.List;

public class ProductImageAdapter extends RecyclerView.Adapter<ProductImageAdapter.ImageViewHolder> {
    
    private List<ProductImage> images = new ArrayList<>();
    private OnImageClickListener onImageClickListener;
    
    public interface OnImageClickListener {
        void onImageClick(int position, ProductImage image);
    }
    
    public void setImages(List<ProductImage> images) {
        this.images = images;
        notifyDataSetChanged();
    }
    
    public void setOnImageClickListener(OnImageClickListener listener) {
        this.onImageClickListener = listener;
    }
    
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_image, parent, false);
        return new ImageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ProductImage image = images.get(position);
        holder.bind(image, position);
    }
    
    @Override
    public int getItemCount() {
        return images.size();
    }
    
    class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view_product);
        }
        
        public void bind(ProductImage image, int position) {
            Glide.with(itemView.getContext())
                    .load(image.getImageUrl())
                    .placeholder(R.drawable.kibo_logo)
                    .error(R.drawable.kibo_logo)
                    .centerCrop()
                    .into(imageView);
            
            itemView.setOnClickListener(v -> {
                if (onImageClickListener != null) {
                    onImageClickListener.onImageClick(position, image);
                }
            });
        }
    }
}

