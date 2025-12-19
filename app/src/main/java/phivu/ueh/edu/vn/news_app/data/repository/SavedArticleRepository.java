package phivu.ueh.edu.vn.news_app.data.repository;

import android.content.Context;
import android.util.Log; // Nhớ import Log

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

import phivu.ueh.edu.vn.news_app.data.local.saved.SavedArticleDAO;
import phivu.ueh.edu.vn.news_app.data.remote.SavedArticleFirebaseDAO;
import phivu.ueh.edu.vn.news_app.model.Article;

public class SavedArticleRepository {

    private final SavedArticleDAO local;
    private final SavedArticleFirebaseDAO remote;
    // private final FirebaseFirestore db; // Không cần biến này vì remote DAO đã lo rồi

    public SavedArticleRepository(Context ctx) {
        local = new SavedArticleDAO(ctx);
        remote = new SavedArticleFirebaseDAO();
        // db = ... (Xóa dòng này đi cho gọn)
    }

    // =========================
    // KIỂM TRA ĐÃ LƯU (Dùng cho Adapter đổi màu icon)
    // =========================
    public boolean isSavedLocal(String articleId) {
        return articleId != null && local.isSaved(articleId);
    }

    // =========================
    // LƯU BÀI VIẾT (Ghi cả 2 nơi)
    // =========================
    public void save(String userId, String articleId) {
        Log.e("REPO", "GỌI SAVE: User=" + userId + " | Article=" + articleId);

        if (userId == null || articleId == null) {
            Log.e("REPO", ">>> LỖI: Dữ liệu Null");
            return;
        }

        // 1. Lưu vào máy trước (Ưu tiên Offline)
        // Lưu ý: Đảm bảo bảng 'Article' đã có thông tin bài này, nếu không JOIN sẽ tạch
        local.saveArticle(articleId);

        // 2. Backup lên Cloud
        remote.save(userId, articleId);
    }

    // =========================
    // BỎ LƯU (Xóa cả 2 nơi)
    // =========================
    public void unsave(String userId, String articleId) {
        if (userId == null || articleId == null) return;

        local.unsaveArticle(articleId);
        remote.unsave(userId, articleId);
    }

    // =========================
    // LẤY DANH SÁCH HIỂN THỊ
    // =========================
    public List<Article> getSavedArticlesLocal() {
        // Hàm này sẽ thực hiện câu lệnh SELECT ... JOIN ...
        return local.getSavedArticles();
    }

    // =========================
    // ĐỒNG BỘ TỪ CLOUD VỀ MÁY (Dùng 1 hàm này thôi)
    // =========================
    public void syncFromRemote(String userId, Runnable onDone) {
        if (userId == null) {
            if (onDone != null) onDone.run();
            return;
        }

        // Lấy danh sách ID từ Firestore
        FirebaseFirestore.getInstance()
                .collection("user_saved_articles")
                .document(userId)
                .collection("articles")
                .get()
                .addOnSuccessListener(snapshot -> {
                    // Duyệt qua từng bài trên Cloud và lưu ID xuống máy
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String articleId = doc.getId(); // Lấy ID bài viết (là tên document)
                        if (articleId != null && !articleId.isEmpty()) {
                            // Lưu ID này vào bảng SavedArticle
                            local.saveArticle(articleId);
                        }
                    }
                    // Báo cho UI biết đã xong để reload lại list
                    if (onDone != null) onDone.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("REPO", "Lỗi Sync: " + e.getMessage());
                    if (onDone != null) onDone.run(); // Vẫn báo done để tắt loading
                });
    }
}