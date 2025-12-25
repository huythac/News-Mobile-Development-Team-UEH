package phivu.ueh.edu.vn.news_app.data.repository;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

import phivu.ueh.edu.vn.news_app.data.local.saved.SavedArticleDAO;
import phivu.ueh.edu.vn.news_app.data.remote.SavedArticleFirebaseDAO;
import phivu.ueh.edu.vn.news_app.model.Article;

public class SavedArticleRepository {

    private final SavedArticleDAO local;
    private final SavedArticleFirebaseDAO remote;
    private final ArticleRepository articleRepo; // ✅ BỔ SUNG

    public SavedArticleRepository(Context ctx) {
        local = new SavedArticleDAO(ctx);
        remote = new SavedArticleFirebaseDAO();
        articleRepo = new ArticleRepository(ctx); // ✅ BỔ SUNG
    }

    // =========================
    // CHECK LOCAL STATE
    // =========================
    public boolean isSavedLocal(String articleId) {
        return articleId != null && local.isSaved(articleId);
    }

    // =========================
    // SAVE ARTICLE
    // =========================
    public void save(String userId, String articleId) {
        if (userId == null || articleId == null) return;

        // ⚠️ Article PHẢI tồn tại trong SQLite trước
        local.saveArticle(articleId);
        remote.save(userId, articleId);
    }

    // =========================
    // UNSAVE ARTICLE
    // =========================
    public void unsave(String userId, String articleId) {
        if (userId == null || articleId == null) return;

        local.unsaveArticle(articleId);
        remote.unsave(userId, articleId);
    }

    // =========================
    // LOAD LIST FROM SQLITE
    // =========================
    public List<Article> getSavedArticlesLocal() {
        return local.getSavedArticles();
    }

    // =========================
    // SYNC FROM FIREBASE → SQLITE (CHUẨN)
    // =========================
    public void syncFromRemote(String userId, Runnable onDone) {
        if (userId == null) {
            if (onDone != null) onDone.run();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("user_saved_articles")
                .document(userId)
                .collection("articles")
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        if (onDone != null) onDone.run();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : snapshot) {
                        String articleId = doc.getId();
                        if (articleId == null || articleId.isEmpty()) continue;

                        // ✅ BẮT BUỘC SYNC ARTICLE TRƯỚC
                        articleRepo.syncArticleFromRemote(articleId, new ArticleRepository.SingleCallback() {
                            @Override
                            public void onSuccess(Article article) {
                                local.saveArticle(articleId);
                            }

                            @Override
                            public void onError(String err) {
                                Log.e("SavedRepo", "Sync article failed: " + err);
                            }
                        });

                    }

                    if (onDone != null) onDone.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("SavedRepo", "Sync error: " + e.getMessage());
                    if (onDone != null) onDone.run();
                });
    }
}
