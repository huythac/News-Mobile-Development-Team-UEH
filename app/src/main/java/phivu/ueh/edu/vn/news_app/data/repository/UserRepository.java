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
    // ĐỒNG BỘ USER GOOGLE
    // =========================
    public void syncGoogleUser(FirebaseUser firebaseUser, Callback cb) {
        if (firebaseUser == null) {
            cb.onError("No Firebase User");
            return;
        }

        // 1. Kiểm tra xem user đã có trong Firestore chưa
        remote.getUser(firebaseUser.getUid(), new UserFirebaseDAO.SingleListener() {
            @Override
            public void onLoaded(User user) {
                // Đã có tài khoản -> Trả về user hiện tại (Giữ nguyên dữ liệu cũ)
                cb.onSuccess(user);
            }

            @Override
            public void onError(String err) {
                // 2. Chưa có tài khoản (Lỗi "User not found") -> Tạo mới từ info Google

                // Lấy link ảnh từ Google (nếu có)
                String photoUrl = "";
                if (firebaseUser.getPhotoUrl() != null) {
                    photoUrl = firebaseUser.getPhotoUrl().toString();
                }

                // Tạo User mới với ảnh từ Google
                User newUser = new User(
                        firebaseUser.getUid(),
                        firebaseUser.getDisplayName(),
                        firebaseUser.getEmail(),
                        "USER",
                        "",         // Bio rỗng
                        photoUrl    // Avatar từ Google
                );

                // Lưu lên Firestore
                remote.createUser(newUser);

                cb.onSuccess(newUser);
            }
        });
    }

    // Các hàm login/logout khác giữ nguyên...
    public FirebaseUser getCurrentFirebaseUser() { return auth.getCurrentUser(); }
    public void logout() { auth.logout(); }
}