package phivu.ueh.edu.vn.news_app.data.repository;

import android.content.Context;

import java.util.HashMap;

import phivu.ueh.edu.vn.news_app.data.local.category.FollowCategoryDAO;
import phivu.ueh.edu.vn.news_app.data.remote.FollowCategoryFirebaseDAO;

public class FollowCategoryRepository {

    private final FollowCategoryDAO local;
    private final FollowCategoryFirebaseDAO remote;

    public FollowCategoryRepository(Context ctx) {
        local = new FollowCategoryDAO(ctx);
        remote = new FollowCategoryFirebaseDAO();
    }

    public interface Listener {
        void onResult(HashMap<String, Boolean> map);
        void onError(String err);
    }

    // =========================
    // FOLLOW (NO CALLBACK) - dùng cho Adapter
    // =========================
    public void follow(String userId, String categoryId) {
        remote.follow(userId, categoryId);
        local.follow(userId, categoryId);
    }

    // =========================
    // UNFOLLOW (NO CALLBACK) - dùng cho Adapter
    // =========================
    public void unfollow(String userId, String categoryId) {
        remote.unfollow(userId, categoryId);
        local.unfollow(userId, categoryId);
    }


    // =========================
    // GET FOLLOW LIST
    // =========================
    public void getFollowed(String userId, Listener cb) {
        remote.getFollowed(userId, new FollowCategoryFirebaseDAO.ValueListener() {
            @Override
            public void onLoaded(HashMap<String, Boolean> remoteMap) {

                // clear local trước
                local.clearByUser(userId);

                // sync lại từ Firebase
                for (String id : remoteMap.keySet()) {
                    local.follow(userId, id);
                }

                cb.onResult(remoteMap);
            }

            @Override
            public void onError(String err) {
                cb.onResult(local.getFollowed(userId));
            }
        });
    }
}
