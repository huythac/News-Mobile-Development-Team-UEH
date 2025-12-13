package phivu.ueh.edu.vn.news_app.data.local.category;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FollowCategoryLocalDataSource extends SQLiteOpenHelper {

    private static final String DB_NAME = "follow_category.db";
    private static final int DB_VERSION = 1;

    public FollowCategoryLocalDataSource(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS followed_category (" +
                        "user_id TEXT, " +
                        "category_id TEXT, " +
                        "PRIMARY KEY (user_id, category_id))"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS followed_category");
        onCreate(db);
    }
}
