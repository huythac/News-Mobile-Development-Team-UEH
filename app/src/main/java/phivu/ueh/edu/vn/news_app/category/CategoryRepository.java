package phivu.ueh.edu.vn.news_app.category;

import android.content.Context;
import java.util.List;

public class CategoryRepository {
    private CategoryDAO dao;
    public CategoryRepository(Context ctx) { dao = new CategoryDAO(ctx); }

    public boolean addCategory(String name, String desc) { return dao.addCategory(name, desc); }
    public boolean deleteCategory(int id) { return dao.deleteCategory(id); }
    public List<Category> getAll() { return dao.getAll(); }
}
