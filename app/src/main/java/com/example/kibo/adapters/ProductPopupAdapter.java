package com.example.kibo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.kibo.R;
import com.example.kibo.models.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductPopupAdapter extends RecyclerView.Adapter<ProductPopupAdapter.ProductPopupViewHolder> {

    private Context context;
    private List<Product> products;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductPopupAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductPopupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_popup, parent, false);
        return new ProductPopupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductPopupViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    class ProductPopupViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgProduct;
        private TextView tvProductName;
        private TextView tvStock;
        private TextView tvPrice;
        private ImageView imgArrow;

        public ProductPopupViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvStock = itemView.findViewById(R.id.tv_stock);
            tvPrice = itemView.findViewById(R.id.tv_price);
            imgArrow = itemView.findViewById(R.id.img_arrow);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onProductClick(products.get(position));
                    }
                }
            });

            // Click listener cho mũi tên
            imgArrow.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onProductClick(products.get(position));
                    }
                }
            });
        }

        public void bind(Product product) {
            // Tên sản phẩm
            tvProductName.setText(product.getProductName());

            // Tồn kho
            int quantity = product.getQuantity();
            String stockText = quantity > 0 ? "Còn: " + quantity : "Hết hàng";
            tvStock.setText(stockText);

            // Màu sắc tồn kho - sử dụng cùng logic với AdminProductAdapter
            if (quantity <= 0) {
                // Đỏ: Hết hàng
                tvStock.setBackgroundResource(R.drawable.stock_red_background);
                tvStock.setTextColor(context.getResources().getColor(android.R.color.white));
            } else if (quantity < 10) {
                // Cam: Sắp hết hàng (1-9)
                tvStock.setBackgroundResource(R.drawable.stock_yellow_background);
                tvStock.setTextColor(context.getResources().getColor(android.R.color.white));
            } else {
                // Xanh: Còn nhiều hàng (>=10)
                tvStock.setBackgroundResource(R.drawable.stock_green_background);
                tvStock.setTextColor(context.getResources().getColor(android.R.color.white));
            }

            // Giá sản phẩm
            if (product.getPrice() > 0) {
                NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                String priceText = formatter.format(product.getPrice()) + " VNĐ";
                tvPrice.setText(priceText);
                tvPrice.setVisibility(View.VISIBLE);
            } else {
                tvPrice.setVisibility(View.GONE);
            }

            // Hình ảnh sản phẩm
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(product.getImageUrl())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.placeholder_product)
                        .error(R.drawable.placeholder_product)
                        .into(imgProduct);
            } else {
                imgProduct.setImageResource(R.drawable.placeholder_product);
            }
        }
    }
}
