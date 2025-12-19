package phivu.ueh.edu.vn.news_app.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
    private static final String DB_NAME = "news.db";

    // ⚠️ QUAN TRỌNG: Tăng version lên 8 để thêm bảng SavedArticle và ReadHistory
    private static final int DB_VERSION = 8;

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
        //  bảng Article với ĐẦY ĐỦ các cột
        db.execSQL("CREATE TABLE IF NOT EXISTS Article (" +
                "id TEXT PRIMARY KEY, " +
                "title TEXT, " +
                "image TEXT, " +
                "content TEXT, " +
                "description TEXT, " +
                "authorId TEXT, " +
                "authorName TEXT, " +
                "categoryId TEXT, " +
                "publishDate INTEGER, " +
                "viewed INTEGER DEFAULT 0, " +
                "saved INTEGER DEFAULT 0, " +
                "lastSynced INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE IF NOT EXISTS Category (" +
                "id TEXT PRIMARY KEY, " +
                "name TEXT, " +
                "description TEXT)");

        // Bảng SavedArticle: Quản lý các bài viết đã lưu (để đọc offline)
        db.execSQL("CREATE TABLE IF NOT EXISTS SavedArticle (" +
                "articleId TEXT PRIMARY KEY, " +
                "savedAt INTEGER NOT NULL)");

        // Bảng ReadHistory: Quản lý lịch sử đọc bài viết
        db.execSQL("CREATE TABLE IF NOT EXISTS ReadHistory (" +
                "articleId TEXT PRIMARY KEY, " +
                "readAt INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        if (oldV < 8) {
            // Tạo bảng mới nếu chưa có (không làm mất dữ liệu)
            db.execSQL("CREATE TABLE IF NOT EXISTS SavedArticle (" +
                    "articleId TEXT PRIMARY KEY, " +
                    "savedAt INTEGER NOT NULL)");
            
            db.execSQL("CREATE TABLE IF NOT EXISTS ReadHistory (" +
                    "articleId TEXT PRIMARY KEY, " +
                    "readAt INTEGER NOT NULL)");
            
            // Migrate dữ liệu từ field `saved` sang bảng SavedArticle (nếu có)
            try {
                db.execSQL("INSERT OR IGNORE INTO SavedArticle (articleId, savedAt) " +
                        "SELECT id, lastSynced FROM Article WHERE saved=1");
            } catch (Exception e) {
                // Ignore migration errors
            }
        }
    }
}

