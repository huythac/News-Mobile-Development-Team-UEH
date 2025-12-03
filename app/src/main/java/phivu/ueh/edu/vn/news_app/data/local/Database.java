package phivu.ueh.edu.vn.news_app.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
    private static final String DB_NAME = "news.db";
    private static final int DB_VERSION = 2; // tăng khi thay đổi schema

    // ⭐ Singleton Instance
    private static Database instance;

    // ⭐ Hàm lấy instance dùng chung toàn app
    public static synchronized Database getInstance(Context context) {
        if (instance == null) {
            instance = new Database(context.getApplicationContext());
        }
        return instance;
    }

    // ⭐ Để constructor private ngăn tạo sai cách
    private Database(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Article (" +
                "id TEXT PRIMARY KEY, " +
                "title TEXT, " +
                "image TEXT, " +
                "content TEXT, " +
                "description TEXT, " +
                "viewed INTEGER DEFAULT 0, " +      // 0 = chưa xem, 1 = đã xem
                "lastSynced INTEGER DEFAULT 0)");   // epoch millis

        db.execSQL("CREATE TABLE Category (" +
                "id TEXT PRIMARY KEY, " +
                "name TEXT)");

        db.execSQL("CREATE TABLE User (" +
                "id TEXT PRIMARY KEY, " +
                "username TEXT," +
                "password TEXT," +
                "role TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        // simple upgrade strategy: drop & recreate
        db.execSQL("DROP TABLE IF EXISTS Article");
        db.execSQL("DROP TABLE IF EXISTS Category");
        db.execSQL("DROP TABLE IF EXISTS User");
        onCreate(db);
    }
}
