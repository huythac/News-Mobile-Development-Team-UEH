package phivu.ueh.edu.vn.news_app.data.remote;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import phivu.ueh.edu.vn.news_app.model.Article;

public class ArticleFirebaseDAO {

    private final FirebaseFirestore db;

    public ArticleFirebaseDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public interface ListListener {
        void onLoaded(List<Article> list);
        void onError(String err);
    }

    public interface SingleListener {
        void onLoaded(Article a);
        void onError(String err);
    }

    // =========================
    // INSERT
    // =========================
    public void insert(Article a) {
        db.collection("articles")
                .add(a)
                .addOnSuccessListener(docRef -> {
                    a.setId(docRef.getId());
                });
    }

    // =========================
    // FETCH ALL (new → old)
    // =========================
    public void fetchAll(final ListListener listener) {
        db.collection("articles")
                .orderBy("publishDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    List<Article> list = new ArrayList<>();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Article a = doc.toObject(Article.class);
                        if (a != null) {
                            a.setId(doc.getId());
                            list.add(a);
                        }
                    }

                    listener.onLoaded(list);
                })
                .addOnFailureListener(e ->
                        listener.onError(e.getMessage())
                );
    }

    // =========================
    // FETCH BY ID
    // =========================
    public void fetchById(String id, final SingleListener listener) {
        db.collection("articles")
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Article a = doc.toObject(Article.class);
                        if (a != null) {
                            a.setId(doc.getId());
                            listener.onLoaded(a);
                        } else {
                            listener.onError("Parse error");
                        }
                    } else {
                        listener.onError("Not found");
                    }
                })
                .addOnFailureListener(e ->
                        listener.onError(e.getMessage())
                );
    }

    // =========================
    // FETCH BY CATEGORY
    // =========================
    public void fetchByCategory(String categoryId, final ListListener listener) {
        db.collection("articles")
                .whereEqualTo("categoryId", categoryId)
                .orderBy("publishDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    List<Article> list = new ArrayList<>();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Article a = doc.toObject(Article.class);
                        if (a != null) {
                            a.setId(doc.getId());
                            list.add(a);
                        }
                    }

                    listener.onLoaded(list);
                })
                .addOnFailureListener(e ->
                        listener.onError(e.getMessage())
                );
    }

    // =========================
    // UPDATE
    // =========================
    public void update(Article article) {
        if (article.getId() == null) return;

        db.collection("articles")
                .document(article.getId())
                .set(article);
    }

    // =========================
    // DELETE
    // =========================
    public void delete(String id) {
        db.collection("articles")
                .document(id)
                .delete();
    }
}
