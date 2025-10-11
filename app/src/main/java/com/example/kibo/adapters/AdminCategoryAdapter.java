package com.example.kibo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kibo.R;
import com.example.kibo.models.Category;
import java.util.ArrayList;
import java.util.List;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.CategoryViewHolder> {
    
    private List<Category> categoryList;
    private java.util.Map<Integer, Integer> productCountMap; // categoryId -> product count
    private OnEditClickListener onEditClickListener;
    private OnDeleteClickListener onDeleteClickListener;
    
    public interface OnEditClickListener {
        void onEditClick(Category category);
    }
    
    public interface OnDeleteClickListener {
        void onDeleteClick(Category category);
    }
    
    public AdminCategoryAdapter(List<Category> categoryList, 
                               OnEditClickListener onEditClickListener,
                               OnDeleteClickListener onDeleteClickListener) {
        this.categoryList = categoryList != null ? categoryList : new ArrayList<>();
        this.productCountMap = new java.util.HashMap<>();
        this.onEditClickListener = onEditClickListener;
        this.onDeleteClickListener = onDeleteClickListener;
    }
    
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_category, parent, false);
        return new CategoryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.bind(category);
    }
    
    @Override
    public int getItemCount() {
        return categoryList.size();
    }
    
    public void updateList(List<Category> newList) {
        this.categoryList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void filterList(List<Category> filteredList) {
        this.categoryList = filteredList;
        notifyDataSetChanged();
    }
    
    public void updateProductCount(int categoryId, int count) {
        productCountMap.put(categoryId, count);
        notifyDataSetChanged();
    }
    
    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCategoryName;
        private TextView tvCategoryId;
        private ImageButton btnEdit;
        private ImageButton btnDelete;
        
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvCategoryId = itemView.findViewById(R.id.tv_category_id);
            btnEdit = itemView.findViewById(R.id.btn_edit_category);
            btnDelete = itemView.findViewById(R.id.btn_delete_category);
        }
        
        public void bind(Category category) {
            tvCategoryName.setText(category.getCategoryName());
            
            // Hiển thị số lượng sản phẩm thay vì ID
            Integer productCount = productCountMap.get(category.getCategoryId());
            if (productCount != null) {
                tvCategoryId.setText("Tổng số sản phẩm: " + productCount);
            } else {
                tvCategoryId.setText("Đang tải...");
            }
            
            btnEdit.setOnClickListener(v -> {
                if (onEditClickListener != null) {
                    onEditClickListener.onEditClick(category);
                }
            });
            
            btnDelete.setOnClickListener(v -> {
                if (onDeleteClickListener != null) {
                    onDeleteClickListener.onDeleteClick(category);
                }
            });
        }
    }
}
