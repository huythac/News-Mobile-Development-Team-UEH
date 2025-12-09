package phivu.ueh.edu.vn.news_app.data.remote;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import phivu.ueh.edu.vn.news_app.model.Category;

public class CategoryFirebaseDAO {
    private final DatabaseReference ref;
    public interface ListListener { void onLoaded(List<Category> list); void onError(String err); }

    public CategoryFirebaseDAO() {
        ref = FirebaseDatabase.getInstance().getReference("categories");
    }

    public void fetchAll(final ListListener listener) {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
                List<Category> out = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    Category c = s.getValue(Category.class);
                    if (c != null) {
                        c.setId(s.getKey());
                        out.add(c);
                    }
                }
                listener.onLoaded(out);
            }
            @Override public void onCancelled(DatabaseError error) { listener.onError(error.getMessage()); }
        });
    }

    public void insert(Category c) {
        String id = ref.push().getKey();
        c.setId(id);
        ref.child(id).setValue(c);
    }

    public void update(Category c) {
        if (c.getId() == null) return;
        ref.child(c.getId()).setValue(c);
    }

    public void delete(String id) { ref.child(id).removeValue(); }
}
