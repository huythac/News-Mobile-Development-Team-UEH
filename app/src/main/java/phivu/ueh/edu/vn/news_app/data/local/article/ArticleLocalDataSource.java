package phivu.ueh.edu.vn.news_app.data.local.article;

import android.content.Context;

import java.util.List;

import phivu.ueh.edu.vn.news_app.model.Article;

public class ArticleLocalDataSource {
    private final ArticleDAO dao;

    public ArticleLocalDataSource(Context ctx) {
        dao = new ArticleDAO(ctx);
    }

    public void upsert(Article a, long ts) { dao.upsert(a, ts); }
    public void markViewed(Article a) { dao.markViewed(a); }
    public Article getById(String id) { return dao.getById(id); }
    public List<Article> getViewed() { return dao.getViewedArticles(); }
}
