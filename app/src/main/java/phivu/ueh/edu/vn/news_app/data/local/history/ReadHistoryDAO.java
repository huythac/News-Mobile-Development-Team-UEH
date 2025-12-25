//package phivu.ueh.edu.vn.news_app.data.local.history;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import phivu.ueh.edu.vn.news_app.data.local.Database;
//import phivu.ueh.edu.vn.news_app.model.Article;
//
//public class ReadHistoryDAO {
//
//    private final Database dbHelper;
//
//    public ReadHistoryDAO(Context ctx) {
//        dbHelper = Database.getInstance(ctx);
//    }
//
//    // =========================
//    // RECORD READ HISTORY
//    // =========================
//    public void recordRead(String articleId) {
//        if (articleId == null || articleId.trim().isEmpty()) {
//            return;
//        }
//
//        SQLiteDatabase db = null;
//        try {
//            db = dbHelper.getWritableDatabase();
//            ContentValues cv = new ContentValues();
//            cv.put("articleId", articleId.trim());
//            cv.put("readAt", System.currentTimeMillis());
//
//            db.insertWithOnConflict(
//                    "ReadHistory",
//                    null,
//                    cv,
//                    SQLiteDatabase.CONFLICT_REPLACE
//            );
//        } catch (Exception e) {
//            // Ignore database errors
//        } finally {
//            if (db != null) db.close();
//        }
//    }
//
//    // =========================
//    // DELETE FROM READ HISTORY
//    // =========================
//    public void deleteFromHistory(String articleId) {
//        if (articleId == null || articleId.trim().isEmpty()) {
//            return;
//        }
//
//        SQLiteDatabase db = null;
//        try {
//            db = dbHelper.getWritableDatabase();
//            db.delete("ReadHistory", "articleId=?", new String[]{articleId.trim()});
//        } catch (Exception e) {
//            // Ignore database errors
//        } finally {
//            if (db != null) db.close();
//        }
//    }
//
//    // =========================
//    // GET READ HISTORY (JOIN WITH Article)
//    // =========================
//    public List<Article> getReadHistory() {
//        List<Article> out = new ArrayList<>();
//        SQLiteDatabase db = null;
//        Cursor c = null;
//
//        try {
//            db = dbHelper.getReadableDatabase();
//            // Join ReadHistory với Article để lấy đầy đủ thông tin
//            c = db.rawQuery(
//                    "SELECT a.id, a.title, a.image, a.content, a.description, " +
//                            "a.authorId, a.authorName, a.categoryId, a.publishDate, " +
//                            "rh.readAt " +
//                            "FROM ReadHistory rh " +
//                            "INNER JOIN Article a ON rh.articleId = a.id " +
//                            "ORDER BY rh.readAt DESC",
//                    null
//            );
//
//            if (c != null && c.moveToFirst()) {
//                do {
//                    Article a = new Article();
//                    a.setId(c.getString(0));
//                    a.setTitle(c.getString(1));
//                    a.setImage(c.getString(2));
//                    a.setContent(c.getString(3));
//                    a.setDescription(c.getString(4));
//                    a.setAuthorId(c.getString(5));
//                    a.setAuthorName(c.getString(6));
//                    a.setCategoryId(c.getString(7));
//                    a.setPublishDate(c.getLong(8));
//
//                    out.add(a);
//                } while (c.moveToNext());
//            }
//        } catch (Exception e) {
//            // Return empty list on error
//        } finally {
//            if (c != null) c.close();
//            if (db != null) db.close();
//        }
//
//        return out;
//    }
//
//    // =========================
//    // CHECK IF ARTICLE IS IN HISTORY
//    // =========================
//    public boolean isInHistory(String articleId) {
//        if (articleId == null || articleId.trim().isEmpty()) {
//            return false;
//        }
//
//        SQLiteDatabase db = null;
//        Cursor c = null;
//        try {
//            db = dbHelper.getReadableDatabase();
//            c = db.rawQuery(
//                    "SELECT articleId FROM ReadHistory WHERE articleId=?",
//                    new String[]{articleId.trim()}
//            );
//
//            boolean inHistory = c != null && c.moveToFirst();
//            return inHistory;
//        } catch (Exception e) {
//            return false;
//        } finally {
//            if (c != null) c.close();
//            if (db != null) db.close();
//        }
//    }
//}
//


