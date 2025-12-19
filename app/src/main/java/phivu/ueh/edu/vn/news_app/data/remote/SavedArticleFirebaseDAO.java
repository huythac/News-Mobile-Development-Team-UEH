package phivu.ueh.edu.vn.news_app.data.remote;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SavedArticleFirebaseDAO {

    private static final String TAG = "SavedArticleFirebase";
    private final FirebaseFirestore db;

    public SavedArticleFirebaseDAO() {
        db = FirebaseFirestore.getInstance();
    }

    // =========================
    // SAVE ARTICLE
    // =========================
    public void save(String userId, String articleId) {
        if (userId == null || articleId == null) {
            Log.w(TAG, "save: userId or articleId is null");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("articleId", articleId);
        data.put("savedAt", System.currentTimeMillis());

        db.collection("user_saved_articles")
                .document(userId)
                .collection("articles")
                .document(articleId)
                .set(data)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Saved article: " + articleId))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Save failed", e));
    }

    // =========================
    // UNSAVE ARTICLE
    // =========================
    public void unsave(String userId, String articleId) {
        if (userId == null || articleId == null) {
            Log.w(TAG, "unsave: userId or articleId is null");
            return;
        }

        db.collection("user_saved_articles")
                .document(userId)
                .collection("articles")
                .document(articleId)
                .delete()
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Unsaved article: " + articleId))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Unsave failed", e));
    }
}
