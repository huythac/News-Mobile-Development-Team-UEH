package phivu.ueh.edu.vn.news_app.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * Helper class để gửi notification với check permission và user preference
 * 
 * CÁCH SỬ DỤNG:
 * 
 * // Tạo notification builder
 * NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
 *     .setSmallIcon(R.drawable.ic_notification)
 *     .setContentTitle("Title")
 *     .setContentText("Message")
 *     .setPriority(NotificationCompat.PRIORITY_DEFAULT);
 * 
 * // Gửi notification (sẽ tự động check permission và user preference)
 * NotificationHelper.showNotification(context, NOTIFICATION_ID, builder);
 * 
 * HOẶC nếu bạn đã có NotificationManager:
 * 
 * if (NotificationHelper.canShowNotification(context)) {
 *     notificationManager.notify(NOTIFICATION_ID, notification);
 * }
 */
public class NotificationHelper {
    
    private static final String CHANNEL_ID = "news_app_channel";
    private static final String CHANNEL_NAME = "News App Notifications";
    
    /**
     * Kiểm tra xem có thể hiển thị notification không
     * (check cả permission và user preference)
     */
    public static boolean canShowNotification(Context context) {
        // 1. Check user preference
        if (!NotificationPreferences.isNotificationsEnabled(context)) {
            return false;
        }
        
        // 2. Check permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            return notificationManager.areNotificationsEnabled();
        }
        
        // 3. API < 33, notifications are enabled by default
        return true;
    }
    
    /**
     * Tạo notification channel (cần gọi một lần khi app khởi động)
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for news articles and updates");
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    /**
     * Hiển thị notification với auto-check permission và preference
     * 
     * @param context Context
     * @param notificationId Unique ID cho notification
     * @param builder NotificationCompat.Builder đã được setup
     */
    public static void showNotification(Context context, int notificationId, NotificationCompat.Builder builder) {
        if (!canShowNotification(context)) {
            // Không hiển thị notification nếu user đã tắt hoặc chưa có permission
            return;
        }
        
        // Đảm bảo channel đã được tạo
        createNotificationChannel(context);
        
        // Set channel ID nếu chưa có
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }
        
        // Gửi notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            // Permission might have been revoked
            // Log error but don't crash
            android.util.Log.e("NotificationHelper", "Failed to show notification: " + e.getMessage());
        }
    }
    
    /**
     * Hủy một notification cụ thể
     */
    public static void cancelNotification(Context context, int notificationId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(notificationId);
    }
    
    /**
     * Hủy tất cả notifications
     */
    public static void cancelAllNotifications(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancelAll();
    }
}