package phivu.ueh.edu.vn.news_app.data.local.history;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import phivu.ueh.edu.vn.news_app.data.local.Database;
import phivu.ueh.edu.vn.news_app.model.Article;

public class ReadHistoryDAO {

    private final Database dbHelper;
    private static final String TAG = "ReadHistoryDAO";

    public ReadHistoryDAO(Context ctx) {
        dbHelper = Database.getInstance(ctx);
    }

    // =========================
    // 1. GHI LẠI LỊCH SỬ
    // =======================================================
    public void addToHistory(String articleId) {
        if (articleId == null || articleId.trim().isEmpty()) return;

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("articleId", articleId.trim());
            cv.put("readAt", System.currentTimeMillis());

            // Conflict Algorithm: REPLACE
            // Nếu đọc lại bài cũ -> Cập nhật thời gian mới nhất lên đầu
            db.insertWithOnConflict(
                    "ReadHistory",
                    null,
                    cv,
                    SQLiteDatabase.CONFLICT_REPLACE
            );
            Log.d(TAG, "Đã lưu lịch sử: " + articleId);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi lưu lịch sử: " + e.getMessage()); // In lỗi ra để biết đường sửa
        } finally {
            if (db != null) db.close();
        }
    }

    // =========================
    // 2. XÓA KHỎI LỊCH SỬ
    // ================================================================
    public void deleteHistory(String articleId) {
        if (articleId == null || articleId.trim().isEmpty()) return;

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.delete("ReadHistory", "articleId=?", new String[]{articleId.trim()});
            Log.d(TAG, "Đã xóa lịch sử bài: " + articleId);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi xóa lịch sử: " + e.getMessage());
        } finally {
            if (db != null) db.close();
        }
    }

    // =========================
    // 3. LẤY DANH SÁCH HIỂN THỊ
    // =========================
    public List<Article> getReadHistory() {
        List<Article> out = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor c = null;

        try {
            db = dbHelper.getReadableDatabase();
            // Join ReadHistory với Article để lấy đầy đủ thông tin
            c = db.rawQuery(
                    "SELECT a.id, a.title, a.image, a.content, a.description, " +
                            "a.authorId, a.authorName, a.categoryId, a.publishDate, " +
                            "rh.readAt " +
                            "FROM ReadHistory rh " +
                            "INNER JOIN Article a ON rh.articleId = a.id " +
                            "ORDER BY rh.readAt DESC", // Sắp xếp bài mới đọc lên đầu
                    null
            );

            if (c != null && c.moveToFirst()) {
                do {
                    Article a = new Article();
                    a.setId(c.getString(0));
                    a.setTitle(c.getString(1));
                    a.setImage(c.getString(2));
                    a.setContent(c.getString(3));
                    a.setDescription(c.getString(4));
                    a.setAuthorId(c.getString(5));
                    a.setAuthorName(c.getString(6));
                    a.setCategoryId(c.getString(7));
                    a.setPublishDate(c.getLong(8));

                    out.add(a);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi lấy danh sách lịch sử: " + e.getMessage());
        } finally {
            if (c != null) c.close();
            if (db != null) db.close();
        }

        return out;
    }

    // =========================
    // CHECK CÓ TRONG LỊCH SỬ KHÔNG
    // =========================
    public boolean isInHistory(String articleId) {
        if (articleId == null || articleId.trim().isEmpty()) return false;

        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            db = dbHelper.getReadableDatabase();
            c = db.rawQuery(
                    "SELECT articleId FROM ReadHistory WHERE articleId=?",
                    new String[]{articleId.trim()}
            );
            return c != null && c.moveToFirst();
        } catch (Exception e) {
            return false;
        } finally {
            if (c != null) c.close();
            if (db != null) db.close();
        }
    }
}
