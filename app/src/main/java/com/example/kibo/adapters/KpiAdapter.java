package com.example.kibo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kibo.R;
import com.example.kibo.models.KpiCard;
import java.util.List;

public class KpiAdapter extends RecyclerView.Adapter<KpiAdapter.KpiViewHolder> {
    private List<KpiCard> kpiCards;

    public KpiAdapter(List<KpiCard> kpiCards) {
        this.kpiCards = kpiCards;
    }

    @NonNull
    @Override
    public KpiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_kpi_card, parent, false);
        return new KpiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KpiViewHolder holder, int position) {
        KpiCard kpi = kpiCards.get(position);

        holder.tvTitle.setText(kpi.getTitle());
        holder.tvValue.setText(kpi.getValue());
        holder.tvUnit.setText(kpi.getUnit());

        // Set color cho giá trị
        int color = ContextCompat.getColor(holder.itemView.getContext(), kpi.getColorRes());
        holder.tvValue.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return kpiCards.size();
    }

    static class KpiViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvValue, tvUnit;

        public KpiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_kpi_title);
            tvValue = itemView.findViewById(R.id.tv_kpi_value);
            tvUnit = itemView.findViewById(R.id.tv_kpi_unit);
        }
    }
}