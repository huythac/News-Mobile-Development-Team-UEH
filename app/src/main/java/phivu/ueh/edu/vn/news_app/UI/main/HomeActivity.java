package phivu.ueh.edu.vn.news_app.UI.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent; // 1. Import Intent
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView; // 2. Import BottomNav

import java.util.List;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.UI.profile.ProfileActivity; // 3. Import các Activity đích
import phivu.ueh.edu.vn.news_app.UI.saved.SavedActivity;
import phivu.ueh.edu.vn.news_app.UI.search.SearchActivity;
import phivu.ueh.edu.vn.news_app.data.repository.ArticleRepository;
import phivu.ueh.edu.vn.news_app.data.repository.CategoryRepository;
import phivu.ueh.edu.vn.news_app.model.Article;
import phivu.ueh.edu.vn.news_app.model.Category;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvArticles;
    private RecyclerView rvTopics;

    private ArticleAdapter articleAdapter;
    private CategoryAdapter categoryAdapter;

    private ArticleRepository repo;
    private CategoryRepository categoryRepo;

    TextView tvForYou, tvTopic;
    View underlineForYou, underlineTopic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // ===== 1. ÁNH XẠ TAB =====
        tvForYou = findViewById(R.id.tvForYou);
        tvTopic = findViewById(R.id.tvTopic);

        underlineForYou = findViewById(R.id.viewUnderlineForYou);
        underlineTopic = findViewById(R.id.viewUnderlineTopic);

        // ===== 2. ÁNH XẠ LIST =====
        rvArticles = findViewById(R.id.recyclerViewArticles);
        rvArticles.setLayoutManager(new LinearLayoutManager(this));

        rvTopics = findViewById(R.id.recyclerViewTopics);
        rvTopics.setLayoutManager(new LinearLayoutManager(this));
        rvTopics.setVisibility(View.GONE);

        // ===== 3. KHỞI TẠO REPO =====
        repo = new ArticleRepository(this);
        categoryRepo = new CategoryRepository(this);

        // ===== 4. SETUP NAV & LOAD DATA =====
        setupBottomNavigation();
        loadArticles();          // Load bài báo mặc định

        // ===== 5. XỬ LÝ SỰ KIỆN CLICK TAB =====
        tvForYou.setOnClickListener(v -> {
            setTabSelected(true);
            showArticles();
        });

        tvTopic.setOnClickListener(v -> {
            setTabSelected(false);
            showTopics();
            loadCategories(); // Load category khi bấm tab
        });

        setTabSelected(true);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) return true;

            Intent intent = null;
            if (id == R.id.nav_search) {
                intent = new Intent(this, SearchActivity.class);
            }
            else if (id == R.id.nav_saved) {
                intent = new Intent(this, SavedActivity.class);
            }
            else if (id == R.id.nav_account) {
                intent = new Intent(this, ProfileActivity.class);
            }

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(0, 0); // Tắt hiệu ứng chuyển cảnh để mượt hơn
                return true;
            }
            return false;
        });
    }

    // =================================================
    // LOAD ARTICLE
    // =================================================
    private void loadArticles() {
        repo.getList(new ArticleRepository.ListCallback() {
            @Override
            public void onSuccess(List<Article> list) {
                articleAdapter = new ArticleAdapter(HomeActivity.this, list);
                rvArticles.setAdapter(articleAdapter);
            }

            @Override
            public void onError(String err) {
                Toast.makeText(HomeActivity.this, "Không tải được dữ liệu: " + err, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =================================================
    // LOAD CATEGORY
    // =================================================
    private void loadCategories() {
        categoryRepo.getCategories(new CategoryRepository.Callback() {
            @Override
            public void onSuccess(List<Category> list) {
                categoryAdapter = new CategoryAdapter(HomeActivity.this, list);
                rvTopics.setAdapter(categoryAdapter);
            }

            @Override
            public void onError(String err) {
                Toast.makeText(HomeActivity.this, "Không tải được chủ đề: " + err, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =================================================
    // TAB UI LOGIC
    // =================================================
    private void setTabSelected(boolean isForYou) {
        if (isForYou) {
            tvForYou.setTextColor(Color.BLACK);
            tvForYou.setTypeface(null, Typeface.BOLD);
            underlineForYou.setVisibility(View.VISIBLE);

            tvTopic.setTextColor(Color.GRAY);
            tvTopic.setTypeface(null, Typeface.NORMAL);
            underlineTopic.setVisibility(View.INVISIBLE);
        } else {
            tvTopic.setTextColor(Color.BLACK);
            tvTopic.setTypeface(null, Typeface.BOLD);
            underlineTopic.setVisibility(View.VISIBLE);

            tvForYou.setTextColor(Color.GRAY);
            tvForYou.setTypeface(null, Typeface.NORMAL);
            underlineForYou.setVisibility(View.INVISIBLE);
        }
    }

    private void showArticles() {
        rvArticles.setVisibility(View.VISIBLE);
        rvTopics.setVisibility(View.GONE);
    }

    private void showTopics() {
        rvArticles.setVisibility(View.GONE);
        rvTopics.setVisibility(View.VISIBLE);
    }
}