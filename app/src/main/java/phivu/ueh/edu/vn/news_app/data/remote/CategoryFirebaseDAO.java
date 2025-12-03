package phivu.ueh.edu.vn.news_app.data.remote;

import com.google.firebase.database.*;
import java.util.*;
import phivu.ueh.edu.vn.news_app.model.Category;

public class CategoryFirebaseDAO {

    private final DatabaseReference ref;

    public CategoryFirebaseDAO() {
        ref = FirebaseDatabase.getInstance().getReference("categories");
    }

    // Callback list
    public interface ListListener {
        void onLoaded(List<Category> list);
        void onError(String err);
    }

    // Callback single
    public interface SingleListener {
        void onLoaded(Category category);
        void onError(String err);
    }

    // CREATE
    public void insert(Category c) {
        String id = ref.push().getKey();
        c.setId(id);
        ref.child(id).setValue(c);
    }

    // READ ALL
    public void fetchAll(ListListener listener) {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Category> out = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    Category c = s.getValue(Category.class);
                    if (c != null) {
                        c.setId(s.getKey()); // gán id từ Firebase
                        out.add(c);
                    }
                }
                listener.onLoaded(out);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    // READ ONE
    public void fetchById(String id, SingleListener listener) {
        ref.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Category c = snapshot.getValue(Category.class);
                if (c != null) {
                    c.setId(snapshot.getKey());
                    listener.onLoaded(c);
                } else {
                    listener.onError("Not found");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    // UPDATE
    public void update(Category c) {
        if (c.getId() == null) return;
        ref.child(c.getId()).setValue(c);
    }

    // DELETE
    public void delete(String id) {
        ref.child(id).removeValue();
    }
}
