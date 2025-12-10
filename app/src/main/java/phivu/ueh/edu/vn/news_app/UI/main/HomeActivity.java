package phivu.ueh.edu.vn.news_app.UI.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import java.util.List;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.data.repository.ArticleRepository;
import phivu.ueh.edu.vn.news_app.model.Article;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvArticles;
    private ArticleAdapter adapter;
    private ArticleRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        rvArticles = findViewById(R.id.recyclerViewArticles);
        rvArticles.setLayoutManager(new LinearLayoutManager(this));

        repo = new ArticleRepository(this);

        loadArticles();
    }

    private void loadArticles() {
        repo.getList(new ArticleRepository.ListCallback() {
            @Override
            public void onSuccess(List<Article> list) {
                adapter = new ArticleAdapter(HomeActivity.this, list);
                rvArticles.setAdapter(adapter);
            }

            @Override
            public void onError(String err) {
                Toast.makeText(HomeActivity.this, "Không tải được dữ liệu: " + err,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
