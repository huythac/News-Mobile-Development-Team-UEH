package phivu.ueh.edu.vn.news_app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.ui.main.HomeActivity;
import phivu.ueh.edu.vn.news_app.ui.saved.SavedActivity;
import phivu.ueh.edu.vn.news_app.ui.search.SearchActivity;
import phivu.ueh.edu.vn.news_app.data.remote.UserFirebaseDAO;
import phivu.ueh.edu.vn.news_app.model.User;

public class ProfileActivity extends AppCompatActivity {

    // Khai báo các biến giao diện
    private TextView tvName, tvBio, tvWriteBio;
    private LinearLayout layoutEmpty;

    // Khai báo xử lý dữ liệu
    private UserFirebaseDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 1. Khởi tạo DAO
        userDAO = new UserFirebaseDAO();

        // 2. Ánh xạ View (Kết nối ID từ XML)
        tvName = findViewById(R.id.tvProfileName);
        tvBio = findViewById(R.id.tvUserBio);          // Text hiển thị Bio khi có dữ liệu
        tvWriteBio = findViewById(R.id.tvWriteBio);    // Nút chữ xanh "Viết giới thiệu"
        layoutEmpty = findViewById(R.id.layoutEmptyState); // Khối hiển thị khi chưa có Bio

        // 3. Xử lý nút Cài đặt (Bánh răng) - Code cũ của bạn
        ImageView btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // 4. Xử lý Chuyển sang màn hình Chỉnh sửa (EditProfileActivity)
        // Tạo một sự kiện chung để gán cho nhiều nút
        View.OnClickListener goToEditScreen = v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        };

        findViewById(R.id.btnEditProfile).setOnClickListener(goToEditScreen); // Nút xám to
        findViewById(R.id.fabEditBio).setOnClickListener(goToEditScreen);     // Nút tròn xanh (FAB)
        tvWriteBio.setOnClickListener(goToEditScreen);                        // Dòng chữ xanh lá

        // 5. Cài đặt Bottom Navigation (Code cũ của bạn)
        setupBottomNavigation();
    }

    // Hàm onResume: Chạy mỗi khi màn hình hiện lên (VD: quay lại từ trang Edit)
    // Để tự động cập nhật dữ liệu mới nhất mà không cần tắt app mở lại
    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    // Hàm tải dữ liệu từ Firebase
    private void loadUserData() {
        String uid = FirebaseAuth.getInstance().getUid();

        // Nếu chưa đăng nhập thì không làm gì
        if (uid == null) return;

        // Gọi hàm lấy User từ DAO
        userDAO.getUser(uid, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    // Cập nhật tên hiển thị
                    if (user.getFullName() != null) {
                        tvName.setText(user.getFullName());
                    }

                    // --- LOGIC QUAN TRỌNG: KIỂM TRA BIO ---
                    if (user.getBio() != null && !user.getBio().isEmpty()) {
                        // TRƯỜNG HỢP 1: Đã có Bio
                        tvBio.setText(user.getBio());

                        // Hiện text Bio, Ẩn khối Empty
                        tvBio.setVisibility(View.VISIBLE);
                        layoutEmpty.setVisibility(View.GONE);
                    } else {
                        // TRƯỜNG HỢP 2: Chưa có Bio
                        // Ẩn text Bio, Hiện khối Empty
                        tvBio.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm xử lý thanh điều hướng dưới cùng
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_account); // Đánh dấu icon Profile đang sáng

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_account) {
                return true;
            }
            else if (id == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                overridePendingTransition(0, 0); // Tắt hiệu ứng chuyển cảnh
                finish();
                return true;
            }
            else if (id == R.id.nav_search) {
                startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            else if (id == R.id.nav_saved) {
                startActivity(new Intent(getApplicationContext(), SavedActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }
}