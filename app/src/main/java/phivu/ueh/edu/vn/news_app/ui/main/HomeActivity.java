package phivu.ueh.edu.vn.news_app.ui.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.List;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.auth.SessionManager;
import phivu.ueh.edu.vn.news_app.data.repository.ArticleRepository;
import phivu.ueh.edu.vn.news_app.data.repository.CategoryRepository;
import phivu.ueh.edu.vn.news_app.data.repository.FollowCategoryRepository;
import phivu.ueh.edu.vn.news_app.model.Article;
import phivu.ueh.edu.vn.news_app.model.Category;
import phivu.ueh.edu.vn.news_app.ui.profile.ProfileActivity;
import phivu.ueh.edu.vn.news_app.ui.saved.SavedActivity;
import phivu.ueh.edu.vn.news_app.ui.search.SearchActivity;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvArticles, rvTopics;

    private ArticleAdapter articleAdapter;
    private CategoryAdapter categoryAdapter;

    private ArticleRepository articleRepo;
    private CategoryRepository categoryRepo;
    private FollowCategoryRepository followRepo;

    private HashMap<String, Boolean> followMap = new HashMap<>();
    private String userId;

    private TextView tvForYou, tvTopic;
    private View underlineForYou, underlineTopic;

    // ======================================================
    // LIFECYCLE
    // ======================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        userId = currentUser.getUid();

        initViews();
        initRepos();
        setupBottomNavigation();

        loadArticles();
        setTabSelected(true);
    }

    // ======================================================
    // INIT
    // ======================================================
    private void initViews() {
        FloatingActionButton fabEditBio = findViewById(R.id.fabEditBio);
        fabEditBio.setVisibility(SessionManager.isAdmin() ? View.VISIBLE : View.GONE);

        tvForYou = findViewById(R.id.tvForYou);
        tvTopic = findViewById(R.id.tvTopic);
        underlineForYou = findViewById(R.id.viewUnderlineForYou);
        underlineTopic = findViewById(R.id.viewUnderlineTopic);

        rvArticles = findViewById(R.id.recyclerViewArticles);
        rvArticles.setLayoutManager(new LinearLayoutManager(this));

        rvTopics = findViewById(R.id.recyclerViewTopics);
        rvTopics.setLayoutManager(new LinearLayoutManager(this));
        rvTopics.setVisibility(View.GONE);

        tvForYou.setOnClickListener(v -> {
            setTabSelected(true);
            showArticles();
        });

        tvTopic.setOnClickListener(v -> {
            setTabSelected(false);
            showTopics();
            loadFollowThenCategories();
        });
    }

    private void initRepos() {
        articleRepo = new ArticleRepository(this);
        categoryRepo = new CategoryRepository(this);
        followRepo = new FollowCategoryRepository(this);
    }

    // ======================================================
    // DATA LOAD
    // ======================================================
    private void loadArticles() {
        articleRepo.getList(new ArticleRepository.ListCallback() {
            @Override
            public void onSuccess(List<Article> list) {
                articleAdapter = new ArticleAdapter(HomeActivity.this, list);
                rvArticles.setAdapter(articleAdapter);
            }

            @Override
            public void onError(String err) {
                Toast.makeText(HomeActivity.this,
                        "Không tải được bài viết", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 🔥 LOAD FOLLOW → RỒI MỚI LOAD CATEGORY
     */
    private void loadFollowThenCategories() {
        followRepo.getFollowed(userId, new FollowCategoryRepository.Listener() {
            @Override
            public void onResult(HashMap<String, Boolean> map) {
                followMap = map;
                loadCategories();
            }

            @Override
            public void onError(String err) {
                followMap.clear();
                loadCategories();
            }
        });
    }

    private void loadCategories() {
        categoryRepo.getCategories(new CategoryRepository.Callback() {
            @Override
            public void onSuccess(List<Category> list) {
                // Kiểm tra nếu adapter chưa khởi tạo thì tạo mới
                if (categoryAdapter == null) {
                    categoryAdapter = new CategoryAdapter(
                            HomeActivity.this,
                            list,
                            followMap, // Map đã lấy từ loadFollowThenCategories
                            userId
                    );
                    rvTopics.setAdapter(categoryAdapter);
                } else {
                    // Nếu adapter đã có, chỉ cập nhật dữ liệu để tránh nháy màn hình
                    // Bạn cần thêm hàm setList trong Adapter nếu chưa có,
                    // hoặc tạm thời gán list trực tiếp nếu biến là public (không khuyến khích)
                    // Nhưng quan trọng nhất là cập nhật MAP:
                    categoryAdapter.updateFollowMap(followMap);

                    // Nếu danh sách category thay đổi, bạn nên update cả list category
                    // categoryAdapter.updateList(list); // Cần viết thêm hàm này trong Adapter
                    // categoryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String err) {
                Toast.makeText(HomeActivity.this,
                        "Không tải được chủ đề", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ======================================================
    // TAB UI
    // ======================================================
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

    // ======================================================
    // BOTTOM NAVIGATION
    // ======================================================
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            Intent intent = null;

            if (item.getItemId() == R.id.nav_search) {
                intent = new Intent(this, SearchActivity.class);
            } else if (item.getItemId() == R.id.nav_saved) {
                intent = new Intent(this, SavedActivity.class);
            } else if (item.getItemId() == R.id.nav_account) {
                intent = new Intent(this, ProfileActivity.class);
            } else {
                return true;
            }

            startActivity(intent);
            overridePendingTransition(0, 0);
            return true;
        });
    }
}
