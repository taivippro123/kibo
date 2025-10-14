package com.example.kibo.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.kibo.R;

public class OrdersStatusFragment extends Fragment {
    private static final String ARG_STATUS = "status";

    public static OrdersStatusFragment newInstance(String status) {
        OrdersStatusFragment fragment = new OrdersStatusFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_orders_status, container, false);
        String status = getArguments() != null ? getArguments().getString(ARG_STATUS, "ALL") : "ALL";
        TextView tv = root.findViewById(R.id.tv_placeholder);
        tv.setText(status);
        return root;
    }
}


