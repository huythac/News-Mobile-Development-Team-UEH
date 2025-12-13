package phivu.ueh.edu.vn.news_app.ui.saved;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.ui.main.HomeActivity;
import phivu.ueh.edu.vn.news_app.ui.profile.ProfileActivity;
import phivu.ueh.edu.vn.news_app.ui.search.SearchActivity;

public class SavedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved); // Gắn layout Saved

        // 1. Setup RecyclerView (Để code sẵn chờ đổ data)
        RecyclerView rv = findViewById(R.id.rvSavedArticles);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // Mặc định hiện trạng thái Empty (như trong XML)
        // Khi có data thì setVisibility cho rv là VISIBLE và layoutEmptyState là GONE

        // 2. Xử lý Bottom Navigation
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // Đặt mục chọn hiện tại là Saved
        bottomNav.setSelectedItemId(R.id.nav_saved);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_saved) {
                return true; // Đang ở Saved thì không làm gì
            }
            else if (id == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            else if (id == R.id.nav_search) {
                startActivity(new Intent(getApplicationContext(), SearchActivity.class));
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