package phivu.ueh.edu.vn.news_app.data.remote;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import phivu.ueh.edu.vn.news_app.model.Article;

public class ArticleFirebaseDAO {
    private final DatabaseReference ref;

    public ArticleFirebaseDAO() {
        ref = FirebaseDatabase.getInstance().getReference("articles");
    }

    public interface ListListener { void onLoaded(List<Article> list); void onError(String err); }
    public interface SingleListener { void onLoaded(Article a); void onError(String err); }

    public void insert(Article a) {
        String id = ref.push().getKey();
        a.setId(id);
        ref.child(id).setValue(a);
    }

    public void fetchAll(final ListListener listener) {
        ref.orderByChild("publishDate")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Article> out = new ArrayList<>();

                        for (DataSnapshot s : snapshot.getChildren()) {
                            Article a = s.getValue(Article.class);
                            if (a != null) {
                                a.setId(s.getKey());
                                out.add(a);
                            }
                        }

                        // 🔹 Firebase trả CŨ → MỚI → đảo lại
                        java.util.Collections.reverse(out);

                        listener.onLoaded(out);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }


    public void fetchById(String id, final SingleListener listener) {
        ref.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
                Article a = snapshot.getValue(Article.class);
                if (a != null) {
                    a.setId(snapshot.getKey());
                    listener.onLoaded(a);
                } else listener.onError("Not found");
            }
            @Override public void onCancelled(DatabaseError error) { listener.onError(error.getMessage()); }
        });
    }

    public void fetchByCategory(String categoryId, ListListener listener) {

        ref.orderByChild("categoryId").equalTo(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Article> list = new ArrayList<>();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Article a = ds.getValue(Article.class);
                            if (a != null) {
                                a.setId(ds.getKey());
                                list.add(a);
                            }
                        }

                        // 🔹 Sort theo publishDate (mới → cũ)
                        list.sort((a1, a2) ->
                                Long.compare(a2.getPublishDate(), a1.getPublishDate())
                        );

                        listener.onLoaded(list);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }




    public void update(Article article) {
        if (article.getId() == null) return;
        ref.child(article.getId()).setValue(article);
    }

    public void delete(String id) { ref.child(id).removeValue(); }
}
