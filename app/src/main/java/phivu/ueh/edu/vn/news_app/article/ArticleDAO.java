package phivu.ueh.edu.vn.news_app.article;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import phivu.ueh.edu.vn.news_app.data.Database;

public class ArticleDAO {
    private Database dbHelper;

    public ArticleDAO(Context ctx) {
        dbHelper = Database.getInstance(ctx);
    }

    public long addArticle(String title, String description, String imageUrl, String content,
                           int mainCategoryId, int authorId, String status, String createdAt) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("description", description);
        cv.put("image_url", imageUrl);
        cv.put("content", content);
        cv.put("main_category_id", mainCategoryId);
        cv.put("author_id", authorId);
        cv.put("status", status);
        cv.put("created_at", createdAt);
        long id = db.insert("articles", null, cv);
        db.close();
        return id;
    }

    public boolean updateArticle(int id, String title, String description, String imageUrl, String content, int mainCategoryId, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title); cv.put("description", description);
        cv.put("image_url", imageUrl); cv.put("content", content);
        cv.put("main_category_id", mainCategoryId); cv.put("status", status);
        int rows = db.update("articles", cv, "id=?", new String[]{String.valueOf(id)});
        db.close(); return rows > 0;
    }

    public boolean deleteArticle(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete("articles", "id=?", new String[]{String.valueOf(id)});
        db.close(); return rows > 0;
    }

    public Article getById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id,title,description,image_url,content,main_category_id,author_id,status,created_at FROM articles WHERE id=?", new String[]{String.valueOf(id)});
        if (c.moveToFirst()) {
            Article a = new Article(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4),
                    c.getInt(5), c.getInt(6), c.getString(7), c.getString(8));
            c.close(); db.close(); return a;
        }
        c.close(); db.close(); return null;
    }

    public List<Article> getAll() {
        List<Article> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id,title,description,image_url,content,main_category_id,author_id,status,created_at FROM articles ORDER BY created_at DESC", null);
        if (c.moveToFirst()) {
            do {
                list.add(new Article(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4),
                        c.getInt(5), c.getInt(6), c.getString(7), c.getString(8)));
            } while (c.moveToNext());
        }
        c.close(); db.close(); return list;
    }

    public List<Article> getByCategory(int categoryId) {
        List<Article> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id,title,description,image_url,content,main_category_id,author_id,status,created_at FROM articles WHERE main_category_id=? ORDER BY created_at DESC", new String[]{String.valueOf(categoryId)});
        if (c.moveToFirst()) {
            do {
                list.add(new Article(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4),
                        c.getInt(5), c.getInt(6), c.getString(7), c.getString(8)));
            } while (c.moveToNext());
        }
        c.close(); db.close(); return list;
    }
}
