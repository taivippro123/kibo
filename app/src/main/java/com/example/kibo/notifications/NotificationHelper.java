package com.example.kibo.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.kibo.R;

import me.leolin.shortcutbadger.ShortcutBadger;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final String CART_CHANNEL_ID = "cart_badge_channel";
    private static final int BADGE_NOTIFICATION_ID = 999;

    /**
     * Create notification channel for badge (required for Android 8+)
     */
    private static void createBadgeChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null)
                return;

            NotificationChannel channel = new NotificationChannel(
                    CART_CHANNEL_ID,
                    "Cart Badge",
                    NotificationManager.IMPORTANCE_MIN); // MIN = no sound, no popup
            channel.setDescription("Shows cart count on app icon");
            channel.setShowBadge(true); // IMPORTANT: Enable badge
            manager.createNotificationChannel(channel);
            Log.d(TAG, "Badge channel created");
        }
    }

    /**
     * Update app icon badge with cart count
     * Uses both ShortcutBadger AND invisible notification for maximum compatibility
     */
    public static void updateCartBadge(Context context, int count) {
        Log.d(TAG, "updateCartBadge called with count: " + count);

        if (count <= 0) {
            clearCartBadge(context);
            return;
        }

        // Method 1: ShortcutBadger (works on Samsung, Xiaomi, Huawei, etc.)
        try {
            boolean success = ShortcutBadger.applyCount(context, count);
            Log.d(TAG, "ShortcutBadger.applyCount(" + count + ") result: " + success);
        } catch (Exception e) {
            Log.e(TAG, "ShortcutBadger error: " + e.getMessage());
        }

        // Method 2: Invisible notification with badge (works on Pixel, modern Android)
        try {
            createBadgeChannel(context);

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                // Create invisible notification that only shows badge
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CART_CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("") // Empty to hide notification
                        .setContentText("")
                        .setPriority(NotificationCompat.PRIORITY_MIN) // Minimum priority
                        .setOngoing(false)
                        .setAutoCancel(true)
                        .setNumber(count) // This creates the badge
                        .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                        .setNotificationSilent();

                manager.notify(BADGE_NOTIFICATION_ID, builder.build());
                Log.d(TAG, "Badge notification posted with count: " + count);
            }
        } catch (Exception e) {
            Log.e(TAG, "Notification badge error: " + e.getMessage());
        }
    }

    /**
     * Clear badge from app icon
     */
    public static void clearCartBadge(Context context) {
        Log.d(TAG, "clearCartBadge called");

        // Clear ShortcutBadger
        try {
            boolean success = ShortcutBadger.removeCount(context);
            Log.d(TAG, "ShortcutBadger.removeCount() result: " + success);
        } catch (Exception e) {
            Log.e(TAG, "Error clearing ShortcutBadger: " + e.getMessage());
        }

        // Clear notification
        try {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.cancel(BADGE_NOTIFICATION_ID);
                Log.d(TAG, "Badge notification cancelled");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling notification: " + e.getMessage());
        }
    }
}
