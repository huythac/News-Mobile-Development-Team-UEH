package phivu.ueh.edu.vn.news_app.data.repository;

import com.google.firebase.auth.FirebaseUser;

import phivu.ueh.edu.vn.news_app.auth.AuthHelper;
import phivu.ueh.edu.vn.news_app.data.remote.UserFirebaseDAO;
import phivu.ueh.edu.vn.news_app.model.User;

public class UserRepository {

    private final AuthHelper auth;
    private final UserFirebaseDAO remote;

    public interface Callback {
        void onSuccess(User user);
        void onError(String err);
    }

    public UserRepository() {
        auth = new AuthHelper();
        remote = new UserFirebaseDAO();
    }

    // =========================
    // REGISTER
    // =========================
    public void register(String fullName, String email, String password, Callback cb) {
        auth.register(email, password, new AuthHelper.RegisterCallback() {

            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                User u = new User(
                        firebaseUser.getUid(),
                        fullName,
                        email,
                        "USER"
                );
                remote.createUser(u);
                cb.onSuccess(u);
            }

            @Override
            public void onError(String err) {
                cb.onError(err);
            }
        });
    }

    // =========================
    // LOGIN
    // =========================
    public void login(String email, String password, Callback cb) {
        auth.login(email, password, new AuthHelper.LoginCallback() {

            @Override
            public void onSuccess(FirebaseUser firebaseUser) {

                remote.getUser(firebaseUser.getUid(),
                        new UserFirebaseDAO.SingleListener() {

                            @Override
                            public void onLoaded(User user) {
                                cb.onSuccess(user);
                            }

                            @Override
                            public void onError(String err) {
                                cb.onError(err);
                            }
                        }
                );
            }

            @Override
            public void onError(String err) {
                cb.onError(err);
            }
        });
    }

    // =========================
    // AUTH HELPERS
    // =========================
    public FirebaseUser getCurrentFirebaseUser() {
        return auth.getCurrentUser();
    }

    public void logout() {
        auth.logout();
    }
}
