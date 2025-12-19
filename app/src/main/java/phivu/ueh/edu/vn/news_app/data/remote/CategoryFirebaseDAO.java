package phivu.ueh.edu.vn.news_app.data.remote;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import phivu.ueh.edu.vn.news_app.model.Category;

public class CategoryFirebaseDAO {

    private final FirebaseFirestore db;

    public interface ListListener {
        void onLoaded(List<Category> list);
        void onError(String err);
    }

    public CategoryFirebaseDAO() {
        db = FirebaseFirestore.getInstance();
    }

    // =========================
    // FETCH ALL
    // =========================
    public void fetchAll(final ListListener listener) {
        db.collection("categories")
                .get()
                .addOnSuccessListener(query -> {
                    List<Category> list = new ArrayList<>();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Category c = doc.toObject(Category.class);
                        if (c != null) {
                            c.setId(doc.getId());
                            list.add(c);
                        }
                    }

                    listener.onLoaded(list);
                })
                .addOnFailureListener(e ->
                        listener.onError(e.getMessage())
                );
    }

    // =========================
    // INSERT
    // =========================
    public void insert(Category c) {
        db.collection("categories")
                .add(c)
                .addOnSuccessListener(doc -> {
                    c.setId(doc.getId());
                });
    }

    // =========================
    // UPDATE
    // =========================
    public void update(Category c) {
        if (c.getId() == null) return;

        db.collection("categories")
                .document(c.getId())
                .set(c);
    }

    // =========================
    // DELETE
    // =========================
    public void delete(String id) {
        db.collection("categories")
                .document(id)
                .delete();
    }
}
