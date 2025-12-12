package phivu.ueh.edu.vn.news_app.UI.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent; // 1. Import Intent
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView; // 2. Import BottomNav

import java.util.List;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.UI.profile.ProfileActivity; // 3. Import ProfileActivity
import phivu.ueh.edu.vn.news_app.UI.saved.SavedActivity;
import phivu.ueh.edu.vn.news_app.UI.search.SearchActivity;
import phivu.ueh.edu.vn.news_app.data.repository.ArticleRepository;
import phivu.ueh.edu.vn.news_app.model.Article;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvArticles;
    private ArticleAdapter adapter;
    private ArticleRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // --- PHẦN 1: Setup RecyclerView (Code cũ của bạn) ---
        rvArticles = findViewById(R.id.recyclerViewArticles);
        rvArticles.setLayoutManager(new LinearLayoutManager(this));

        // --- PHẦN 2: Xử lý Bottom Navigation (Code MỚI THÊM) ---
        setupBottomNavigation();

        // --- PHẦN 3: Load Data (Code cũ của bạn) ---
        repo = new ArticleRepository(this);
        loadArticles();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_home); // Home chọn Home

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) return true;

            Intent intent = null;
            if (id == R.id.nav_search) intent = new Intent(this, SearchActivity.class);
            else if (id == R.id.nav_saved) intent = new Intent(this, SavedActivity.class);
            else if (id == R.id.nav_account) intent = new Intent(this, ProfileActivity.class);

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(0, 0); // Quan trọng: Tắt hiệu ứng nháy
                // Không gọi finish() ở Home để giữ nó làm gốc (hoặc gọi finish() tùy logic bạn muốn)
                return true;
            }
            return false;
        });
    }

    private void loadArticles() {
        repo.getList(new ArticleRepository.ListCallback() {
            @Override
            public void onSuccess(List<Article> list) {
                adapter = new ArticleAdapter(HomeActivity.this, list); // Lưu ý: Adapter constructor có thể khác tùy bạn viết
                rvArticles.setAdapter(adapter);
            }

            @Override
            public void onError(String err) {
                Toast.makeText(HomeActivity.this, "Không tải được dữ liệu: " + err,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}