package phivu.ueh.edu.vn.news_app.data.local.category;

import android.content.Context;

import java.util.List;

import phivu.ueh.edu.vn.news_app.model.Category;

public class CategoryLocalDataSource {
    private final CategoryDAO dao;
    public CategoryLocalDataSource(Context ctx) { dao = new CategoryDAO(ctx); }
    public void upsert(Category c) { dao.upsert(c); }
    public List<Category> getAll() { return dao.getAll(); }
    public Category getById(String id) { return dao.getById(id); }
}
