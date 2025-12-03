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
    private Database dbHelper;

    public CategoryDAO(Context ctx) {
        dbHelper = Database.getInstance(ctx);
    }

    public boolean addCategory(String name, String description) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("description", description);
        long id = db.insert("categories", null, cv);
        db.close();
        return id != -1;
    }

    public boolean deleteCategory(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete("categories", "id=?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }

    public List<Category> getAll() {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, name, description FROM categories", null);

        if (c.moveToFirst()) {
            do {
                list.add(new Category(
                        c.getString(0),   // ✔ id là String
                        c.getString(1),   // name
                        c.getString(2)    // description
                ));
            } while (c.moveToNext());
        }

        c.close();
        db.close();
        return list;
    }

}
