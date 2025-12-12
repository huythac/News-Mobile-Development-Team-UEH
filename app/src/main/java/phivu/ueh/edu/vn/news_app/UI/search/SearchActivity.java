package phivu.ueh.edu.vn.news_app.UI.search;


import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.UI.main.HomeActivity; // Import Home
import phivu.ueh.edu.vn.news_app.UI.profile.ProfileActivity; // Import Profile
import phivu.ueh.edu.vn.news_app.UI.saved.SavedActivity; // Import Saved

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search); // Gắn layout Search

        // 1. Setup RecyclerView (Nếu muốn hiện data mẫu)
        // RecyclerView rv = findViewById(R.id.rvSearchArticles);
        // rv.setLayoutManager(new LinearLayoutManager(this));

        // 2. Xử lý Bottom Navigation
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // Đặt mục chọn hiện tại là Search
        bottomNav.setSelectedItemId(R.id.nav_search);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_search) {
                return true; // Đang ở Search thì không làm gì
            }
            else if (id == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
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
            else if (id == R.id.nav_account) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }
}