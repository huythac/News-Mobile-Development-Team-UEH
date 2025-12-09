package phivu.ueh.edu.vn.news_app.data.remote;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import phivu.ueh.edu.vn.news_app.model.User;

public class UserFirebaseDAO {
    private final DatabaseReference ref;
    public UserFirebaseDAO() {
        ref = FirebaseDatabase.getInstance().getReference("users");
    }

    public void createUser(User u) {
        ref.child(u.getId()).setValue(u);
    }

    public void updateUser(User u) {
        ref.child(u.getId()).setValue(u);
    }

    public void getUser(String uid, ValueEventListener listener) {
        ref.child(uid).addListenerForSingleValueEvent(listener);
    }
}
