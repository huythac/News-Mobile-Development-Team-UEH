package phivu.ueh.edu.vn.news_app.ui.profile;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.data.remote.UserFirebaseDAO;
import phivu.ueh.edu.vn.news_app.model.User;

public class EditProfileActivity extends AppCompatActivity {

    private EditText edtBio;
    private UserFirebaseDAO userDAO;
    private String currentUid;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        edtBio = findViewById(R.id.edtBio);
        Button btnSave = findViewById(R.id.btnSaveProfile);
        Button btnCancel = findViewById(R.id.btnCancelEdit);

        userDAO = new UserFirebaseDAO();
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();

        if (fbUser == null) {
            Toast.makeText(this, "Lỗi: Chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUid = fbUser.getUid();

        // Load dữ liệu user từ Firestore
        loadCurrentData();

        btnSave.setOnClickListener(v -> {
            String newBio = edtBio.getText().toString().trim();

            // Nếu user chưa tồn tại trong Firestore (trường hợp hiếm)
            if (currentUser == null) {
                currentUser = new User(
                        fbUser.getUid(),
                        fbUser.getDisplayName() != null
                                ? fbUser.getDisplayName()
                                : "No Name",
                        fbUser.getEmail(),
                        "USER"
                );
            }

            currentUser.setBio(newBio);
            userDAO.updateUser(currentUser);

            Toast.makeText(this,
                    "Cập nhật thành công!",
                    Toast.LENGTH_SHORT).show();

            finish();
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadCurrentData() {
        if (currentUid == null) return;

        userDAO.getUser(currentUid, new UserFirebaseDAO.SingleListener() {

            @Override
            public void onLoaded(User user) {
                currentUser = user;

                if (user.getBio() != null) {
                    edtBio.setText(user.getBio());
                }
            }

            @Override
            public void onError(String err) {
                Toast.makeText(EditProfileActivity.this,
                        "Không tải được profile",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
