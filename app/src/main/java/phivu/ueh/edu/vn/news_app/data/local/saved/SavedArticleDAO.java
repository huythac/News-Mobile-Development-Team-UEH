package phivu.ueh.edu.vn.news_app.data.local.saved;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import phivu.ueh.edu.vn.news_app.data.local.Database;
import phivu.ueh.edu.vn.news_app.model.Article;

public class SavedArticleDAO {

    private static final String TAG = "SavedArticleDAO";
    private final Database dbHelper;

    public SavedArticleDAO(Context ctx) {
        dbHelper = Database.getInstance(ctx);
    }

    // =========================
    // SAVE ARTICLE ID
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

            if (result == -1) {
                Log.e(TAG, "Insert failed (Article chưa tồn tại?) id=" + articleId);
            } else {
                Log.d(TAG, "SavedArticle inserted id=" + articleId);
            }

        } catch (Exception e) {
            Log.e(TAG, "saveArticle error: " + e.getMessage(), e);
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
            Log.d(TAG, "Unsaved article id=" + articleId);
        } catch (Exception e) {
            Log.e(TAG, "unsaveArticle error: " + e.getMessage(), e);
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
    }

    // =========================
    // CHECK SAVED STATE
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
            return c != null && c.moveToFirst();
        } catch (Exception e) {
            Log.e(TAG, "isSaved error: " + e.getMessage(), e);
            return false;
        } finally {
            if (c != null) c.close();
            if (db != null && db.isOpen()) db.close();
        }
    }

    // =========================
    // GET SAVED ARTICLES (JOIN)
    // =========================
    public List<Article> getSavedArticles() {
        List<Article> out = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor c = null;

        try {
            db = dbHelper.getReadableDatabase();

            /*
             * CHỈ hiển thị khi:
             * - SavedArticle.articleId tồn tại
             * - Article.id tồn tại trong SQLite
             * => đảm bảo dữ liệu đầy đủ & an toàn UI
             */
            String query =
                    "SELECT a.id, a.title, a.image, a.content, a.description, " +
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

            Log.d(TAG, "Loaded saved articles = " + out.size());

        } catch (Exception e) {
            Log.e(TAG, "getSavedArticles error: " + e.getMessage(), e);
        } finally {
            if (c != null) c.close();
            if (db != null && db.isOpen()) db.close();
        }

        return out;
    }
}
