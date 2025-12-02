package phivu.ueh.edu.vn.news_app.data.local.user;

import android.content.Context;

import phivu.ueh.edu.vn.news_app.model.User;

public class UserRepository {
    private UserDAO dao;

    public UserRepository(Context context) {
        dao = new UserDAO(context);
    }

    public boolean register(String username, String email, String password, String fullName) {
        // validate, hash password here
        return dao.register(username, email, password, fullName);
    }

    public User login(String usernameOrEmail, String password) {
        // validate, hash password here (must match stored)
        return dao.login(usernameOrEmail, password);
    }

    public User getById(int id) { return dao.getById(id); }
}
