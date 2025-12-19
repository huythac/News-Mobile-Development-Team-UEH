package phivu.ueh.edu.vn.news_app.data.remote;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class FollowCategoryFirebaseDAO {

    private final FirebaseFirestore db;

    public FollowCategoryFirebaseDAO() {
        db = FirebaseFirestore.getInstance();
    }

// File: FollowCategoryFirebaseDAO.java

    // =========================
    // FOLLOW (Sửa lại logic Nested Map)
    // =========================
    public void follow(String userId, String categoryId) {
        // 1. Tạo Map con chứa { "categoryId": true }
        Map<String, Object> childMap = new HashMap<>();
        childMap.put(categoryId, true);

        // 2. Tạo Map cha chứa { "categories": childMap }
        Map<String, Object> parentMap = new HashMap<>();
        parentMap.put("categories", childMap);

        // 3. Set với Merge -> Nó sẽ hòa trộn childMap vào field categories có sẵn
        db.collection("user_follows")
                .document(userId)
                .set(parentMap, SetOptions.merge());
    }

    // =========================
    // UNFOLLOW (Sửa lại logic Nested Map)
    // =========================
    public void unfollow(String userId, String categoryId) {
        // 1. Dùng FieldValue.delete() để xóa key con
        Map<String, Object> childMap = new HashMap<>();
        childMap.put(categoryId, FieldValue.delete());

        // 2. Map cha
        Map<String, Object> parentMap = new HashMap<>();
        parentMap.put("categories", childMap);

        // 3. Cập nhật
        db.collection("user_follows")
                .document(userId)
                .set(parentMap, SetOptions.merge());
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
                        // Log dữ liệu thô từ Firestore để debug
                        android.util.Log.d("FIREBASE_CHECK", "Data: " + doc.getData());

                        // Kiểm tra null an toàn hơn
                        Object rawData = doc.get("categories");
                        if (rawData instanceof Map) {
                            Map<String, Object> cats = (Map<String, Object>) rawData;
                            for (String key : cats.keySet()) {
                                result.put(key, true);
                            }
                        } else {
                            android.util.Log.e("FIREBASE_CHECK", "Field 'categories' is null or not a Map");
                        }
                    } else {
                        android.util.Log.e("FIREBASE_CHECK", "Document user_follows/" + userId + " does not exist");
                    }

                    listener.onLoaded(result);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("FIREBASE_CHECK", "Error: " + e.getMessage());
                    listener.onError(e.getMessage());
                });
    }

    public interface ValueListener {
        void onLoaded(HashMap<String, Boolean> map);
        void onError(String err);
    }
}
