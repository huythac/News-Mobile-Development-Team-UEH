package phivu.ueh.edu.vn.news_app.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Utility class để quản lý trạng thái bật/tắt thông báo
 */
public class NotificationPreferences {
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    
    // Default là true (bật thông báo)
    private static final boolean DEFAULT_ENABLED = true;

    /**
     * Lưu trạng thái bật/tắt thông báo
     */
    public static void setNotificationsEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).commit();
    }

    /**
     * Đọc trạng thái bật/tắt thông báo
     * @return true nếu thông báo được bật, false nếu tắt
     */
    public static boolean isNotificationsEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, DEFAULT_ENABLED);
    }
}

