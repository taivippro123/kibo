package com.example.kibo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kibo.R;
import com.example.kibo.models.Voucher;

import java.util.List;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {
    
    private List<Voucher> vouchers;
    private double orderValue;
    private OnVoucherClickListener listener;
    
    public interface OnVoucherClickListener {
        void onVoucherClick(Voucher voucher);
    }
    
    public VoucherAdapter(List<Voucher> vouchers, double orderValue, OnVoucherClickListener listener) {
        this.vouchers = vouchers;
        this.orderValue = orderValue;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher, parent, false);
        return new VoucherViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = vouchers.get(position);
        holder.bind(voucher);
    }
    
    @Override
    public int getItemCount() {
        return vouchers.size();
    }
    
    class VoucherViewHolder extends RecyclerView.ViewHolder {
        private TextView textVoucherCode;
        private TextView textVoucherDescription;
        private TextView textDiscountValue;
        private TextView textMinOrder;
        private TextView textExpiryDate;
        private TextView textQuantity;
        private TextView textStatus;
        
        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            textVoucherCode = itemView.findViewById(R.id.text_voucher_code);
            textVoucherDescription = itemView.findViewById(R.id.text_voucher_description);
            textDiscountValue = itemView.findViewById(R.id.text_discount_value);
            textMinOrder = itemView.findViewById(R.id.text_min_order);
            textExpiryDate = itemView.findViewById(R.id.text_expiry_date);
            textQuantity = itemView.findViewById(R.id.text_quantity);
            textStatus = itemView.findViewById(R.id.text_status);
            
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        Voucher voucher = vouchers.get(position);
                        if (voucher.isValidForOrder(orderValue)) {
                            listener.onVoucherClick(voucher);
                        }
                    }
                }
            });
        }
        
        public void bind(Voucher voucher) {
            textVoucherCode.setText(voucher.getCode());
            textVoucherDescription.setText(voucher.getDescription());
            textDiscountValue.setText(voucher.getDiscountDisplayText());
            textMinOrder.setText(voucher.getMinOrderDisplayText());
            
            // Format expiry date
            String expiryDate = voucher.getEndDate();
            if (expiryDate != null && !expiryDate.isEmpty()) {
                try {
                    // Parse date and format it
                    String formattedDate = expiryDate.substring(0, 10); // Get YYYY-MM-DD part
                    textExpiryDate.setText("HSD: " + formattedDate);
                } catch (Exception e) {
                    textExpiryDate.setText("HSD: " + expiryDate);
                }
            } else {
                textExpiryDate.setText("HSD: Không giới hạn");
            }
            
            // Display quantity
            textQuantity.setText("Còn lại: " + voucher.getQuantity());
            
            // Set status based on validity
            boolean isValid = voucher.isValidForOrder(orderValue);
            if (isValid) {
                textStatus.setText("Có thể sử dụng");
                textStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                itemView.setAlpha(1.0f);
            } else {
                textStatus.setText("Không thể sử dụng");
                textStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                itemView.setAlpha(0.6f);
            }
        }
    }
}
