package com.example.kibo.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.kibo.MainActivity;
import com.example.kibo.R;

import me.leolin.shortcutbadger.ShortcutBadger;

public class NotificationHelper {
    public static final String CART_CHANNEL_ID = "cart_channel";
    private static final int CART_NOTIFICATION_ID = 1001;

    /**
     * Create notification channel for cart updates (Android O+)
     */
    public static void createCartNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null)
                return;

            // Use DEFAULT importance so launcher can show numeric badges on supported
            // launchers
            NotificationChannel channel = new NotificationChannel(
                    CART_CHANNEL_ID,
                    "Cập nhật giỏ hàng",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Hiển thị số lượng sản phẩm trong giỏ hàng");
            // Allow launcher to show badge for this channel
            channel.setShowBadge(true);
            manager.createNotificationChannel(channel);
        }
    }

    /**
     * Show notification with cart count AND set app icon badge using ShortcutBadger
     * This ensures badge displays on app icon across different launchers
     */
    public static void showCartNotification(Context context, int count) {
        createCartNotificationChannel(context);

        // Set badge on app launcher icon using ShortcutBadger
        // This works even when app is closed and supports multiple launchers
        try {
            ShortcutBadger.applyCount(context, count);
        } catch (Exception e) {
            // ShortcutBadger may fail on unsupported launchers, continue with notification
            e.printStackTrace();
        }

        // Check notification permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context,
                    android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, can't post notification but badge is already set
                return;
            }
        }

        // Create intent to open MainActivity when notification is tapped
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("selected_tab", 2); // Open cart tab
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String title = "Giỏ hàng của bạn";
        String text = count > 0
                ? String.format("Bạn có %d sản phẩm trong giỏ hàng", count)
                : "Giỏ hàng trống";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CART_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher) // Use app icon (should be adaptive)
                .setContentTitle(title)
                .setContentText(text)
                // Keep notification silent but visible so launcher can show numeric badge when
                // supported
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setNumber(count) // This sets the badge count in notification
                // Ask system to use a small badge icon type (some launchers respect this)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL);

        NotificationManagerCompat.from(context).notify(CART_NOTIFICATION_ID, builder.build());
    }

    /**
     * Clear cart notification and remove badge from app icon
     */
    public static void clearCartNotification(Context context) {
        NotificationManagerCompat.from(context).cancel(CART_NOTIFICATION_ID);

        // Remove badge from app launcher icon
        try {
            ShortcutBadger.removeCount(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
