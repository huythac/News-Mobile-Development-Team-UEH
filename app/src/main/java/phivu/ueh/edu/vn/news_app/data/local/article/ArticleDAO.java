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

    public void upsert(Article a, long lastSyncedMillis) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id", a.getId());
        cv.put("title", a.getTitle());
        cv.put("image", a.getImage());
        cv.put("content", a.getContent());
        cv.put("description", a.getDescription());
        cv.put("lastSynced", lastSyncedMillis);
        db.insertWithOnConflict("Article", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void markViewed(Article a) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("viewed", 1);
        cv.put("lastSynced", System.currentTimeMillis());
        int updated = db.update("Article", cv, "id=?", new String[]{a.getId()});
        if (updated == 0) {
            ContentValues all = new ContentValues();
            all.put("id", a.getId());
            all.put("title", a.getTitle());
            all.put("image", a.getImage());
            all.put("content", a.getContent());
            all.put("description", a.getDescription());
            all.put("viewed", 1);
            all.put("lastSynced", System.currentTimeMillis());
            db.insertWithOnConflict("Article", null, all, SQLiteDatabase.CONFLICT_REPLACE);
        }
        db.close();
    }

    public Article getById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, title, image, content, description FROM Article WHERE id=?", new String[]{id});
        if (c.moveToFirst()) {
            Article a = new Article(
                    c.getString(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4)
            );
            c.close(); db.close(); return a;
        }
        c.close(); db.close();
        return null;
    }

    public List<Article> getViewedArticles() {
        List<Article> out = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, title, image, content, description FROM Article WHERE viewed=1 ORDER BY lastSynced DESC", null);
        if (c.moveToFirst()) {
            do {
                out.add(new Article(
                        c.getString(0),
                        c.getString(1),
                        c.getString(2),
                        c.getString(3),
                        c.getString(4)
                ));
            } while (c.moveToNext());
        }
        c.close(); db.close();
        return out;
    }
}
