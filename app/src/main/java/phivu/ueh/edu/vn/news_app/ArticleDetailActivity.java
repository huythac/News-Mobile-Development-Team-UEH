package phivu.ueh.edu.vn.news_app;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import phivu.ueh.edu.vn.news_app.article.Article;
import phivu.ueh.edu.vn.news_app.article.ArticleRepository;

public class ArticleDetailActivity extends AppCompatActivity {

    private int articleId;
    private ArticleRepository articleRepository;

    // Khai báo View
    private ImageView btnBack, imgDetailThumb, btnSave, btnShare;
    private TextView tvDetailTag, tvDetailTitle, tvDetailSummary, tvAuthorName, tvDetailDate, tvDetailContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        // 1. Ánh xạ View (Khớp với ID trong XML)
        btnBack = findViewById(R.id.btnBack);
        imgDetailThumb = findViewById(R.id.imgDetailThumb);
        tvDetailTag = findViewById(R.id.tvDetailTag);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailSummary = findViewById(R.id.tvDetailSummary);
        tvAuthorName = findViewById(R.id.tvAuthorName);
        tvDetailDate = findViewById(R.id.tvDetailDate);
        tvDetailContent = findViewById(R.id.tvDetailContent);
        btnSave = findViewById(R.id.btnSave);
        btnShare = findViewById(R.id.btnShare);

        // 2. Nhận ID bài viết từ MainActivity gửi sang
        articleId = getIntent().getIntExtra("ARTICLE_ID", -1);

        // 3. Lấy dữ liệu từ Database
        articleRepository = new ArticleRepository(this);
        loadArticleData(articleId);

        // 4. Xử lý sự kiện Click
        btnBack.setOnClickListener(v -> finish()); // Quay lại

        btnSave.setOnClickListener(v ->
                Toast.makeText(this, "Đã lưu vào danh sách đọc sau!", Toast.LENGTH_SHORT).show()
        );

        btnShare.setOnClickListener(v ->
                Toast.makeText(this, "Đã sao chép liên kết!", Toast.LENGTH_SHORT).show()
        );
    }

    private void loadArticleData(int id) {
        if (id == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy bài viết", Toast.LENGTH_SHORT).show();
            return;
        }

        Article article = articleRepository.getById(id);
        if (article != null) {
            // Đổ dữ liệu vào View
            tvDetailTitle.setText(article.getTitle());
            tvDetailSummary.setText(article.getDescription());
            tvDetailContent.setText(article.getContent());
            tvDetailDate.setText(article.getCreatedAt());

            // Hardcode tạm tên tác giả (hoặc lấy từ UserDAO nếu bạn đã join bảng)
            tvAuthorName.setText("Pham Thanh Dat");

            // Hình ảnh (Dùng tạm ảnh mặc định)
            imgDetailThumb.setImageResource(R.drawable.ic_launcher_background);
        } else {
            Toast.makeText(this, "Bài viết đã bị xóa hoặc không tồn tại", Toast.LENGTH_SHORT).show();
        }
    }
}