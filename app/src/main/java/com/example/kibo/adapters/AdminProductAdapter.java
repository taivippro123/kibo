package com.example.kibo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.kibo.R;
import com.example.kibo.models.Product;
import com.example.kibo.models.Category; // ✅ THÊM IMPORT
import java.util.ArrayList;
import java.util.List;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private List<Category> categoryList; // ✅ THÊM DANH SÁCH CATEGORIES
    private OnEditClickListener onEditClickListener;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnEditClickListener {
        void onEditClick(Product product);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Product product);
    }

    public AdminProductAdapter(List<Product> productList,
                               List<Category> categoryList, // ✅ THÊM PARAMETER
                               OnEditClickListener onEditClickListener,
                               OnDeleteClickListener onDeleteClickListener) {
        this.productList = new ArrayList<>(productList);
        this.categoryList = new ArrayList<>(categoryList); // ✅ KHỞI TẠO
        this.onEditClickListener = onEditClickListener;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // Gắn dữ liệu từ Product model
        holder.tvName.setText(product.getProductName());
        holder.tvPrice.setText(product.getFormattedPrice());
        holder.tvStock.setText(product.getStockText());

        // ✅ SỬ DỤNG HELPER METHOD
        String categoryName = getCategoryNameById(product.getCategoryId());
        holder.tvCategory.setText("Danh mục: " + categoryName);

        // Load ảnh sản phẩm
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.imgThumb);
        } else {
            holder.imgThumb.setImageResource(R.drawable.ic_launcher_foreground);
        }

        holder.btnEdit.setOnClickListener(v -> onEditClickListener.onEditClick(product));
        holder.btnDelete.setOnClickListener(v -> onDeleteClickListener.onDeleteClick(product));
    }

    // ✅ HELPER METHOD - GIỐNG NHƯ TRONG ADMINPRODUCTFORMACTIVITY
    private String getCategoryNameById(int categoryId) {
        for (Category category : categoryList) {
            if (category.getCategoryId() == categoryId) {
                return category.getCategoryName();
            }
        }
        return "Chưa phân loại";
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void filterList(List<Product> filteredList) {
        productList.clear();
        productList.addAll(filteredList);
        notifyDataSetChanged();
    }

    // ✅ METHOD ĐỂ UPDATE CATEGORY LIST
    public void updateCategoryList(List<Category> newCategoryList) {
        this.categoryList.clear();
        this.categoryList.addAll(newCategoryList);
        notifyDataSetChanged(); // Refresh để hiển thị category names mới
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvName, tvPrice, tvStock;
        TextView tvCategory;
        ImageButton btnEdit, btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.img_thumb);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvStock = itemView.findViewById(R.id.tv_stock);
            tvCategory = itemView.findViewById(R.id.tv_category);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}