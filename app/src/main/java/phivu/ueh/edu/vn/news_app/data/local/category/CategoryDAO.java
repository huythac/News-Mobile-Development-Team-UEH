package phivu.ueh.edu.vn.news_app.data.local.category;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import phivu.ueh.edu.vn.news_app.data.local.Database;
import phivu.ueh.edu.vn.news_app.model.Category;

public class CategoryDAO {
    private final Database dbHelper;

    public CategoryDAO(Context ctx) {
        dbHelper = Database.getInstance(ctx);
    }

    public void upsert(Category c) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id", c.getId());
        cv.put("name", c.getName());
        cv.put("description", c.getDescription());
        db.insertWithOnConflict("Category", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public List<Category> getAll() {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, name, description FROM Category", null);
        if (c.moveToFirst()) {
            do {
                list.add(new Category(c.getString(0), c.getString(1), c.getString(2)));
            } while (c.moveToNext());
        }
        c.close(); db.close();
        return list;
    }

    public Category getById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, name, description FROM Category WHERE id=?", new String[]{id});
        if (c.moveToFirst()) {
            Category cat = new Category(c.getString(0), c.getString(1), c.getString(2));
            c.close(); db.close(); return cat;
        }
        c.close(); db.close();
        return null;
    }
}
