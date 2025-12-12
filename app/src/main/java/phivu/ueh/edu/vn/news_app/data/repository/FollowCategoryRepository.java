package phivu.ueh.edu.vn.news_app.data.repository;

import android.content.Context;

import java.util.HashMap;

import phivu.ueh.edu.vn.news_app.data.local.category.FollowCategoryDAO;
import phivu.ueh.edu.vn.news_app.data.remote.FollowCategoryFirebaseDAO;

public class FollowCategoryRepository {

    private FollowCategoryDAO local;
    private FollowCategoryFirebaseDAO remote;

    public FollowCategoryRepository(Context ctx) {
        local = new FollowCategoryDAO(ctx);
        remote = new FollowCategoryFirebaseDAO();
    }

    public interface Listener {
        void onResult(HashMap<String, Boolean> map);
        void onError(String err);
    }

    // FOLLOW
    public void follow(String userId, String categoryId) {
        remote.follow(userId, categoryId);
        local.follow(userId, categoryId);
    }

    // UNFOLLOW
    public void unfollow(String userId, String categoryId) {
        remote.unfollow(userId, categoryId);
        local.unfollow(userId, categoryId);
    }

    // GET FOLLOW LIST (PRIMARY = FIREBASE, FALLBACK = LOCAL)
    public void getFollowed(String userId, Listener cb) {
        remote.getFollowed(userId, new FollowCategoryFirebaseDAO.ValueListener() {
            @Override
            public void onLoaded(HashMap<String, Boolean> remoteMap) {
                // Lưu xuống local
                for (String id : remoteMap.keySet()) {
                    local.follow(userId, id);
                }
                cb.onResult(remoteMap);
            }

            @Override
            public void onError(String err) {
                // lỗi Firebase → dùng local
                cb.onResult(local.getFollowed(userId));
            }
        });
    }
}
