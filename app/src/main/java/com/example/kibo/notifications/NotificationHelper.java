package com.example.kibo.notifications;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.kibo.MainActivity;
import com.example.kibo.R;

import me.leolin.shortcutbadger.ShortcutBadger;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final String CART_CHANNEL_ID = "cart_badge_channel_v3"; // v3: Using HIGH importance
    private static final String CART_REMINDER_CHANNEL_ID = "cart_reminder_channel_v2";
    private static final int BADGE_NOTIFICATION_ID = 999;
    private static final int CART_REMINDER_NOTIFICATION_ID = 1000;

    /**
     * Create notification channel for badge (required for Android 8+)
     */
    private static void createBadgeChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null)
                return;

            // Check if channel already exists
            NotificationChannel existingChannel = manager.getNotificationChannel(CART_CHANNEL_ID);
            if (existingChannel != null) {
                // Channel exists, don't recreate
                Log.d(TAG, "Badge channel already exists with importance: " + existingChannel.getImportance());
                return;
            }

            // Create fresh channel
            NotificationChannel channel = new NotificationChannel(
                    CART_CHANNEL_ID,
                    "Cart Badge",
                    NotificationManager.IMPORTANCE_HIGH); // HIGH to force Android to recognize
            channel.setDescription("Shows cart count on app icon");
            channel.setShowBadge(true);
            channel.setSound(null, null); // Silent despite HIGH importance
            channel.enableVibration(false);
            channel.enableLights(false);
            manager.createNotificationChannel(channel);
            Log.d(TAG, "Badge channel created with IMPORTANCE_HIGH (silent)");
        }
    }

    /**
     * Create notification channel for cart reminders
     */
    private static void createCartReminderChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null)
                return;

            NotificationChannel channel = new NotificationChannel(
                    CART_REMINDER_CHANNEL_ID,
                    "Cart Reminders",
                    NotificationManager.IMPORTANCE_HIGH); // HIGH to ensure visibility
            channel.setDescription("Reminds you about items in your cart");
            channel.setShowBadge(true);
            manager.createNotificationChannel(channel);
            Log.d(TAG, "Cart reminder channel created with IMPORTANCE_HIGH");
        }
    }

    /**
     * Update app icon badge with cart count
     * Uses dual approach: ShortcutBadger + invisible notification
     */
    public static void updateCartBadge(Context context, int count) {
        Log.d(TAG, "updateCartBadge called with count: " + count);

        if (count <= 0) {
            clearCartBadge(context);
            return;
        }

        // Try ShortcutBadger first (works on Samsung, Xiaomi, Huawei, some launchers)
        try {
            boolean success = ShortcutBadger.applyCount(context, count);
            Log.d(TAG, "ShortcutBadger.applyCount(" + count + ") result: " + success);

            // If ShortcutBadger failed, use notification-based badge as fallback
            // But only if we have permission
            if (!success && hasNotificationPermission(context)) {
                postBadgeNotification(context, count);
            } else if (!success) {
                Log.w(TAG, "Cannot show badge: ShortcutBadger failed and no POST_NOTIFICATIONS permission");
            }
        } catch (Exception e) {
            Log.e(TAG, "ShortcutBadger error: " + e.getMessage());
            // Fallback to notification-based badge if we have permission
            if (hasNotificationPermission(context)) {
                postBadgeNotification(context, count);
            }
        }
    }

    /**
     * Check if app has POST_NOTIFICATIONS permission (Android 13+)
     */
    private static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        // Below Android 13, notification permission is granted by default
        return true;
    }

    /**
     * Post invisible notification to trigger badge on launchers that don't support
     * ShortcutBadger
     * Requires POST_NOTIFICATIONS permission on Android 13+
     * Only posts if permission is granted
     */
    private static void postBadgeNotification(Context context, int count) {
        // Double check permission before posting
        if (!hasNotificationPermission(context)) {
            Log.w(TAG, "Cannot post badge notification: POST_NOTIFICATIONS permission not granted");
            return;
        }

        try {
            createBadgeChannel(context);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CART_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("") // Empty to make it invisible
                    .setContentText("")
                    .setPriority(NotificationCompat.PRIORITY_MIN) // Minimize visibility
                    .setOngoing(true) // Keep it persistent for badge
                    .setNumber(count) // This triggers the badge
                    .setShowWhen(false)
                    .setAutoCancel(false);

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.notify(BADGE_NOTIFICATION_ID, builder.build());
                Log.d(TAG, "Badge notification posted with count: " + count);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException posting badge notification (missing permission?): " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error posting badge notification: " + e.getMessage());
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

        // Cancel badge notification
        try {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.cancel(BADGE_NOTIFICATION_ID);
                Log.d(TAG, "Badge notification cancelled");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling badge notification: " + e.getMessage());
        }
    }

    /**
     * Show toast notification reminder when user has items in cart
     * Simple alternative that doesn't require notification permission
     */
    public static void showCartReminderNotification(Context context, int itemCount) {
        String message = itemCount == 1
                ? "Bạn có 1 sản phẩm trong giỏ hàng"
                : "Bạn có " + itemCount + " sản phẩm trong giỏ hàng";

        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Cart reminder shown via Toast for " + itemCount + " items");
    }
}
