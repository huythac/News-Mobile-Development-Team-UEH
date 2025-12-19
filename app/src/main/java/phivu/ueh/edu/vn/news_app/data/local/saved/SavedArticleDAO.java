package phivu.ueh.edu.vn.news_app.data.local.saved;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import phivu.ueh.edu.vn.news_app.data.local.Database;
import phivu.ueh.edu.vn.news_app.model.Article;

public class SavedArticleDAO {

    private final Database dbHelper;

    public SavedArticleDAO(Context ctx) {
        dbHelper = Database.getInstance(ctx);
    }

    // =========================
    // SAVE ARTICLE
    // =========================
    public void saveArticle(String articleId) {
        if (articleId == null || articleId.trim().isEmpty()) {
            return;
        }

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("articleId", articleId.trim());
            cv.put("savedAt", System.currentTimeMillis());

            db.insertWithOnConflict(
                    "SavedArticle",
                    null,
                    cv,
                    SQLiteDatabase.CONFLICT_REPLACE
            );
        } catch (Exception e) {
            // Ignore database errors
        } finally {
            if (db != null) db.close();
        }
    }

    // =========================
    // UNSAVE ARTICLE
    // =========================
    public void unsaveArticle(String articleId) {
        if (articleId == null || articleId.trim().isEmpty()) {
            return;
        }

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.delete("SavedArticle", "articleId=?", new String[]{articleId.trim()});
        } catch (Exception e) {
            // Ignore database errors
        } finally {
            if (db != null) db.close();
        }
    }

    // =========================
    // CHECK IF ARTICLE IS SAVED
    // =========================
    public boolean isSaved(String articleId) {
        if (articleId == null || articleId.trim().isEmpty()) {
            return false;
        }

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
            return false;
        } finally {
            if (c != null) c.close();
            if (db != null) db.close();
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
            c = db.rawQuery(
                    "SELECT a.id, a.title, a.image, a.content, a.description, " +
                            "a.authorId, a.authorName, a.categoryId, a.publishDate, " +
                            "sa.savedAt " +
                            "FROM SavedArticle sa " +
                            "INNER JOIN Article a ON sa.articleId = a.id " +
                            "ORDER BY sa.savedAt DESC",
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
            // Return empty list on error
        } finally {
            if (c != null) c.close();
            if (db != null) db.close();
        }

        return out;
    }
}

