package com.example.kibo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Make fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialize views
        ImageView logoImageView = findViewById(R.id.logo_image);
        TextView appNameTextView = findViewById(R.id.app_name);
        TextView taglineTextView = findViewById(R.id.tagline);

        // Load animations
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation slideDownAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down);

        // Apply animations
        logoImageView.startAnimation(fadeInAnimation);
        appNameTextView.startAnimation(slideUpAnimation);
        taglineTextView.startAnimation(slideDownAnimation);

        // Navigate to LoginActivity after splash duration
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                
                // Add transition animation
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        }, SPLASH_DURATION);
    }

    @Override
    public void onBackPressed() {
        // Disable back button during splash screen
        // Do nothing
    }
}
