package com.example.kibo.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * Custom ImageView tự động điều chỉnh kích thước theo tỷ lệ ảnh
 * Đảm bảo ảnh hiển thị đầy đủ và đẹp mắt
 */
public class AspectRatioImageView extends AppCompatImageView {

    public AspectRatioImageView(Context context) {
        super(context);
        init();
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AspectRatioImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setScaleType(ScaleType.FIT_CENTER);
        setAdjustViewBounds(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        // Lấy kích thước ảnh hiện tại
        if (getDrawable() != null) {
            int drawableWidth = getDrawable().getIntrinsicWidth();
            int drawableHeight = getDrawable().getIntrinsicHeight();
            
            if (drawableWidth > 0 && drawableHeight > 0) {
                int measuredWidth = getMeasuredWidth();
                
                // Tính chiều cao dựa trên tỷ lệ ảnh
                float aspectRatio = (float) drawableHeight / drawableWidth;
                int calculatedHeight = (int) (measuredWidth * aspectRatio);
                
                // Giới hạn chiều cao tối đa là 300dp
                int maxHeight = (int) (300 * getResources().getDisplayMetrics().density);
                if (calculatedHeight > maxHeight) {
                    calculatedHeight = maxHeight;
                }
                
                // Giới hạn chiều cao tối thiểu là 150dp
                int minHeight = (int) (150 * getResources().getDisplayMetrics().density);
                if (calculatedHeight < minHeight) {
                    calculatedHeight = minHeight;
                }
                
                setMeasuredDimension(measuredWidth, calculatedHeight);
            }
        }
    }
}
