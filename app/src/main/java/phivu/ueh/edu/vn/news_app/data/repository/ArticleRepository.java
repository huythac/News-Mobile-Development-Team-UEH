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
        // First, try to load from cache (offline-friendly)
        Article cached = local.getById(id);
        if (cached != null) {
            // Return cached data immediately
            cb.onSuccess(cached);
        }

        // Then, sync from Firestore in background
        remote.fetchById(id, new ArticleFirebaseDAO.SingleListener() {
            @Override public void onLoaded(Article article) {
                // Update cache and return fresh data
                local.upsert(article, System.currentTimeMillis());
                cb.onSuccess(article);
            }
            @Override public void onError(String err) {
                // If no cache was found earlier, return error
                if (cached == null) {
                    cb.onError(err);
                }
                // If cache exists, silently fail (already shown cached data)
            }
        });
    }

    /**
     * Get article from cache only (no network call)
     * Thread-safe: Can be called from any thread
     */
    public Article getArticleFromCache(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        try {
            return local.getById(id.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Sync article from Firestore (force refresh)
     * Hardening: Validate id, handle null article, handle upsert errors
     */
    public void syncArticleFromRemote(final String id, final SingleCallback cb) {
        if (id == null || id.trim().isEmpty()) {
            if (cb != null) {
                cb.onError("Article ID is null or empty");
            }
            return;
        }

        final String sanitizedId = id.trim();
        remote.fetchById(sanitizedId, new ArticleFirebaseDAO.SingleListener() {
            @Override public void onLoaded(Article article) {
                if (article == null) {
                    if (cb != null) {
                        cb.onError("Article data is null");
                    }
                    return;
                }

                // Validate article ID matches
                if (article.getId() == null || !article.getId().equals(sanitizedId)) {
                    article.setId(sanitizedId); // Fix ID mismatch
                }

                try {
                    local.upsert(article, System.currentTimeMillis());
                    if (cb != null) {
                        cb.onSuccess(article);
                    }
                } catch (Exception e) {
                    // Even if upsert fails, return the article
                    if (cb != null) {
                        cb.onSuccess(article);
                    }
                }
            }
            @Override public void onError(String err) {
                if (cb != null) {
                    cb.onError(err != null ? err : "Unknown error");
                }
            }
        });
    }

    public void getArticlesByCategory(String categoryId, ListCallback cb) {
        remote.fetchByCategory(categoryId, new ArticleFirebaseDAO.ListListener() {
            @Override
            public void onLoaded(List<Article> list) {
                cb.onSuccess(list);
            }

            @Override
            public void onError(String err) {
                cb.onError(err);
            }
        });
    }

    /**
     * Get articles by author ID, excluding current article
     * @param authorId Author ID to filter by
     * @param excludeId Article ID to exclude (current article)
     * @param maxCount Maximum number of articles (default: 10)
     * @param cb Callback for results
     */
    public void getArticlesByAuthor(String authorId, String excludeId, int maxCount, ListCallback cb) {
        if (authorId == null || authorId.trim().isEmpty()) {
            if (cb != null) {
                cb.onError("Author ID is null or empty");
            }
            return;
        }

        remote.fetchByAuthor(authorId.trim(), excludeId != null ? excludeId.trim() : null, 
                maxCount > 0 ? maxCount : 10, new ArticleFirebaseDAO.ListListener() {
            @Override
            public void onLoaded(List<Article> list) {
                if (cb != null) {
                    cb.onSuccess(list);
                }
            }

            @Override
            public void onError(String err) {
                if (cb != null) {
                    cb.onError(err != null ? err : "Unknown error");
                }
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
