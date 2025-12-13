package phivu.ueh.edu.vn.news_app.ui.search;


import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.data.repository.CategoryRepository;
import phivu.ueh.edu.vn.news_app.model.Category;
import phivu.ueh.edu.vn.news_app.ui.main.HomeActivity;
import phivu.ueh.edu.vn.news_app.ui.profile.ProfileActivity;
import phivu.ueh.edu.vn.news_app.ui.saved.SavedActivity;
import phivu.ueh.edu.vn.news_app.ui.main.ArticleAdapter;
import phivu.ueh.edu.vn.news_app.data.repository.ArticleRepository;
import phivu.ueh.edu.vn.news_app.model.Article;


public class SearchActivity extends AppCompatActivity {

    private RecyclerView rvArticles;
    private ArticleAdapter adapter;
    private ArticleRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        EditText edtFakeSearch = findViewById(R.id.edtFakeSearch);

        if (edtFakeSearch == null) {
        } else {
            // Cấu hình để nó hoạt động như một nút bấm
            edtFakeSearch.setFocusable(false);
            edtFakeSearch.setClickable(true);
            edtFakeSearch.setOnClickListener(v -> {
                // Mở màn hình nhập liệu
                startActivity(new Intent(SearchActivity.this, SearchInputActivity.class));
            });
        }

        rvArticles = findViewById(R.id.rvSearchArticles);
        rvArticles.setLayoutManager(new LinearLayoutManager(this));

        repo = new ArticleRepository(this);
        loadTopArticles();

        setupBottomNavigation();
    }

    private void loadTopArticles() {
        repo.getList(new ArticleRepository.ListCallback() {
            @Override
            public void onSuccess(List<Article> list) {
                adapter = new ArticleAdapter(SearchActivity.this, list);
                rvArticles.setAdapter(adapter);
            }
            @Override
            public void onError(String err) { }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_search);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_search) return true;
            if (id == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                overridePendingTransition(0, 0);
                finish(); return true;
            }
            if (id == R.id.nav_saved) {
                startActivity(new Intent(getApplicationContext(), SavedActivity.class));
                overridePendingTransition(0, 0);
                finish(); return true;
            }
            if (id == R.id.nav_account) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish(); return true;
            }
            return false;
        });
    }
}