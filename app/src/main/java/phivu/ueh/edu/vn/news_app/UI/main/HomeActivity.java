package phivu.ueh.edu.vn.news_app.UI.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import phivu.ueh.edu.vn.news_app.R;
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

        // ===== ÁNH XẠ TAB =====
        tvForYou = findViewById(R.id.tvForYou);
        tvTopic = findViewById(R.id.tvTopic);

        underlineForYou = findViewById(R.id.viewUnderlineForYou);
        underlineTopic = findViewById(R.id.viewUnderlineTopic);

        // ===== ÁNH XẠ LIST =====
        rvArticles = findViewById(R.id.recyclerViewArticles);
        rvArticles.setLayoutManager(new LinearLayoutManager(this));

        rvTopics = findViewById(R.id.recyclerViewTopics);
        rvTopics.setLayoutManager(new LinearLayoutManager(this));
        rvTopics.setVisibility(View.GONE);

        repo = new ArticleRepository(this);
        categoryRepo = new CategoryRepository(this);

        // load bài báo mặc định
        loadArticles();

        tvForYou.setOnClickListener(v -> {
            setTabSelected(true);
            showArticles();
        });

        tvTopic.setOnClickListener(v -> {
            setTabSelected(false);
            showTopics();
            loadCategories(); // <<< QUAN TRỌNG NHẤT
        });

        setTabSelected(true);
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
    // LOAD CATEGORY FROM FIREBASE + LOCAL CACHE
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
    // TAB UI
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
