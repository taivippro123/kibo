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

public class FullscreenImageAdapter extends RecyclerView.Adapter<FullscreenImageAdapter.FullscreenImageViewHolder> {
    
    private List<ProductImage> images = new ArrayList<>();
    
    public void setImages(List<ProductImage> images) {
        this.images = images;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public FullscreenImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fullscreen_image, parent, false);
        return new FullscreenImageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull FullscreenImageViewHolder holder, int position) {
        ProductImage image = images.get(position);
        holder.bind(image);
    }
    
    @Override
    public int getItemCount() {
        return images.size();
    }
    
    static class FullscreenImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        
        public FullscreenImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view_fullscreen);
        }
        
        public void bind(ProductImage image) {
            Glide.with(itemView.getContext())
                    .load(image.getImageUrl())
                    .placeholder(R.drawable.kibo_logo)
                    .error(R.drawable.kibo_logo)
                    .into(imageView);
        }
    }
}

