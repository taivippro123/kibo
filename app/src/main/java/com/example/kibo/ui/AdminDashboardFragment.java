package com.example.kibo.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kibo.R;
import com.example.kibo.adapters.KpiAdapter;
import com.example.kibo.models.KpiCard;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardFragment extends Fragment {
    private RecyclerView rvKpis;
    private KpiAdapter kpiAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        setupViews(view);
        setupKpiData();

        return view;
    }

    private void setupViews(View view) {
        rvKpis = view.findViewById(R.id.rv_kpis);
        rvKpis.setLayoutManager(new GridLayoutManager(getContext(), 2));
    }

    private void setupKpiData() {
        List<KpiCard> kpiCards = new ArrayList<>();

        // Tạo dữ liệu KPI mẫu
        kpiCards.add(new KpiCard("Tổng sản phẩm", "156", "sản phẩm", R.color.primary_color));
        kpiCards.add(new KpiCard("Đơn hàng hôm nay", "23", "đơn", R.color.green));
        kpiCards.add(new KpiCard("Doanh thu tháng", "45.2M", "đồng", R.color.orange));
        kpiCards.add(new KpiCard("Khách hàng mới", "12", "người", R.color.blue));

        kpiAdapter = new KpiAdapter(kpiCards);
        rvKpis.setAdapter(kpiAdapter);
    }
}