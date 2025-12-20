package phivu.ueh.edu.vn.news_app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

import phivu.ueh.edu.vn.news_app.R;

public class LocaleHelper {
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_LOCALE = "locale_code";

    // Locale codes
    public static final String LOCALE_VI = "vi";
    public static final String LOCALE_EN = "en";

    /**
     * Lưu locale code vào SharedPreferences
     */
    public static void saveLocale(Context context, String localeCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Sử dụng commit() để đảm bảo lưu ngay lập tức (blocking)
        prefs.edit().putString(KEY_LOCALE, localeCode).commit();
    }

    /**
     * Đọc locale code đã lưu, mặc định là "vi"
     */
    public static String getSavedLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LOCALE, LOCALE_VI);
    }

    /**
     * Áp dụng locale cho toàn bộ app
     * Sử dụng AppCompatDelegate.setApplicationLocales (API 33+ với backward compatibility)
     */
    public static void applyLocale(String localeCode) {
        LocaleListCompat localeList = LocaleListCompat.forLanguageTags(localeCode);
        AppCompatDelegate.setApplicationLocales(localeList);
    }

    /**
     * Wrap context với locale đã lưu (dùng trong attachBaseContext)
     */
    public static Context wrapContext(Context context) {
        String savedLocale = getSavedLocale(context);
        Locale locale = getLocale(savedLocale);
        
        // Đặt locale mặc định
        Locale.setDefault(locale);
        
        Configuration configuration = context.getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
            Context wrappedContext = context.createConfigurationContext(configuration);
            return wrappedContext;
        } else {
            configuration.locale = locale;
            Resources resources = context.getResources();
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            return context;
        }
    }

    /**
     * Áp dụng locale đã lưu khi khởi động app
     */
    public static void applySavedLocale(Context context) {
        String savedLocale = getSavedLocale(context);
        applyLocale(savedLocale);
    }

    /**
     * Lấy Locale object từ locale code
     */
    public static Locale getLocale(String localeCode) {
        switch (localeCode) {
            case LOCALE_EN:
                return Locale.ENGLISH;
            case LOCALE_VI:
            default:
                return new Locale("vi");
        }
    }

    /**
     * Lấy display name của locale (dùng cho hiển thị trong Settings)
     * Format: "Tiếng Việt (Vietnamese)" hoặc "English (Tiếng Anh)"
     */
    public static String getLocaleDisplayName(Context context, String localeCode) {
        if (localeCode.equals(LOCALE_VI)) {
            return context.getString(R.string.language_vietnamese_display);
        } else {
            return context.getString(R.string.language_english_display);
        }
    }
}

