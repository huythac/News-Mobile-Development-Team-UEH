package phivu.ueh.edu.vn.news_app.data.local.saved;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log; // Nhớ import cái này

import java.util.ArrayList;
import java.util.List;

import phivu.ueh.edu.vn.news_app.data.local.Database;
import phivu.ueh.edu.vn.news_app.model.Article;

public class SavedArticleDAO {

    private final Database dbHelper;
    private static final String TAG = "SavedArticleDAO"; // Tag để lọc log

    public SavedArticleDAO(Context ctx) {
        dbHelper = Database.getInstance(ctx);
    }

    // =========================
    // SAVE ARTICLE
    // =========================
    public void saveArticle(String articleId) {
        if (articleId == null || articleId.trim().isEmpty()) return;

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("articleId", articleId.trim());
            cv.put("savedAt", System.currentTimeMillis());

            long result = db.insertWithOnConflict(
                    "SavedArticle",
                    null,
                    cv,
                    SQLiteDatabase.CONFLICT_REPLACE
            );

            // Log kết quả
            if (result == -1) {
                Log.e(TAG, "Lỗi Insert: Có thể do khóa ngoại (Foreign Key) - Bài viết chưa có trong bảng Article");
            } else {
                Log.d(TAG, "Đã lưu vào SQLite thành công ID: " + articleId);
            }

        } catch (Exception e) {
            Log.e(TAG, "CRASH khi lưu bài: " + e.getMessage());
            e.printStackTrace(); // In lỗi ra để debug
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
    }

    // =========================
    // UNSAVE ARTICLE
    // =========================
    public void unsaveArticle(String articleId) {
        if (articleId == null || articleId.trim().isEmpty()) return;

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.delete("SavedArticle", "articleId=?", new String[]{articleId.trim()});
            Log.d(TAG, "Đã xóa khỏi SQLite ID: " + articleId);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi xóa bài: " + e.getMessage());
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
    }

    // =========================
    // CHECK IF ARTICLE IS SAVED
    // =========================
    public boolean isSaved(String articleId) {
        if (articleId == null || articleId.trim().isEmpty()) return false;

        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            db = dbHelper.getReadableDatabase();
            c = db.rawQuery(
                    "SELECT articleId FROM SavedArticle WHERE articleId=?",
                    new String[]{articleId.trim()}
            );
            boolean saved = c != null && c.moveToFirst();
            return saved;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi check isSaved: " + e.getMessage());
            return false;
        } finally {
            if (c != null) c.close();
            if (db != null && db.isOpen()) db.close();
        }
    }

    // =========================
    // GET SAVED ARTICLES (JOIN WITH Article)
    // =========================
    public List<Article> getSavedArticles() {
        List<Article> out = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor c = null;

        try {
            db = dbHelper.getReadableDatabase();
            // Join SavedArticle với Article để lấy đầy đủ thông tin
            // QUAN TRỌNG: Nếu bảng Article chưa có thông tin bài này, dòng này sẽ KHÔNG trả về kết quả
            String query = "SELECT a.id, a.title, a.image, a.content, a.description, " +
                    "a.authorId, a.authorName, a.categoryId, a.publishDate, " +
                    "sa.savedAt " +
                    "FROM SavedArticle sa " +
                    "INNER JOIN Article a ON sa.articleId = a.id " +
                    "ORDER BY sa.savedAt DESC";

            c = db.rawQuery(query, null);

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
            Log.d(TAG, "Đã load được " + out.size() + " bài viết đã lưu từ SQLite");

        } catch (Exception e) {
            Log.e(TAG, "Lỗi getSavedArticles: " + e.getMessage());
        } finally {
            if (c != null) c.close();
            if (db != null && db.isOpen()) db.close();
        }

        return out;
    }
}