package phivu.ueh.edu.vn.news_app.data.remote;

import com.google.firebase.database.*;
import java.util.*;
import phivu.ueh.edu.vn.news_app.model.User;

public class UserFirebaseDAO {

    private DatabaseReference ref;

    public UserFirebaseDAO() {
        ref = FirebaseDatabase.getInstance().getReference("users");
    }

    public void fetchAll(UserListener listener) {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<User> list = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    User u = s.getValue(User.class);
                    list.add(u);
                }
                listener.onLoaded(list);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    public interface UserListener {
        void onLoaded(List<User> users);
        void onError(String err);
    }
}
