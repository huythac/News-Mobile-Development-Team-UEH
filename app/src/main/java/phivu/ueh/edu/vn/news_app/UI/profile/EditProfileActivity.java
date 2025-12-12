package phivu.ueh.edu.vn.news_app.UI.profile;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

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

        if (fbUser != null) {
            currentUid = fbUser.getUid();
            loadCurrentData();
        } else {
            Toast.makeText(this, "Lỗi: Chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- SỬA LẠI SỰ KIỆN LƯU ---
        btnSave.setOnClickListener(v -> {
            String newBio = edtBio.getText().toString().trim();

            // 1. Nếu currentUser bị null (do chưa có trong DB), ta tạo mới từ Auth
            if (currentUser == null) {
                if (fbUser != null) {
                    currentUser = new User(
                            fbUser.getUid(),
                            fbUser.getDisplayName() != null ? fbUser.getDisplayName() : "No Name",
                            fbUser.getEmail(),
                            "user"
                    );
                } else {
                    Toast.makeText(this, "Không tìm thấy thông tin xác thực!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // 2. Cập nhật Bio mới
            currentUser.setBio(newBio);

            // 3. Đẩy lên Firebase
            userDAO.updateUser(currentUser);

            Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            finish(); // Quay về trang Profile
        });

        // Nút Hủy
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadCurrentData() {
        if (currentUid == null) return;

        userDAO.getUser(currentUid, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // Lấy dữ liệu về
                User userFromDb = snapshot.getValue(User.class);

                if (userFromDb != null) {
                    currentUser = userFromDb; // Gán vào biến toàn cục

                    if (currentUser.getBio() != null) {
                        edtBio.setText(currentUser.getBio());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EditProfileActivity.this, "Lỗi tải data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}