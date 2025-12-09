package phivu.ueh.edu.vn.news_app.data.repository;

import android.content.Context;

import java.util.List;

import phivu.ueh.edu.vn.news_app.data.local.article.ArticleDAO;
import phivu.ueh.edu.vn.news_app.data.remote.ArticleFirebaseDAO;
import phivu.ueh.edu.vn.news_app.model.Article;

public class ArticleRepository {
    private final ArticleDAO local;
    private final ArticleFirebaseDAO remote;

    public interface ListCallback { void onSuccess(List<Article> list); void onError(String err); }
    public interface SingleCallback { void onSuccess(Article a); void onError(String err); }

    public ArticleRepository(Context ctx) {
        local = new ArticleDAO(ctx);
        remote = new ArticleFirebaseDAO();
    }

    public void getList(final ListCallback cb) {
        remote.fetchAll(new ArticleFirebaseDAO.ListListener() {
            @Override public void onLoaded(List<Article> list) { cb.onSuccess(list); }
            @Override public void onError(String err) {
                List<Article> cached = local.getViewedArticles();
                if (cached.isEmpty()) cb.onError(err); else cb.onSuccess(cached);
            }
        });
    }

    public void getArticle(final String id, final SingleCallback cb) {
        remote.fetchById(id, new ArticleFirebaseDAO.SingleListener() {
            @Override public void onLoaded(Article article) {
                local.upsert(article, System.currentTimeMillis());
                cb.onSuccess(article);
            }
            @Override public void onError(String err) {
                Article cached = local.getById(id);
                if (cached != null) cb.onSuccess(cached); else cb.onError(err);
            }
        });
    }

    public void markViewed(Article a) { local.markViewed(a); }

    public void forceSyncAllToLocalIfNeeded() {
        remote.fetchAll(new ArticleFirebaseDAO.ListListener() {
            @Override public void onLoaded(List<Article> list) {
                long now = System.currentTimeMillis();
                for (Article a : list) local.upsert(a, now);
            }
            @Override public void onError(String err) {}
        });
    }

    public List<Article> getViewedCached() { return local.getViewedArticles(); }
}
