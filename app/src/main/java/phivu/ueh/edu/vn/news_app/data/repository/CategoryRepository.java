package phivu.ueh.edu.vn.news_app.data.repository;

import android.content.Context;

import java.util.List;

import phivu.ueh.edu.vn.news_app.data.local.category.CategoryDAO;
import phivu.ueh.edu.vn.news_app.data.remote.CategoryFirebaseDAO;
import phivu.ueh.edu.vn.news_app.model.Category;

public class CategoryRepository {
    private final CategoryDAO local;
    private final CategoryFirebaseDAO remote;

    public interface Callback { void onSuccess(List<Category> list); void onError(String err); }

    public CategoryRepository(Context ctx) {
        local = new CategoryDAO(ctx);
        remote = new CategoryFirebaseDAO();
    }

    public void getCategories(final Callback cb) {
        remote.fetchAll(new CategoryFirebaseDAO.ListListener() {
            @Override public void onLoaded(List<Category> list) {
                for (Category c : list) local.upsert(c);
                cb.onSuccess(list);
            }
            @Override public void onError(String err) {
                List<Category> cached = local.getAll();
                if (cached.isEmpty()) cb.onError(err); else cb.onSuccess(cached);
            }
        });
    }

    // admin methods
    public void createCategory(Category c) { remote.insert(c); }
    public void updateCategory(Category c) { remote.update(c); }
    public void deleteCategory(String id) { remote.delete(id); }
}
