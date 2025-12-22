package phivu.ueh.edu.vn.news_app.ui.profile;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.data.remote.UserFirebaseDAO;
import phivu.ueh.edu.vn.news_app.data.repository.ArticleRepository;
import phivu.ueh.edu.vn.news_app.model.Article;
import phivu.ueh.edu.vn.news_app.model.User;
import phivu.ueh.edu.vn.news_app.ui.base.BaseActivity;
import phivu.ueh.edu.vn.news_app.ui.main.ArticleAdapter; // Dùng adapter bài viết
import phivu.ueh.edu.vn.news_app.ui.main.HomeActivity;
import phivu.ueh.edu.vn.news_app.ui.saved.SavedActivity;
import phivu.ueh.edu.vn.news_app.ui.search.SearchActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.content.Intent;
import android.view.View;
import phivu.ueh.edu.vn.news_app.ui.create.CreateArticleActivity;


public class ProfileActivity extends BaseActivity {

    // Views Profile cũ
    private TextView tvName, tvBio, tvWriteBio;
    private LinearLayout layoutEmpty;
    private ImageView imgAvatar;

    // Views Tabs & Content mới
    private LinearLayout tabInfo, tabArticles;
    private TextView tvTabInfoTitle, tvTabArticlesTitle;
    private View underlineInfo, underlineArticles;
    private LinearLayout layoutInfoContent; // Chứa Bio và Empty State
    private RecyclerView rvMyArticles;
    private ProgressBar progressArticles;

    // Data
    private UserFirebaseDAO userDAO;
    private ArticleRepository articleRepo;
    private ArticleAdapter articleAdapter;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        currentUid = FirebaseAuth.getInstance().getUid();
        userDAO = new UserFirebaseDAO();
        articleRepo = new ArticleRepository(this); // Khởi tạo Repository

        initViews();
        setupEvents();
        setupBottomNavigation();

        // 1. Ánh xạ nút FAB từ XML
        FloatingActionButton fabEditBio = findViewById(R.id.fabEditBio);

        // (Tùy chọn) Nếu muốn hiện nút này luôn để test (kể cả khi chưa check quyền Admin)
        fabEditBio.setVisibility(View.VISIBLE);

        // 2. Bắt sự kiện Click -> Chuyển sang màn hình CreateArticleActivity
        fabEditBio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, CreateArticleActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initViews() {
        // Ánh xạ Profile info
        tvName = findViewById(R.id.tvProfileName);
        tvBio = findViewById(R.id.tvUserBio);
        tvWriteBio = findViewById(R.id.tvWriteBio);
        layoutEmpty = findViewById(R.id.layoutEmptyState);
        imgAvatar = findViewById(R.id.imgAvatarProfile);

        // Ánh xạ Tabs
        tabInfo = findViewById(R.id.tabInfo);
        tabArticles = findViewById(R.id.tabArticles);
        tvTabInfoTitle = findViewById(R.id.tvTabInfoTitle);
        tvTabArticlesTitle = findViewById(R.id.tvTabArticlesTitle);
        underlineInfo = findViewById(R.id.underlineInfo);
        underlineArticles = findViewById(R.id.underlineArticles);

        // Ánh xạ Content
        layoutInfoContent = findViewById(R.id.layoutInfoContent);
        rvMyArticles = findViewById(R.id.rvMyArticles);
        progressArticles = findViewById(R.id.progressArticles);

        // Setup RecyclerView
        rvMyArticles.setLayoutManager(new LinearLayoutManager(this));
        // Khởi tạo adapter rỗng trước
        articleAdapter = new ArticleAdapter(this, new ArrayList<>());
        rvMyArticles.setAdapter(articleAdapter);
    }

    private void setupEvents() {
        // Settings & Edit Profile
        findViewById(R.id.btnSettings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        View.OnClickListener goToEditScreen = v ->
                startActivity(new Intent(this, EditProfileActivity.class));

        findViewById(R.id.btnEditProfile).setOnClickListener(goToEditScreen);
        findViewById(R.id.fabEditBio).setOnClickListener(goToEditScreen);
        tvWriteBio.setOnClickListener(goToEditScreen);

        // Xử lý Click Tabs
        tabInfo.setOnClickListener(v -> switchTab(true));
        tabArticles.setOnClickListener(v -> switchTab(false));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUid != null) {
            loadUserData();
        }
    }

