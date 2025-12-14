package phivu.ueh.edu.vn.news_app.data.local.article;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import phivu.ueh.edu.vn.news_app.data.local.Database;
import phivu.ueh.edu.vn.news_app.model.Article;

public class ArticleDAO {

    private final Database dbHelper;

    public ArticleDAO(Context ctx) {
        dbHelper = Database.getInstance(ctx);
    }

    // =========================
    // UPSERT (SYNC FROM FIRESTORE)
    // =========================
    public void upsert(Article a, long lastSyncedMillis) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("id", a.getId());
        cv.put("title", a.getTitle());
        cv.put("image", a.getImage());
        cv.put("content", a.getContent());
        cv.put("description", a.getDescription());

        // 🔹 NEW FIELDS
        cv.put("authorId", a.getAuthorId());
        cv.put("authorName", a.getAuthorName());

        cv.put("lastSynced", lastSyncedMillis);

        db.insertWithOnConflict(
                "Article",
                null,
                cv,
                SQLiteDatabase.CONFLICT_REPLACE
        );
        db.close();
    }

    // =========================
    // MARK VIEWED
    // =========================
    public void markViewed(Article a) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("viewed", 1);
        cv.put("lastSynced", System.currentTimeMillis());

        int updated = db.update(
                "Article",
                cv,
                "id=?",
                new String[]{a.getId()}
        );

        if (updated == 0) {
            ContentValues all = new ContentValues();
            all.put("id", a.getId());
            all.put("title", a.getTitle());
            all.put("image", a.getImage());
            all.put("content", a.getContent());
            all.put("description", a.getDescription());

            // 🔹 NEW FIELDS
            all.put("authorId", a.getAuthorId());
            all.put("authorName", a.getAuthorName());

            all.put("viewed", 1);
            all.put("lastSynced", System.currentTimeMillis());

            db.insertWithOnConflict(
                    "Article",
                    null,
                    all,
                    SQLiteDatabase.CONFLICT_REPLACE
            );
        }
        db.close();
    }

    // =========================
    // GET BY ID (OFFLINE)
    // =========================
    public Article getById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT id, title, image, content, description, authorId, authorName " +
                        "FROM Article WHERE id=?",
                new String[]{id}
        );

        if (c.moveToFirst()) {
            Article a = new Article();
            a.setId(c.getString(0));
            a.setTitle(c.getString(1));
            a.setImage(c.getString(2));
            a.setContent(c.getString(3));
            a.setDescription(c.getString(4));
            a.setAuthorId(c.getString(5));
            a.setAuthorName(c.getString(6));


            // 🔹 SET AUTHOR
            a.setAuthorId(c.getString(5));
            a.setAuthorName(c.getString(6));

            c.close();
            db.close();
            return a;
        }

        c.close();
        db.close();
        return null;
    }

    // =========================
    // GET VIEWED ARTICLES
    // =========================
    public List<Article> getViewedArticles() {
        List<Article> out = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT id, title, image, content, description, authorId, authorName " +
                        "FROM Article WHERE viewed=1 ORDER BY lastSynced DESC",
                null
        );

        if (c.moveToFirst()) {
            do {
                Article a = new Article();
                a.setId(c.getString(0));
                a.setTitle(c.getString(1));
                a.setImage(c.getString(2));
                a.setContent(c.getString(3));
                a.setDescription(c.getString(4));
                a.setAuthorId(c.getString(5));
                a.setAuthorName(c.getString(6));

                // 🔹 SET AUTHOR
                a.setAuthorId(c.getString(5));
                a.setAuthorName(c.getString(6));

                out.add(a);

            } while (c.moveToNext());
        }

        c.close();
        db.close();
        return out;
    }
}
