package phivu.ueh.edu.vn.news_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import phivu.ueh.edu.vn.news_app.data.repository.ArticleRepository;
import phivu.ueh.edu.vn.news_app.model.Article;

public class ArticleDetailActivity extends AppCompatActivity {

    // Views
    private ImageView imgHeader;
    private TextView tvTitle, tvAuthor, tvDate, tvDescription, tvContent;
    private com.google.android.material.chip.Chip chipCategory;
    private View dividerContent;

    // Bottom bar buttons
    private LinearLayout btnComment, btnSave, btnShare;
    private ImageView iconSave;

    // Loading/Error states
    private ProgressBar progressLoading;
    private LinearLayout layoutError;
    private TextView tvError;
    private MaterialButton btnRetry;
    private View nestedScrollView;

    // Data
    private ArticleRepository articleRepo;
    private String currentArticleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        // Get ID from Intent
        currentArticleId = getIntent().getStringExtra("articleId");
        if (currentArticleId == null) {
            // Fallback for older code if any
            currentArticleId = String.valueOf(getIntent().getIntExtra("ARTICLE_ID", -1));
            if (currentArticleId.equals("-1")) {
                Toast.makeText(this, "Không tìm thấy bài viết", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        initViews();
        initRepositories();
        setupToolbar();
        setupBottomBar();
        setupRetryButton();

        loadArticleData();
    }

    private void initViews() {
        // Content views
        imgHeader = findViewById(R.id.imgHeader);
        tvTitle = findViewById(R.id.tvTitle);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvDate = findViewById(R.id.tvDate);
        chipCategory = findViewById(R.id.chipCategory);
        tvDescription = findViewById(R.id.tvDescription);
        dividerContent = findViewById(R.id.dividerContent);
        tvContent = findViewById(R.id.tvContent);
        nestedScrollView = findViewById(R.id.nestedScrollView);

        // Bottom bar
        btnComment = findViewById(R.id.btnComment);
        btnSave = findViewById(R.id.btnSave);
        btnShare = findViewById(R.id.btnShare);
        iconSave = findViewById(R.id.iconSave);

        // Loading/Error states
        progressLoading = findViewById(R.id.progressLoading);
        layoutError = findViewById(R.id.layoutError);
        tvError = findViewById(R.id.tvError);
        btnRetry = findViewById(R.id.btnRetry);
    }

    private void initRepositories() {
        articleRepo = new ArticleRepository(this);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.detailToolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupBottomBar() {
        if (btnComment != null) {
            btnComment.setOnClickListener(v -> 
                Toast.makeText(this, "Tính năng bình luận đang phát triển", Toast.LENGTH_SHORT).show());
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> 
                Toast.makeText(this, "Đã lưu bài viết", Toast.LENGTH_SHORT).show());
        }

        if (btnShare != null) {
            btnShare.setOnClickListener(v -> 
                Toast.makeText(this, "Đã sao chép liên kết", Toast.LENGTH_SHORT).show());
        }
    }

    private void setupRetryButton() {
        if (btnRetry != null) {
            btnRetry.setOnClickListener(v -> {
                hideErrorState();
                loadArticleData();
            });
        }
    }

    private void loadArticleData() {
        showLoadingState();
        articleRepo.getArticle(currentArticleId, new ArticleRepository.SingleCallback() {
            @Override
            public void onSuccess(Article article) {
                runOnUiThread(() -> {
                    if (article != null) {
                        bindArticleData(article);
                        hideLoadingState();
                    } else {
                        showErrorState("Bài viết không tồn tại");
                    }
                });
            }

            @Override
            public void onError(String err) {
                runOnUiThread(() -> showErrorState("Lỗi: " + err));
            }
        });
    }

    private void bindArticleData(Article article) {
        tvTitle.setText(article.getTitle());
        tvAuthor.setText(article.getAuthorName() != null ? article.getAuthorName() : "Tác giả");
        
        // Format date
        if (article.getPublishDate() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvDate.setText(sdf.format(new Date(article.getPublishDate())));
        } else {
            tvDate.setText("—");
        }

        tvContent.setText(article.getContent());

        // Load Image
        if (article.getImage() != null && !article.getImage().isEmpty()) {
            Picasso.get().load(article.getImage())
                    .placeholder(android.R.drawable.ic_menu_report_image)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(imgHeader);
        }

        // Description
        if (article.getDescription() != null && !article.getDescription().isEmpty()) {
            tvDescription.setText(article.getDescription());
            tvDescription.setVisibility(View.VISIBLE);
            if (dividerContent != null) dividerContent.setVisibility(View.VISIBLE);
        } else {
            tvDescription.setVisibility(View.GONE);
            if (dividerContent != null) dividerContent.setVisibility(View.GONE);
        }
    }

    private void showLoadingState() {
        if (progressLoading != null) progressLoading.setVisibility(View.VISIBLE);
        if (nestedScrollView != null) nestedScrollView.setVisibility(View.GONE);
        if (layoutError != null) layoutError.setVisibility(View.GONE);
    }

    private void hideLoadingState() {
        if (progressLoading != null) progressLoading.setVisibility(View.GONE);
        if (nestedScrollView != null) nestedScrollView.setVisibility(View.VISIBLE);
    }

    private void showErrorState(String message) {
        if (progressLoading != null) progressLoading.setVisibility(View.GONE);
        if (nestedScrollView != null) nestedScrollView.setVisibility(View.GONE);
        if (layoutError != null) layoutError.setVisibility(View.VISIBLE);
        if (tvError != null) tvError.setText(message);
    }

    private void hideErrorState() {
        if (layoutError != null) layoutError.setVisibility(View.GONE);
    }
}
