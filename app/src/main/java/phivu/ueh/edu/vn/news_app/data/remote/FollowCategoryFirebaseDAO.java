package phivu.ueh.edu.vn.news_app.data.remote;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FollowCategoryFirebaseDAO {

    private final FirebaseFirestore db;

    public FollowCategoryFirebaseDAO() {
        db = FirebaseFirestore.getInstance();
    }

    // =========================
    // FOLLOW CATEGORY
    // =========================
    public void follow(String userId, String categoryId) {
        Map<String, Object> update = new HashMap<>();
        update.put("categories." + categoryId, true);

        db.collection("user_follows")
                .document(userId)
                .set(update, com.google.firebase.firestore.SetOptions.merge());
    }

    // =========================
    // UNFOLLOW CATEGORY
    // =========================
    public void unfollow(String userId, String categoryId) {
        Map<String, Object> update = new HashMap<>();
        update.put("categories." + categoryId,
                com.google.firebase.firestore.FieldValue.delete());

        db.collection("user_follows")
                .document(userId)
                .update(update);
    }

    // =========================
    // GET FOLLOWED CATEGORIES
    // =========================
    @SuppressWarnings("unchecked")
    public void getFollowed(String userId, ValueListener listener) {
        db.collection("user_follows")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {

                    HashMap<String, Boolean> result = new HashMap<>();

                    if (doc.exists()) {
                        Map<String, Object> cats =
                                (Map<String, Object>) doc.get("categories");

                        if (cats != null) {
                            for (String key : cats.keySet()) {
                                result.put(key, true);
                            }
                        }
                    }

                    listener.onLoaded(result);

                })
                .addOnFailureListener(e ->
                        listener.onError(e.getMessage())
                );
    }

    public interface ValueListener {
        void onLoaded(HashMap<String, Boolean> map);
        void onError(String err);
    }
}
