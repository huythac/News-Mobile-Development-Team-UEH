package phivu.ueh.edu.vn.news_app.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthHelper {
    private final FirebaseAuth auth;

    public AuthHelper() {
        auth = FirebaseAuth.getInstance();
    }

    public interface RegisterCallback {
        void onSuccess(FirebaseUser user);
        void onError(String err);
    }

    public interface LoginCallback {
        void onSuccess(FirebaseUser user);
        void onError(String err);
    }

    public void register(String email, String password, RegisterCallback cb) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) cb.onSuccess(auth.getCurrentUser());
                    else cb.onError(task.getException() == null ? "Unknown error" : task.getException().getMessage());
                });
    }

    public void login(String email, String password, LoginCallback cb) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) cb.onSuccess(auth.getCurrentUser());
                    else cb.onError(task.getException() == null ? "Unknown error" : task.getException().getMessage());
                });
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void logout() {
        auth.signOut();
    }
}
