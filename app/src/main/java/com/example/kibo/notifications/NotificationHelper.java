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
    private static final String CART_CHANNEL_ID = "cart_badge_channel_v4"; // v4: Improved badge configuration
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
                // Channel exists, check if badge is enabled
                Log.d(TAG, "Badge channel exists - Importance: " + existingChannel.getImportance()
                        + ", Badge enabled: " + existingChannel.canShowBadge());
                return;
            }

            // Create fresh channel with proper badge configuration
            NotificationChannel channel = new NotificationChannel(
                    CART_CHANNEL_ID,
                    "Badge giỏ hàng",
                    NotificationManager.IMPORTANCE_LOW); // LOW is enough for badge
            channel.setDescription("Hiển thị số lượng sản phẩm trong giỏ hàng trên icon ứng dụng");
            channel.setShowBadge(true); // CRITICAL: Enable badge for this channel
            channel.setSound(null, null); // Silent
            channel.enableVibration(false); // No vibration
            channel.enableLights(false); // No lights
            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_SECRET); // Don't show on lockscreen
            manager.createNotificationChannel(channel);
            Log.d(TAG, "Badge channel (v4) created - IMPORTANCE_LOW with badge enabled");
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
     * Uses dual approach: ShortcutBadger + notification badge for maximum
     * compatibility
     */
    public static void updateCartBadge(Context context, int count) {
        Log.d(TAG, "updateCartBadge called with count: " + count);

        if (count <= 0) {
            clearCartBadge(context);
            return;
        }

        // Approach 1: Try ShortcutBadger (works on Samsung, Xiaomi, Huawei, some
        // launchers)
        boolean shortcutBadgerSuccess = false;
        try {
            shortcutBadgerSuccess = ShortcutBadger.applyCount(context, count);
            Log.d(TAG, "ShortcutBadger.applyCount(" + count + ") result: " + shortcutBadgerSuccess);
        } catch (Exception e) {
            Log.e(TAG, "ShortcutBadger error: " + e.getMessage());
        }

        // Approach 2: ALWAYS post notification badge if we have permission
        // This ensures badge works on Pixel, stock Android, and launchers that use
        // notifications
        if (hasNotificationPermission(context)) {
            postBadgeNotification(context, count);
            Log.d(TAG, "Posted badge notification for additional launcher support");
        } else {
            Log.w(TAG, "Cannot post badge notification: no POST_NOTIFICATIONS permission");
            // If ShortcutBadger also failed and no permission, badge won't show
            if (!shortcutBadgerSuccess) {
                Log.e(TAG, "Badge cannot be shown: both ShortcutBadger and notification badge unavailable");
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

            // Create intent to open MainActivity when notification is clicked
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CART_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification_cart) // Use cart icon for notification
                    .setContentTitle("Giỏ hàng") // Need title for badge to work
                    .setContentText(count + " sản phẩm") // Need text for badge to work
                    .setPriority(NotificationCompat.PRIORITY_LOW) // LOW instead of MIN
                    .setOngoing(false) // Don't make it ongoing (causes issues)
                    .setNumber(count) // This triggers the badge - CRITICAL!
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL) // Show as badge
                    .setShowWhen(false)
                    .setAutoCancel(true) // Auto dismiss when clicked
                    .setContentIntent(pendingIntent) // Add click action
                    .setOnlyAlertOnce(true) // Don't alert repeatedly
                    .setSound(null) // Silent
                    .setVibrate(null); // No vibration

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
