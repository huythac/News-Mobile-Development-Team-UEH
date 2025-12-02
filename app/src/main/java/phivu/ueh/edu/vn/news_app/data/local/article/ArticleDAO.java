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

    private Database dbHelper;

    public ArticleDAO(Context ctx) {
        dbHelper = new Database(ctx);
    }

    public void clear() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("Article", null, null);
    }

    // INSERT or UPDATE (Firebase → SQLite)
    public void upsert(Article a, long lastSyncedMillis) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("id", a.getId());
        cv.put("title", a.getTitle());
        cv.put("image", a.getImage());
        cv.put("content", a.getContent());
        cv.put("description", a.getDescription());   // 🔥 ADD DESCRIPTION
        cv.put("lastSynced", lastSyncedMillis);

        db.insertWithOnConflict("Article", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // GET ALL ARTICLES (cached)
    public List<Article> getAll() {
        List<Article> out = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT id, title, image, content, description FROM Article ORDER BY lastSynced DESC",
                null
        );

        if (c.moveToFirst()) {
            do {
                out.add(new Article(
                        c.getString(0),   // id
                        c.getString(1),   // title
                        c.getString(2),   // image
                        c.getString(3),   // content
                        c.getString(4)    // description  🔥 ADD
                ));
            } while (c.moveToNext());
        }
        c.close();
        return out;
    }

    // GET ONE ARTICLE BY ID
    public Article getById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT id, title, image, content, description FROM Article WHERE id=?",
                new String[]{id}
        );

        if (c.moveToFirst()) {
            Article a = new Article(
                    c.getString(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4)    // 🔥 description
            );
            c.close();
            return a;
        }
        c.close();
        return null;
    }

    // MARK AS VIEWED (save offline)
    public void markViewed(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("viewed", 1);
        db.update("Article", cv, "id=?", new String[]{id});
    }

    // GET viewed cached articles
    public List<Article> getViewedArticles() {
        List<Article> out = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT id, title, image, content, description FROM Article WHERE viewed=1 ORDER BY lastSynced DESC",
                null
        );

        if (c.moveToFirst()) {
            do {
                out.add(new Article(
                        c.getString(0),
                        c.getString(1),
                        c.getString(2),
                        c.getString(3),
                        c.getString(4)    // 🔥 description
                ));
            } while (c.moveToNext());
        }
        c.close();
        return out;
    }
}
