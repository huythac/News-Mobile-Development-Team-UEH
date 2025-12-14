package phivu.ueh.edu.vn.news_app.ui.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    private MaterialButton btnFollow;

    private HashMap<String, Boolean> followMap = new HashMap<>();

    // =========================
    // LIFECYCLE
    // =========================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);

        // ===== AUTH =====
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        userId = user.getUid();
        Log.d("UID_CHECK", "AUTH UID = " + userId);

        // ===== INTENT =====
        categoryId = getIntent().getStringExtra("categoryId");
        categoryName = getIntent().getStringExtra("categoryName");

        if (categoryId == null) {
            Toast.makeText(this, "Thiếu categoryId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ===== REPO =====
        articleRepo = new ArticleRepository(this);
        followRepo = new FollowCategoryRepository(this);

        // ===== VIEW =====
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvCategoryName = findViewById(R.id.tvCategoryName);
        btnFollow = findViewById(R.id.btnFollowCategory);

        rvArticles = findViewById(R.id.rvCategoryArticles);
        rvArticles.setLayoutManager(new LinearLayoutManager(this));

        tvCategoryName.setText(categoryName);

        btnBack.setOnClickListener(v -> finish());
        btnFollow.setOnClickListener(v -> toggleFollow());

        loadCategoryArticles();
        loadCategoryStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFollowState(); // luôn reload từ Firestore
    }

    // =========================
    // FOLLOW STATE
    // =========================
    private void loadFollowState() {
        followRepo.getFollowed(userId, new FollowCategoryRepository.Listener() {
            @Override
            public void onResult(HashMap<String, Boolean> map) {
                followMap = map;

                Log.d("FOLLOW_DEBUG", "FOLLOW MAP = " + followMap);
                Log.d("FOLLOW_DEBUG", "CATEGORY ID = " + categoryId);

                updateFollowButton();
            }

            @Override
            public void onError(String err) {
                Log.e("FOLLOW_DEBUG", "ERROR = " + err);
            }
        });
    }

    private void updateFollowButton() {
        boolean isFollowed = followMap.get(categoryId) != null;

        if (isFollowed) {
            btnFollow.setText("Đang theo dõi");
            btnFollow.setTextColor(Color.WHITE);
            btnFollow.setBackgroundTintList(
                    getColorStateList(R.color.black)
            );
        } else {
            btnFollow.setText("Theo dõi");
            btnFollow.setTextColor(Color.BLACK);
            btnFollow.setBackgroundTintList(
                    getColorStateList(R.color.white)
            );
        }
    }

    private void toggleFollow() {
        boolean isFollowed = followMap.get(categoryId) != null;

        if (isFollowed) {
            followRepo.unfollow(userId, categoryId, this::loadFollowState);
        } else {
            followRepo.follow(userId, categoryId, this::loadFollowState);
        }
    }

    // =========================
    // ARTICLES
    // =========================
    private void loadCategoryArticles() {
        articleRepo.getArticlesByCategory(categoryId,
                new ArticleRepository.ListCallback() {
                    @Override
                    public void onSuccess(List<Article> list) {
                        articleAdapter =
                                new ArticleAdapter(CategoryDetailActivity.this, list);
                        rvArticles.setAdapter(articleAdapter);
                    }

                    @Override
                    public void onError(String err) {
                        Toast.makeText(
                                CategoryDetailActivity.this,
                                "Không tải được bài viết",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    // =========================
    // STATS
    // =========================
    private void loadCategoryStats() {
        TextView tvStats = findViewById(R.id.tvCategoryDescription);

        FirebaseFirestore.getInstance()
                .collection("articles")
                .whereEqualTo("categoryId", categoryId)
                .get()
                .addOnSuccessListener(query ->
                        tvStats.setText(query.size() + " bài viết")
                )
                .addOnFailureListener(e ->
                        tvStats.setText("0 bài viết")
                );
    }
}
