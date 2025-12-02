package phivu.ueh.edu.vn.news_app.data.local.category;

import android.content.Context;

import java.util.List;

import phivu.ueh.edu.vn.news_app.model.Category;

public class CategoryRepository {
    private CategoryDAO dao;
    public CategoryRepository(Context ctx) { dao = new CategoryDAO(ctx); }

    public boolean addCategory(String name, String desc) { return dao.addCategory(name, desc); }
    public boolean deleteCategory(int id) { return dao.deleteCategory(id); }
    public List<Category> getAll() { return dao.getAll(); }
}
