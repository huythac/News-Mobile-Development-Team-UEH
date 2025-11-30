package phivu.ueh.edu.vn.news_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

import phivu.ueh.edu.vn.news_app.article.Article;
import phivu.ueh.edu.vn.news_app.article.ArticleRepository;

public class AccountActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private TabLayout tabLayoutAccount;

    // Các Layout chứa nội dung
    private RelativeLayout layoutTabPosts;
    private LinearLayout layoutTabInfo;
    private TextView layoutTabStats;
    private LinearLayout layoutEmptyState; // Hiển thị khi không có bài

    // RecyclerView cho bài viết
    private RecyclerView rvMyArticles;
    private ArticleAdapter adapter;
    private ArticleRepository articleRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        initViews();
        setupBottomNav();
        setupTabs();
        loadMyArticles();
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        tabLayoutAccount = findViewById(R.id.tabLayoutAccount);

        layoutTabPosts = findViewById(R.id.layoutTabPosts);
        layoutTabInfo = findViewById(R.id.layoutTabInfo);
        layoutTabStats = findViewById(R.id.layoutTabStats);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        rvMyArticles = findViewById(R.id.rvMyArticles);
        rvMyArticles.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupBottomNav() {
        // Set mặc định chọn tab Tài khoản
        bottomNavigationView.setSelectedItemId(R.id.nav_account);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    // Chuyển về Home
                    Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                    // Dùng flags để xóa stack, tránh back lại account khi ở home
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // Đóng AccountActivity
                    return true;
                } else if (id == R.id.nav_account) {
                    return true;
                } else if (id == R.id.nav_search) {
                    Toast.makeText(AccountActivity.this, "Tìm kiếm", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });
    }

    private void setupTabs() {
        tabLayoutAccount.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Ẩn tất cả trước
                layoutTabPosts.setVisibility(View.GONE);
                layoutTabInfo.setVisibility(View.GONE);
                layoutTabStats.setVisibility(View.GONE);

                // Hiện tab tương ứng
                int position = tab.getPosition();
                switch (position) {
                    case 0: // Bài viết
                        layoutTabPosts.setVisibility(View.VISIBLE);
                        break;
                    case 1: // Thông tin
                        layoutTabInfo.setVisibility(View.VISIBLE);
                        break;
                    case 2: // Số liệu
                        layoutTabStats.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadMyArticles() {
        articleRepository = new ArticleRepository(this);
        // Tạm thời lấy tất cả (Sau này bạn viết thêm hàm getByAuthorId trong Repo)
        List<Article> myList = articleRepository.getAll();

        if (myList != null && !myList.isEmpty()) {
            adapter = new ArticleAdapter(myList, this);
            rvMyArticles.setAdapter(adapter);
            rvMyArticles.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        } else {
            // Nếu không có bài nào -> Hiện Empty State
            rvMyArticles.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        }
    }
}