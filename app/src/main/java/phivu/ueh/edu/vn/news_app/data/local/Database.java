package phivu.ueh.edu.vn.news_app.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
    private static final String DB_NAME = "news.db";
    private static final int DB_VERSION = 2;

    private static Database instance;

    private Database(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static synchronized Database getInstance(Context ctx) {
        if (instance == null) instance = new Database(ctx.getApplicationContext());
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS Article (" +
                "id TEXT PRIMARY KEY, " +
                "title TEXT, " +
                "image TEXT, " +
                "content TEXT, " +
                "description TEXT, " +
                "viewed INTEGER DEFAULT 0, " +
                "lastSynced INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE IF NOT EXISTS Category (" +
                "id TEXT PRIMARY KEY, " +
                "name TEXT, " +
                "description TEXT)");
        // note: we DO NOT create User table in local—auth is via Firebase
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        // simple upgrade strategy: drop & recreate (adjust per need)
        db.execSQL("DROP TABLE IF EXISTS Article");
        db.execSQL("DROP TABLE IF EXISTS Category");
        onCreate(db);
    }
}
