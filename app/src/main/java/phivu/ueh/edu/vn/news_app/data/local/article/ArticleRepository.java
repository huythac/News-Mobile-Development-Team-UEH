package phivu.ueh.edu.vn.news_app.data.repo;

import android.content.Context;

import java.util.List;

import phivu.ueh.edu.vn.news_app.data.local.article.ArticleDAO;
import phivu.ueh.edu.vn.news_app.data.remote.ArticleFirebaseDAO;
import phivu.ueh.edu.vn.news_app.model.Article;

/**
 * Repository cung cấp: getList, getArticle, markViewed, forceSync
 * Luồng:
 * - getList(): cố gắng lấy từ Firebase (nếu online) và cập nhật SQLite; nếu lỗi -> trả về cache SQLite
 * - getArticle(id): cố gắng fetch Firebase by id, nếu thành công lưu vào SQLite và trả về; nếu lỗi -> trả về cache SQLite nếu có
 * - markViewed(id): đánh dấu viewed trong SQLite (khi xem xong)
 */
public class ArticleRepository {

    private ArticleDAO local;
    private ArticleFirebaseDAO remote;

    public interface ListCallback { void onSuccess(List<Article> list); void onError(String err); }
    public interface SingleCallback { void onSuccess(Article a); void onError(String err); }

    public ArticleRepository(Context ctx) {
        local = new ArticleDAO(ctx);
        remote = new ArticleFirebaseDAO();
    }

    // get list with fallback
    public void getList(final ListCallback cb) {
        remote.fetchAll(new ArticleFirebaseDAO.Listener() {
            @Override
            public void onLoaded(List<Article> list) {
                // update local cache with lastSynced = now
                long now = System.currentTimeMillis();
                for (Article a : list) {
                    local.upsert(a, now);
                }
                cb.onSuccess(list);
            }
            @Override
            public void onError(String err) {
                // fallback to local cache
                List<Article> cached = local.getAll();
                if (cached.isEmpty()) cb.onError(err);
                else cb.onSuccess(cached);
            }
        });
    }

    // get single article with offline fallback
    public void getArticle(final String id, final SingleCallback cb) {
        remote.fetchById(id, new ArticleFirebaseDAO.SingleListener() {
            @Override
            public void onLoaded(Article article) {
                // cache locally and return
                local.upsert(article, System.currentTimeMillis());
                cb.onSuccess(article);
            }

            @Override
            public void onError(String err) {
                // fallback local
                Article cached = local.getById(id);
                if (cached != null) cb.onSuccess(cached);
                else cb.onError(err);
            }
        });
    }

    // mark article as viewed locally (call when user opens article successfully)
    public void markViewed(String id) {
        local.markViewed(id);
    }

    // Forcing a full sync from remote -> local
    public void forceSyncFromRemote() {
        remote.fetchAll(new ArticleFirebaseDAO.Listener() {
            @Override public void onLoaded(List<Article> list) {
                long now = System.currentTimeMillis();
                for (Article a : list) local.upsert(a, now);
            }
            @Override public void onError(String err) { /* log */ }
        });
    }

    // get viewed cached articles
    public List<Article> getViewedCached() {
        return local.getViewedArticles();
    }
}
