package phivu.ueh.edu.vn.news_app.data.remote;

import com.google.firebase.database.*;
import java.util.*;
import phivu.ueh.edu.vn.news_app.model.Article;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;


public class ArticleFirebaseDAO {

    private DatabaseReference ref;

    public ArticleFirebaseDAO() {
        ref = FirebaseDatabase.getInstance().getReference("articles");
    }

    public interface ListListener {
        void onLoaded(List<Article> list);
        void onError(String err);
    }

    public interface SingleListener {
        void onLoaded(Article article);
        void onError(String err);
    }

    // CREATE
    public void insert(Article a) {
        String id = ref.push().getKey();
        a.setId(id);
        ref.child(id).setValue(a);
    }

    // READ ALL
    public void fetchAll(ListListener listener) {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Article> out = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    Article a = s.getValue(Article.class);
                    out.add(a);
                }
                listener.onLoaded(out);
            }

            @Override public void onCancelled(DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    // READ ONE
    public void fetchById(String id, SingleListener listener) {
        ref.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
                Article a = snapshot.getValue(Article.class);
                if (a != null) listener.onLoaded(a);
                else listener.onError("Not found");
            }
            @Override public void onCancelled(DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    // UPDATE
    public void update(Article article) {
        if (article.getId() == null) return;
        ref.child(article.getId()).setValue(article);
    }

    // DELETE
    public void delete(String id) {
        ref.child(id).removeValue();
    }
}
