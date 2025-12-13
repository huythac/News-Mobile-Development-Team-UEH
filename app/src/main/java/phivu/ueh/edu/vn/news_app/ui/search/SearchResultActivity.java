package phivu.ueh.edu.vn.news_app.ui.search;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.ui.main.ArticleAdapter;
import phivu.ueh.edu.vn.news_app.data.repository.ArticleRepository;
import phivu.ueh.edu.vn.news_app.data.repository.CategoryRepository;
import phivu.ueh.edu.vn.news_app.model.Article;
import phivu.ueh.edu.vn.news_app.model.Category;
import phivu.ueh.edu.vn.news_app.ui.search.SearchInputActivity;


public class SearchResultActivity extends AppCompatActivity {

    private RecyclerView rvResults;
    private LinearLayout layoutNoResult;
    private TextView tvSearchQuery, tvTryAgain;

    private ArticleRepository articleRepo;
    private CategoryRepository categoryRepo;
    private ArticleAdapter adapter;

    // Biến này dùng để tra cứu: ID -> Tên Category
    // Ví dụ: "-Ndkj123..." -> "Nghiên cứu AI"
    private Map<String, String> categoryNameMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        // 1. Ánh xạ View
        rvResults = findViewById(R.id.rvSearchResults);
        layoutNoResult = findViewById(R.id.layoutNoResult);
        tvSearchQuery = findViewById(R.id.tvSearchQuery);
        tvTryAgain = findViewById(R.id.tvTryAgain);

        rvResults.setLayoutManager(new LinearLayoutManager(this));

        // 2. Nút Back và Nút X
        View.OnClickListener closeAction = v -> finish();
        findViewById(R.id.btnBackResult).setOnClickListener(closeAction);

        View btnClear = findViewById(R.id.btnClearResult);
        if (btnClear != null) btnClear.setOnClickListener(closeAction);

        if (tvTryAgain != null) tvTryAgain.setOnClickListener(closeAction);

        // 3. Khởi tạo Repo
        articleRepo = new ArticleRepository(this);
        categoryRepo = new CategoryRepository(this);

        // 4. Lấy từ khóa và Bắt đầu quy trình tìm kiếm
        String query = getIntent().getStringExtra("SEARCH_QUERY");
        if (query != null) {
            tvSearchQuery.setText(query);
            // BƯỚC QUAN TRỌNG: Tải Category trước, sau đó mới tìm bài viết
            prepareCategoriesAndSearch(query);
        }
    }

    // Hàm này tải danh sách chủ đề về để biết ID nào ứng với Tên nào
    private void prepareCategoriesAndSearch(String keyword) {
        categoryRepo.getCategories(new CategoryRepository.Callback() {
            @Override
            public void onSuccess(List<Category> list) {
                // Đổ dữ liệu vào Map để tra cứu nhanh
                categoryNameMap.clear();
                for (Category c : list) {
                    if (c.getId() != null && c.getName() != null) {
                        categoryNameMap.put(c.getId(), c.getName().toLowerCase());
                    }
                }

                // Sau khi đã có danh sách chủ đề, tiến hành tải và lọc bài viết
                searchArticles(keyword);
            }

            @Override
            public void onError(String err) {
                // Nếu lỗi tải category, vẫn cố gắng tìm kiếm (nhưng chỉ tìm theo tiêu đề bài viết)
                searchArticles(keyword);
            }
        });
    }

    private void searchArticles(String keyword) {
        articleRepo.getList(new ArticleRepository.ListCallback() {
            @Override
            public void onSuccess(List<Article> list) {
                List<Article> filteredList = new ArrayList<>();
                String key = keyword.toLowerCase().trim();

                for (Article article : list) {
                    boolean isMatch = false;

                    // 1. Kiểm tra Tiêu đề (Title)
                    if (article.getTitle() != null && article.getTitle().toLowerCase().contains(key)) {
                        isMatch = true;
                    }

                    // 2. Kiểm tra Chủ đề (Dựa vào categoryId map sang Name)
                    if (!isMatch && article.getCategoryId() != null) {
                        // Lấy tên chủ đề từ Map dựa vào ID
                        String catName = categoryNameMap.get(article.getCategoryId());

                        // Nếu tìm thấy tên chủ đề và tên đó chứa từ khóa
                        if (catName != null && catName.contains(key)) {
                            isMatch = true;
                        }
                    }

                    if (isMatch) {
                        filteredList.add(article);
                    }
                }

                updateUI(filteredList);
            }

            @Override
            public void onError(String err) {
                updateUI(new ArrayList<>());
                Toast.makeText(SearchResultActivity.this, "Lỗi tải bài viết: " + err, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(List<Article> list) {
        if (list.isEmpty()) {
            rvResults.setVisibility(View.GONE);
            layoutNoResult.setVisibility(View.VISIBLE);
        } else {
            rvResults.setVisibility(View.VISIBLE);
            layoutNoResult.setVisibility(View.GONE);
            adapter = new ArticleAdapter(SearchResultActivity.this, list);
            rvResults.setAdapter(adapter);
        }
    }
}