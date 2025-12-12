package phivu.ueh.edu.vn.news_app.data.local.category;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;

public class FollowCategoryDAO {

    private SQLiteDatabase db;

    public FollowCategoryDAO(Context ctx) {
        db = new FollowCategoryLocalDataSource(ctx).getWritableDatabase();
    }

    public void follow(String userId, String categoryId) {
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("category_id", categoryId);
        db.insertWithOnConflict("followed_category", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void unfollow(String userId, String categoryId) {
        db.delete("followed_category", "user_id=? AND category_id=?", new String[]{userId, categoryId});
    }

    public HashMap<String, Boolean> getFollowed(String userId) {
        HashMap<String, Boolean> map = new HashMap<>();

        Cursor c = db.rawQuery(
                "SELECT category_id FROM followed_category WHERE user_id=?",
                new String[]{userId}
        );
        while (c.moveToNext()) {
            map.put(c.getString(0), true);
        }
        c.close();
        return map;
    }
}

