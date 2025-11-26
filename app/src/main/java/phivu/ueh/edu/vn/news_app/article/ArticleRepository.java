package phivu.ueh.edu.vn.news_app.article;

import android.content.Context;

import java.util.List;

public class ArticleRepository {
    private ArticleDAO dao;
    public ArticleRepository(Context ctx) { dao = new ArticleDAO(ctx); }

    public long addArticle(String title, String description, String imageUrl, String content, int mainCategoryId, int authorId, String status, String createdAt) {
        return dao.addArticle(title, description, imageUrl, content, mainCategoryId, authorId, status, createdAt);
    }
    public boolean updateArticle(int id, String title, String description, String imageUrl, String content, int mainCategoryId, String status) {
        return dao.updateArticle(id, title, description, imageUrl, content, mainCategoryId, status);
    }
    public boolean deleteArticle(int id) { return dao.deleteArticle(id); }
    public Article getById(int id) { return dao.getById(id); }
    public List<Article> getAll() { return dao.getAll(); }
    public List<Article> getByCategory(int catId) { return dao.getByCategory(catId); }
}
