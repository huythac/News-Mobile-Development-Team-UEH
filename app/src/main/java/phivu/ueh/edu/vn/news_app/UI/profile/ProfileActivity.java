package phivu.ueh.edu.vn.news_app.UI.profile; // Sửa package phù hợp

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.UI.main.MainActivity;
import phivu.ueh.edu.vn.news_app.UI.profile.SettingsActivity; // Import SettingsActivity

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // Gắn layout profile

        // --- PHẦN 1: XỬ LÝ NÚT BÁNH RĂNG (SETTINGS) ---
        ImageView btnSettings = findViewById(R.id.btnSettings); // ID trong XML là btnSettings hoặc btnOpenSettings

        btnSettings.setOnClickListener(v -> {
            // Chuyển sang trang Cài đặt
            Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        bottomNav.setSelectedItemId(R.id.nav_account);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_account) {
                return true;
            }
            else if (id == R.id.nav_home) {
                // Quay về trang chủ
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish(); // Đóng Profile để không chồng stack
                return true;
            }
            else if (id == R.id.nav_search) ;
            else if (id == R.id.nav_saved) ;

            return false;
        });
    }
}