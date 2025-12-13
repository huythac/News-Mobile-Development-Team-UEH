package phivu.ueh.edu.vn.news_app.data.remote;

import android.content.Context;
import android.util.Log;

import java.util.List;

import phivu.ueh.edu.vn.news_app.data.local.category.CategoryDAO;
import phivu.ueh.edu.vn.news_app.data.local.article.ArticleDAO;
import phivu.ueh.edu.vn.news_app.model.Category;
import phivu.ueh.edu.vn.news_app.model.Article;

/**
 * Lightweight sync helper.
 * - By default we sync categories to local (so filtering works offline)
 * - Articles are viewer-based cached; force sync all articles is provided for admin use
 */
public class SyncService {
    private static final String TAG = "SyncService";

    private final CategoryFirebaseDAO categoryRemote;
    private final ArticleFirebaseDAO articleRemote;
    private final CategoryDAO categoryLocal;
    private final ArticleDAO articleLocal;

    public SyncService(Context ctx) {
        categoryRemote = new CategoryFirebaseDAO();
        articleRemote = new ArticleFirebaseDAO();
        categoryLocal = new CategoryDAO(ctx);
        articleLocal = new ArticleDAO(ctx);
    }

    public void syncCategories() {
        categoryRemote.fetchAll(new CategoryFirebaseDAO.ListListener() {
            @Override public void onLoaded(List<Category> list) {
                for (Category c : list) categoryLocal.upsert(c);
                Log.d(TAG, "Categories synced: " + list.size());
            }
            @Override public void onError(String err) { Log.w(TAG, "Category sync failed: " + err); }
        });
    }

    // admin-only: force copy all articles to local
    public void syncAllArticlesToLocal() {
        articleRemote.fetchAll(new ArticleFirebaseDAO.ListListener() {
            @Override public void onLoaded(List<Article> list) {
                long now = System.currentTimeMillis();
                for (Article a : list) articleLocal.upsert(a, now);
                Log.d(TAG, "Articles force-synced: " + list.size());
            }
            @Override public void onError(String err) { Log.w(TAG, "Article force sync failed: " + err); }
        });
    }
}
