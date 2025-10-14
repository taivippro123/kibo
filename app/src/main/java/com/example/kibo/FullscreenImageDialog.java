package com.example.kibo;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.kibo.adapters.FullscreenImageAdapter;
import com.example.kibo.models.ProductImage;

import java.util.ArrayList;
import java.util.List;

public class FullscreenImageDialog extends DialogFragment {
    
    private static final String ARG_IMAGES = "images";
    private static final String ARG_POSITION = "position";
    
    private List<ProductImage> images;
    private int initialPosition;
    
    private ViewPager2 viewPager;
    private TextView tvCounter;
    private ImageButton btnClose;
    private FullscreenImageAdapter adapter;
    
    public static FullscreenImageDialog newInstance(ArrayList<ProductImage> images, int position) {
        FullscreenImageDialog dialog = new FullscreenImageDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_IMAGES, images);
        args.putInt(ARG_POSITION, position);
        dialog.setArguments(args);
        return dialog;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        
        if (getArguments() != null) {
            images = (List<ProductImage>) getArguments().getSerializable(ARG_IMAGES);
            initialPosition = getArguments().getInt(ARG_POSITION, 0);
        }
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_fullscreen_image, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewPager = view.findViewById(R.id.view_pager_fullscreen);
        tvCounter = view.findViewById(R.id.tv_fullscreen_counter);
        btnClose = view.findViewById(R.id.btn_close);
        
        // Setup adapter
        adapter = new FullscreenImageAdapter();
        adapter.setImages(images);
        viewPager.setAdapter(adapter);
        
        // Set initial position
        viewPager.setCurrentItem(initialPosition, false);
        updateCounter(initialPosition);
        
        // Setup page change listener
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateCounter(position);
            }
        });
        
        // Close button
        btnClose.setOnClickListener(v -> dismiss());
    }
    
    private void updateCounter(int position) {
        if (images != null && !images.isEmpty()) {
            tvCounter.setText((position + 1) + "/" + images.size());
        }
    }
    
    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }
}

