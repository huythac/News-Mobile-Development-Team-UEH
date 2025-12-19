package phivu.ueh.edu.vn.news_app.data.remote;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import phivu.ueh.edu.vn.news_app.model.User;

public class UserFirebaseDAO {

    private final FirebaseFirestore db;

    public interface SingleListener {
        void onLoaded(User user);
        void onError(String err);
    }

    public UserFirebaseDAO() {
        db = FirebaseFirestore.getInstance();
    }

    // =========================
    // CREATE USER (sau login)
    // =========================
    public void createUser(User u) {
        if (u == null || u.getId() == null) return;

        db.collection("users")
                .document(u.getId())
                .set(u);
    }

    // =========================
    // UPDATE USER
    // =========================
    public void updateUser(User u) {
        if (u == null || u.getId() == null) return;

        db.collection("users")
                .document(u.getId())
                .set(u);
    }

    // =========================
    // GET USER BY UID
    // =========================
    public void getUser(String uid, final SingleListener listener) {
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User u = doc.toObject(User.class);
                        if (u != null) {
                            u.setId(doc.getId());
                            listener.onLoaded(u);
                        } else {
                            listener.onError("Parse error");
                        }
                    } else {
                        listener.onError("User not found");
                    }
                })
                .addOnFailureListener(e ->
                        listener.onError(e.getMessage())
                );
    }
}
