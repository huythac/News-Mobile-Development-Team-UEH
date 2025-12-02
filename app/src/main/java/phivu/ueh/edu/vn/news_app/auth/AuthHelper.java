package phivu.ueh.edu.vn.news_app.auth;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

public class AuthHelper {

    private FirebaseAuth auth;

    public AuthHelper() {
        auth = FirebaseAuth.getInstance();
    }

    // sign in with Google idToken obtained from GoogleSignInClient (UI)
    public void signInWithGoogleIdToken(String idToken, final AuthCallback cb) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        cb.onSuccess(user);
                    } else {
                        cb.onError(task.getException() != null ? task.getException().getMessage() : "Auth failed");
                    }
                });
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(String err);
    }

    // sign out
    public void signOut() {
        auth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }
}