    // =========================
    // LOGIC TAB & BÀI VIẾT
    // =========================

    // Hàm chuyển đổi giữa Tab Thông tin và Bài viết
    private void switchTab(boolean showInfo) {
        // Get theme-aware colors
        int colorActive = ContextCompat.getColor(this, R.color.text_primary);
        int colorInactive = ContextCompat.getColor(this, R.color.text_tertiary);
        
        if (showInfo) {
            // UI Tab: Chọn Info
            tvTabInfoTitle.setTypeface(null, Typeface.BOLD);
            tvTabInfoTitle.setTextColor(colorActive);
            underlineInfo.setVisibility(View.VISIBLE);

            tvTabArticlesTitle.setTypeface(null, Typeface.NORMAL);
            tvTabArticlesTitle.setTextColor(colorInactive);
            underlineArticles.setVisibility(View.INVISIBLE);

            // Content: Hiện Info, ẩn Articles
            layoutInfoContent.setVisibility(View.VISIBLE);
            rvMyArticles.setVisibility(View.GONE);
            progressArticles.setVisibility(View.GONE);
        } else {
            // UI Tab: Chọn Articles
            tvTabInfoTitle.setTypeface(null, Typeface.NORMAL);
            tvTabInfoTitle.setTextColor(colorInactive);
            underlineInfo.setVisibility(View.INVISIBLE);

            tvTabArticlesTitle.setTypeface(null, Typeface.BOLD);
            tvTabArticlesTitle.setTextColor(colorActive);
            underlineArticles.setVisibility(View.VISIBLE);

            // Content: Ẩn Info, hiện Articles
            layoutInfoContent.setVisibility(View.GONE);
            rvMyArticles.setVisibility(View.VISIBLE);

            // Tải dữ liệu bài viết
            loadMyArticles();
        }
    }

    private void loadMyArticles() {
        if (currentUid == null) return;

        progressArticles.setVisibility(View.VISIBLE);
        rvMyArticles.setVisibility(View.GONE);

        // Gọi hàm getArticlesByAuthor như bạn yêu cầu
        // Tham số: authorId, excludeArticleId (null), limit (50), callback
        articleRepo.getArticlesByAuthor(currentUid, null, 50, new ArticleRepository.ListCallback() {
            @Override
            public void onSuccess(List<Article> list) {
                progressArticles.setVisibility(View.GONE);

                if (list != null && !list.isEmpty()) {
                    rvMyArticles.setVisibility(View.VISIBLE);
                    articleAdapter.updateData(list);
                } else {
                    // Nếu không có bài viết, có thể hiện thông báo hoặc để trống
                    Toast.makeText(ProfileActivity.this, "Bạn chưa có bài viết nào", Toast.LENGTH_SHORT).show();
                    rvMyArticles.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String err) {
                progressArticles.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, "Lỗi tải bài viết: " + err, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =========================
    // LOAD USER PROFILE (Giữ nguyên logic cũ)
    // =========================
    private void loadUserData() {
        userDAO.getUser(currentUid, new UserFirebaseDAO.SingleListener() {
            @Override
            public void onLoaded(User user) {
                if (user.getFullName() != null) {
                    tvName.setText(user.getFullName());
                }

                if (user.getBio() != null && !user.getBio().isEmpty()) {
                    tvBio.setText(user.getBio());
                    tvBio.setVisibility(View.VISIBLE);
                    layoutEmpty.setVisibility(View.GONE);
                } else {
                    tvBio.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                }

                String avatarUrl = user.getAvatar();
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Picasso.get()
                            .load(avatarUrl)
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.mipmap.ic_launcher)
                            .fit().centerCrop()
                            .into(imgAvatar);
                }
            }

            @Override
            public void onError(String err) {
                // Handle error
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_account);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_account) return true;
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish(); return true;
            } else if (id == R.id.nav_search) {
                startActivity(new Intent(this, SearchActivity.class));
                finish(); return true;
            } else if (id == R.id.nav_saved) {
                startActivity(new Intent(this, SavedActivity.class));
                finish(); return true;
            }
            return false;
        });
    }
}