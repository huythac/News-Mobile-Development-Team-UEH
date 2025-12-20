package phivu.ueh.edu.vn.news_app.ui.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.data.repository.ArticleRepository;
import phivu.ueh.edu.vn.news_app.data.repository.FollowCategoryRepository;
import phivu.ueh.edu.vn.news_app.model.Article;

public class CategoryDetailActivity extends AppCompatActivity {

    private String categoryId;
    private String categoryName;
    private String userId;

    private ArticleRepository articleRepo;
    private FollowCategoryRepository followRepo;

    private RecyclerView rvArticles;
    private ArticleAdapter articleAdapter;

    private TextView tvCategoryName, tvCategoryStats;
    private MaterialButton btnFollow;
    private ImageView btnBack;

    private HashMap<String, Boolean> followMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        userId = user.getUid();

        categoryId = getIntent().getStringExtra("categoryId");
        categoryName = getIntent().getStringExtra("categoryName");

        if (categoryId == null) {
            Toast.makeText(this, "Thiếu categoryId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        articleRepo = new ArticleRepository(this);
        followRepo = new FollowCategoryRepository(this);

        initViews();

        setupHeaderData();
        loadCategoryArticles();
        loadFollowState();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        tvCategoryStats = findViewById(R.id.tvCategoryStats);
        btnFollow = findViewById(R.id.btnFollowCategory);

        rvArticles = findViewById(R.id.rvCategoryArticles);
        rvArticles.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());
        btnFollow.setOnClickListener(v -> toggleFollow());
    }

    private void setupHeaderData() {
        if (categoryName != null) {
            tvCategoryName.setText(categoryName);
        } else {
            tvCategoryName.setText("Chủ đề");
        }

        tvCategoryStats.setText("Đang tải dữ liệu...");

        FirebaseFirestore.getInstance()
                .collection("articles")
                .whereEqualTo("categoryId", categoryId)
                .get()
                .addOnSuccessListener(query -> {
                    int count = query.size();
                    // Giả lập số người theo dõi (vì DB chưa có trường này)
                    String stats = count + " bài viết • 1.2k người theo dõi";
                    tvCategoryStats.setText(stats);
                })
                .addOnFailureListener(e ->
                        tvCategoryStats.setText("0 bài viết")
                );
    }

    // =========================
    // XỬ LÝ FOLLOW (LOGIC NÚT THEO DÕI)
    // =========================
    private void loadFollowState() {
        followRepo.getFollowed(userId, new FollowCategoryRepository.Listener() {
            @Override
            public void onResult(HashMap<String, Boolean> map) {
                followMap = map;
                updateFollowButtonUI();
            }

            @Override
            public void onError(String err) {
                Log.e("FOLLOW", "Lỗi load follow: " + err);
            }
        });
    }

    private void updateFollowButtonUI() {
        boolean isFollowed = followMap != null && followMap.containsKey(categoryId);

        // Get theme-aware colors
        int colorNormalBackground = ContextCompat.getColor(this, R.color.follow_button_background);
        int colorNormalText = ContextCompat.getColor(this, R.color.follow_button_text);
        int colorNormalStroke = ContextCompat.getColor(this, R.color.follow_button_stroke);
        int colorSelectedBackground = ContextCompat.getColor(this, R.color.follow_button_selected_background);
        int colorSelectedText = ContextCompat.getColor(this, R.color.follow_button_selected_text);

        if (isFollowed) {
            btnFollow.setText("Đang theo dõi");
            btnFollow.setTextColor(colorSelectedText);
            btnFollow.setBackgroundTintList(ColorStateList.valueOf(colorSelectedBackground));
            btnFollow.setStrokeColor(ColorStateList.valueOf(colorSelectedBackground));
            btnFollow.setStrokeWidth(0);
        } else {
            btnFollow.setText("Theo dõi");
            btnFollow.setTextColor(colorNormalText);
            btnFollow.setBackgroundTintList(ColorStateList.valueOf(colorNormalBackground));
            btnFollow.setStrokeColor(ColorStateList.valueOf(colorNormalStroke));
            btnFollow.setStrokeWidth(1);
        }
    }

    private void toggleFollow() {
        boolean isFollowed = followMap != null && followMap.containsKey(categoryId);

        if (isFollowed) {
            followRepo.unfollow(userId, categoryId);
            followMap.remove(categoryId);
        } else {
            followRepo.follow(userId, categoryId);
            followMap.put(categoryId, true);
        }
        updateFollowButtonUI();
    }

    // =========================
    // LOAD DANH SÁCH BÀI VIẾT
    // =========================
    private void loadCategoryArticles() {
        articleRepo.getArticlesByCategory(categoryId, new ArticleRepository.ListCallback() {
            @Override
            public void onSuccess(List<Article> list) {
                articleAdapter = new ArticleAdapter(CategoryDetailActivity.this, list);
                rvArticles.setAdapter(articleAdapter);
            }

            @Override
            public void onError(String err) {
                Toast.makeText(CategoryDetailActivity.this, "Không tải được bài viết", Toast.LENGTH_SHORT).show();
            }
        });
    }
}