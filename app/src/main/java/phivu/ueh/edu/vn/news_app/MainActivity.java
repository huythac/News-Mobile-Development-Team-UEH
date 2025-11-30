package phivu.ueh.edu.vn.news_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.List;

import phivu.ueh.edu.vn.news_app.article.Article;
import phivu.ueh.edu.vn.news_app.article.ArticleRepository;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArticleAdapter adapter;
    private List<Article> articleList;
    private ArticleRepository articleRepository;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Ánh xạ View
        recyclerView = findViewById(R.id.recyclerViewArticles);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // 2. Cấu hình RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 3. Lấy dữ liệu từ Database
        articleRepository = new ArticleRepository(this);
        articleList = articleRepository.getAll();

        // Kiểm tra dữ liệu
        if (articleList != null && !articleList.isEmpty()) {
            adapter = new ArticleAdapter(articleList, this);
            recyclerView.setAdapter(adapter);
        } else {
            // Nếu chưa có tin nào (lần đầu chạy), thông báo nhẹ
            Toast.makeText(this, "Chưa có bài viết nào trong CSDL", Toast.LENGTH_SHORT).show();
        }

        // 4. Xử lý sự kiện Menu Đáy (Navigation)
        // Set mặc định chọn tab Tin tức (Home)
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    // Đang ở Home rồi thì không làm gì hoặc reload
                    return true;
                }
                // Trong MainActivity.java -> onNavigationItemSelected

                else if (id == R.id.nav_account) {
                    // CHUYỂN SANG MÀN HÌNH TÀI KHOẢN
                    Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                    startActivity(intent);
                    // Không finish() MainActivity để người dùng có thể back lại nếu muốn,
                    // hoặc finish() nếu muốn cơ chế giống Facebook.
                    return true;
                }
                else if (id == R.id.nav_search) {
                    Toast.makeText(MainActivity.this, "Chức năng Tìm kiếm", Toast.LENGTH_SHORT).show();
                    return true;
                }

                return false;
            }
        });
    }
}