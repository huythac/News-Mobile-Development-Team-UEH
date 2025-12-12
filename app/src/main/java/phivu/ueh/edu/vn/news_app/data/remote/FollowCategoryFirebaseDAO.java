package phivu.ueh.edu.vn.news_app.data.remote;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class FollowCategoryFirebaseDAO {

    private DatabaseReference ref;

    public FollowCategoryFirebaseDAO() {
        ref = FirebaseDatabase.getInstance().getReference("user_follow/category");
    }

    // FOLLOW
    public void follow(String userId, String categoryId) {
        ref.child(userId).child(categoryId).setValue(true);
    }

    // UNFOLLOW
    public void unfollow(String userId, String categoryId) {
        ref.child(userId).child(categoryId).removeValue();
    }

    // GET FOLLOWED LIST
    public void getFollowed(String userId, ValueListener listener) {
        ref.child(userId).get().addOnSuccessListener(snapshot -> {

            HashMap<String, Boolean> result = new HashMap<>();

            if (snapshot.exists()) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    result.put(child.getKey(), true);
                }
            }

            listener.onLoaded(result);

        }).addOnFailureListener(e ->
                listener.onError(e.getMessage())
        );
    }

    public interface ValueListener {
        void onLoaded(HashMap<String, Boolean> map);
        void onError(String err);
    }
}
