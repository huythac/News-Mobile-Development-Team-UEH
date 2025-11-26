package phivu.ueh.edu.vn.news_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.List;

import phivu.ueh.edu.vn.news_app.data.Database;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArticleAdapter adapter;
    private List<Article> articleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewArticles);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // KẾT NỐI DATABASE
        Database db = new Database(this);

        // LẤY DỮ LIỆU THẬT TRONG SQLITE

        // HIỂN THỊ LÊN MÀN HÌNH
        adapter = new ArticleAdapter(articleList);
        recyclerView.setAdapter(adapter);
    }
}
