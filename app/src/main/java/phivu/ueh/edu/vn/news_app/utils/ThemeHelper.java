package phivu.ueh.edu.vn.news_app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import phivu.ueh.edu.vn.news_app.R;

public class ThemeHelper {
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";

    // Theme mode constants
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";

    /**
     * Lưu theme mode vào SharedPreferences
     */
    public static void saveThemeMode(Context context, String themeMode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Sử dụng commit() để đảm bảo lưu ngay lập tức (blocking)
        prefs.edit().putString(KEY_THEME_MODE, themeMode).commit();
    }

    /**
     * Đọc theme mode đã lưu, mặc định là "light"
     */
    public static String getSavedThemeMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_THEME_MODE, THEME_LIGHT);
    }

    /**
     * Áp dụng theme mode cho toàn bộ app
     * Light -> MODE_NIGHT_NO
     * Dark -> MODE_NIGHT_YES
     */
    public static void applyThemeMode(String themeMode) {
        int nightMode;
        if (THEME_DARK.equals(themeMode)) {
            nightMode = AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            nightMode = AppCompatDelegate.MODE_NIGHT_NO;
        }
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    /**
     * Áp dụng theme mode đã lưu khi khởi động app
     */
    public static void applySavedThemeMode(Context context) {
        String savedThemeMode = getSavedThemeMode(context);
        applyThemeMode(savedThemeMode);
    }

    /**
     * Lấy display name của theme mode (dùng cho hiển thị trong Settings)
     */
    public static String getThemeModeDisplayName(Context context, String themeMode) {
        if (THEME_DARK.equals(themeMode)) {
            return context.getString(R.string.theme_dark_display);
        } else {
            return context.getString(R.string.theme_light_display);
        }
    }
}

