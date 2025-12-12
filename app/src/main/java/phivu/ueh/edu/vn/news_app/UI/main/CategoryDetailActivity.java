package phivu.ueh.edu.vn.news_app.UI.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.List;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.data.repository.ArticleRepository;
import phivu.ueh.edu.vn.news_app.data.repository.FollowCategoryRepository;
import phivu.ueh.edu.vn.news_app.model.Article;

public class CategoryDetailActivity extends AppCompatActivity {

    private String categoryId;
    private String categoryName;

    private ArticleRepository articleRepo;
    private FollowCategoryRepository followRepo;

    private RecyclerView rvArticles;
    private ArticleAdapter articleAdapter;

    private MaterialButton btnFollow;

    private HashMap<String, Boolean> followMap = new HashMap<>();
    private final String userId = "123";  // TODO: FirebaseAuth.getUid()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);   // FIX

        // Nhận dữ liệu từ Adapter
        categoryId = getIntent().getStringExtra("categoryId");
        categoryName = getIntent().getStringExtra("categoryName");

        if (categoryId == null) {
            Toast.makeText(this, "Thiếu categoryId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Repo
        articleRepo = new ArticleRepository(this);
        followRepo = new FollowCategoryRepository(this);

        // Ánh xạ view
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvCategoryName = findViewById(R.id.tvCategoryName);
        TextView tvStats = findViewById(R.id.tvCategoryStats);
        btnFollow = findViewById(R.id.btnFollowCategory);

        rvArticles = findViewById(R.id.rvCategoryArticles);
        rvArticles.setLayoutManager(new LinearLayoutManager(this));

        // Set UI
        tvCategoryName.setText(categoryName);
        tvStats.setText("120 bài viết • 14k người theo dõi");

        btnBack.setOnClickListener(v -> finish());

        loadFollowState();
        loadCategoryArticles();

        btnFollow.setOnClickListener(v -> toggleFollow());
    }

    private void loadFollowState() {
        followRepo.getFollowed(userId, new FollowCategoryRepository.Listener() {
            @Override
            public void onResult(HashMap<String, Boolean> map) {
                followMap = map;
                updateFollowButton();
            }

            @Override
            public void onError(String err) {
                Toast.makeText(CategoryDetailActivity.this, err, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFollowButton() {
        boolean isFollowed = followMap.containsKey(categoryId);

        if (isFollowed) {
            btnFollow.setText("Đang theo dõi");
            btnFollow.setTextColor(Color.WHITE);
            btnFollow.setBackgroundTintList(getColorStateList(R.color.black));
        } else {
            btnFollow.setText("Theo dõi");
            btnFollow.setTextColor(Color.BLACK);
            btnFollow.setBackgroundTintList(getColorStateList(R.color.white));
        }
    }

    private void toggleFollow() {
        boolean isFollowed = followMap.containsKey(categoryId);

        if (isFollowed) {
            followRepo.unfollow(userId, categoryId);
            followMap.remove(categoryId);
        } else {
            followRepo.follow(userId, categoryId);
            followMap.put(categoryId, true);
        }

        updateFollowButton();
    }

    private void loadCategoryArticles() {
        articleRepo.getArticlesByCategory(categoryId, new ArticleRepository.ListCallback() {
            @Override
            public void onSuccess(List<Article> list) {
                articleAdapter = new ArticleAdapter(CategoryDetailActivity.this, list);
                rvArticles.setAdapter(articleAdapter);
            }

            @Override
            public void onError(String err) {
                Toast.makeText(CategoryDetailActivity.this, "Không tải được bài viết: " + err, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
