package phivu.ueh.edu.vn.news_app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchHistoryManager {
    private static final String PREF_NAME = "NewsAppPrefs";
    private static final String KEY_HISTORY = "SearchHistory";
    private final SharedPreferences prefs;

    public SearchHistoryManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Lấy danh sách lịch sử
    public List<String> getHistory() {
        String historyStr = prefs.getString(KEY_HISTORY, "");
        if (historyStr.isEmpty()) {
            return new ArrayList<>();
        }
        // Tách chuỗi bằng dấu phẩy
        return new ArrayList<>(Arrays.asList(historyStr.split(",")));
    }

    // Lưu từ khóa mới
    public void addHistory(String keyword) {
        List<String> currentHistory = getHistory();

        // Nếu đã có rồi thì xóa cái cũ để đưa lên đầu
        if (currentHistory.contains(keyword)) {
            currentHistory.remove(keyword);
        }

        // Thêm vào đầu danh sách
        currentHistory.add(0, keyword);

        // Giới hạn lưu 10 cái gần nhất
        if (currentHistory.size() > 10) {
            currentHistory = currentHistory.subList(0, 10);
        }

        // Lưu lại thành chuỗi
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < currentHistory.size(); i++) {
            sb.append(currentHistory.get(i));
            if (i < currentHistory.size() - 1) sb.append(",");
        }
        prefs.edit().putString(KEY_HISTORY, sb.toString()).apply();
    }

    // Xóa lịch sử (nếu cần)
    public void clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply();
    }
}