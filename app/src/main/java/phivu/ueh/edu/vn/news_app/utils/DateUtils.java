package phivu.ueh.edu.vn.news_app.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    /**
     * Format publish date consistently across the app (used in both card and detail)
     * Format: "dd/MM/yyyy" (e.g., "28/11/2025")
     */
    public static String formatPublishDate(long timestamp) {
        if (timestamp <= 0) {
            return "";
        }
        try {
            // Hardening: Handle invalid timestamps
            if (timestamp < 0 || timestamp > Long.MAX_VALUE / 1000) {
                return "";
            }
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            return "";
        }
    }
}
