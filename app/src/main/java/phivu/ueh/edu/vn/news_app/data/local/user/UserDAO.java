package phivu.ueh.edu.vn.news_app.data.local.user;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import phivu.ueh.edu.vn.news_app.data.local.Database;
import phivu.ueh.edu.vn.news_app.model.User;

public class UserDAO {
    private Database dbHelper;

    public UserDAO(Context context) {
        this.dbHelper = Database.getInstance(context);
    }

    // Register
    public boolean register(String username, String email, String password, String fullName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // check username/email exists
        Cursor c = db.rawQuery("SELECT id FROM users WHERE username=? OR email=?", new String[]{username, email});
        if (c.moveToFirst()) {
            c.close();
            db.close();
            return false;
        }
        c.close();

        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("email", email);
        cv.put("password", password); // -> hash in real app
        cv.put("full_name", fullName);
        cv.put("role", "reader");

        long id = db.insert("users", null, cv);
        db.close();
        return id != -1;
    }

    // Login
    public User login(String usernameOrEmail, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT id, username, email, full_name, role FROM users WHERE (username=? OR email=?) AND password=?",
                new String[]{usernameOrEmail, usernameOrEmail, password}
        );

        if (c.moveToFirst()) {
            User u = new User(
                    c.getInt(0),    // id
                    c.getString(1), // username
                    c.getString(2), // email
                    c.getString(3), // full_name
                    c.getString(4)  // role
            );
            c.close();
            db.close();
            return u;
        }
        c.close();
        db.close();
        return null;
    }


    // get by id
    public User getById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT id, username, email, full_name, role FROM users WHERE id=?",
                new String[]{String.valueOf(id)}
        );

        if (c.moveToFirst()) {
            User u = new User(
                    c.getInt(0),    // id
                    c.getString(1), // username
                    c.getString(2), // email
                    c.getString(3), // full_name
                    c.getString(4)  // role
            );
            c.close();
            db.close();
            return u;
        }
        c.close();
        db.close();
        return null;
    }

}

