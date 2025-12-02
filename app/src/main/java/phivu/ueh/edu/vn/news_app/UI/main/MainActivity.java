package phivu.ueh.edu.vn.news_app.UI.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.data.local.Database;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArticleAdapter adapter;

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
        recyclerView.setAdapter(adapter);
    }
}
