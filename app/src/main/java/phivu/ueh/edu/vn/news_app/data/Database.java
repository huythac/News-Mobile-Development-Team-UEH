package phivu.ueh.edu.vn.news_app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ai_news.db";
    private static final int DATABASE_VERSION = 3; // tăng khi thay đổi schema

    private static Database instance;

    public static synchronized Database getInstance(Context context) {
        if (instance == null) {
            instance = new Database(context.getApplicationContext());
        }
        return instance;
    }

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // USERS
        db.execSQL("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE," +
                "email TEXT," +
                "password TEXT," +
                "full_name TEXT," +
                "role TEXT" +
                ");");

        // CATEGORIES
        db.execSQL("CREATE TABLE IF NOT EXISTS categories (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT UNIQUE," +
                "description TEXT" +
                ");");

        // ARTICLES
        db.execSQL("CREATE TABLE IF NOT EXISTS articles (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT," +
                "description TEXT," +
                "image_url TEXT," +
                "content TEXT," +
                "main_category_id INTEGER," +
                "author_id INTEGER," +
                "status TEXT," +
                "created_at TEXT," +
                "FOREIGN KEY(main_category_id) REFERENCES categories(id)," +
                "FOREIGN KEY(author_id) REFERENCES users(id)" +
                ");");

        // ARTICLE_CATEGORIES (many-to-many)
        db.execSQL("CREATE TABLE IF NOT EXISTS article_categories (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "article_id INTEGER," +
                "category_id INTEGER," +
                "FOREIGN KEY(article_id) REFERENCES articles(id)," +
                "FOREIGN KEY(category_id) REFERENCES categories(id)" +
                ");");

        // COMMENTS
        db.execSQL("CREATE TABLE IF NOT EXISTS comments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "article_id INTEGER," +
                "user_id INTEGER," +
                "content TEXT," +
                "status TEXT," +
                "created_at TEXT," +
                "FOREIGN KEY(article_id) REFERENCES articles(id)," +
                "FOREIGN KEY(user_id) REFERENCES users(id)" +
                ");");

        insertSeedData(db);
    }

    private void insertSeedData(SQLiteDatabase db) {
        // NOTE: seed data minimal; bạn có thể mở rộng
        db.execSQL("INSERT OR IGNORE INTO users (id,username,email,password,full_name,role) VALUES " +
                "(1,'admin','admin@ai-news.com','password123','Admin AI','admin');");

        db.execSQL("INSERT OR IGNORE INTO categories (id,name,description) VALUES " +
                "(1,'Nghiên cứu AI','Nghiên cứu và công bố')," +
                "(2,'Học máy','Thuật toán và mô hình');");

        db.execSQL("INSERT OR IGNORE INTO articles (id,title,description,image_url,content,main_category_id,author_id,status,created_at) VALUES " +
                "(1,'Bài mẫu 1','Mô tả ngắn','', 'Nội dung chi tiết bài 1',1,1,'published','2025-09-12');");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // đơn giản drop và tạo lại; production: viết migration cẩn thận
        db.execSQL("DROP TABLE IF EXISTS article_categories");
        db.execSQL("DROP TABLE IF EXISTS comments");
        db.execSQL("DROP TABLE IF EXISTS articles");
        db.execSQL("DROP TABLE IF EXISTS categories");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }
}
