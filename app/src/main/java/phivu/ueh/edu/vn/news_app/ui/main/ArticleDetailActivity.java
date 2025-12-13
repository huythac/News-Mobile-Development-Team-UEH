package phivu.ueh.edu.vn.news_app.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.squareup.picasso.Picasso;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.data.repository.ArticleRepository;
import phivu.ueh.edu.vn.news_app.model.Article;

public class ArticleDetailActivity extends AppCompatActivity {

    private ImageView imgDetail;
    private TextView tvDetailTitle, tvDetailContent;
    private ArticleRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        MaterialToolbar toolbar = findViewById(R.id.detailToolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        imgDetail = findViewById(R.id.imgDetail);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailContent = findViewById(R.id.tvDetailContent);

        repo = new ArticleRepository(this);

        String id = getIntent().getStringExtra("articleId");

        if (id == null || id.isEmpty()) {
            Toast.makeText(this, "Thiếu ID bài báo", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        repo.getArticle(id, new ArticleRepository.SingleCallback() {
            @Override
            public void onSuccess(Article a) {
                tvDetailTitle.setText(a.getTitle());
                tvDetailContent.setText(a.getContent());

                if (a.getImage() != null && !a.getImage().isEmpty()) {
                    Picasso.get().load(a.getImage())
                            .placeholder(android.R.drawable.ic_menu_report_image)
                            .into(imgDetail);
                }

                repo.markViewed(a);
            }

            @Override
            public void onError(String err) {
                Toast.makeText(ArticleDetailActivity.this,
                        "Không tải được bài báo: " + err,
                        Toast.LENGTH_SHORT).show();
            }
        });

        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(ArticleDetailActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

}
