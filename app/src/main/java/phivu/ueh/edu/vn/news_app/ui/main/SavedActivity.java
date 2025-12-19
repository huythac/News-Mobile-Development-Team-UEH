package phivu.ueh.edu.vn.news_app.ui.main;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat; // Thêm thư viện này để lấy màu chuẩn

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.data.local.history.ReadHistoryDAO;
import phivu.ueh.edu.vn.news_app.data.repository.SavedArticleRepository;
import phivu.ueh.edu.vn.news_app.model.Article;

public class SavedActivity extends AppCompatActivity {

    // Tabs
    private LinearLayout tabSaved, tabHistory;
    private TextView tvTabSaved, tvTabHistory;
    private View viewTabSavedIndicator, viewTabHistoryIndicator;

    // RecyclerView
    private RecyclerView rvArticles;
    private ArticleAdapter adapter;

    // Empty states
    private View layoutEmptySaved, layoutEmptyHistory;

    // Repo / DAO
    private SavedArticleRepository savedRepo;
    private ReadHistoryDAO historyDAO;

    // State
    private boolean showingSavedTab = true;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);

        userId = FirebaseAuth.getInstance().getUid();

        initViews();
        initData();
        initRecyclerView();
        initTabs();

        // Không gọi loadSavedTab() ở đây nữa vì onResume() sẽ tự gọi
        // Để tránh load 2 lần gây lag
    }

    // =========================
    // INIT
    // =========================
    private void initViews() {
        tabSaved = findViewById(R.id.tabSaved);
        tabHistory = findViewById(R.id.tabHistory);

        tvTabSaved = findViewById(R.id.tvTabSaved);
        tvTabHistory = findViewById(R.id.tvTabHistory);

        viewTabSavedIndicator = findViewById(R.id.viewTabSavedIndicator);
        viewTabHistoryIndicator = findViewById(R.id.viewTabHistoryIndicator);

        rvArticles = findViewById(R.id.rvSavedArticles);

        layoutEmptySaved = findViewById(R.id.layoutEmptyStateSaved);
        layoutEmptyHistory = findViewById(R.id.layoutEmptyStateHistory);
    }

    private void initData() {
        // Khởi tạo Repo (Quan trọng: Truyền context 'this' vào)
        savedRepo = new SavedArticleRepository(this);
        historyDAO = new ReadHistoryDAO(this);
    }

    private void initRecyclerView() {
        rvArticles.setLayoutManager(new LinearLayoutManager(this));
        rvArticles.setHasFixedSize(true);
    }

    private void initTabs() {
        tabSaved.setOnClickListener(v -> loadSavedTab());
        tabHistory.setOnClickListener(v -> loadHistoryTab());
    }

    // =========================
    // LOAD SAVED (ĐÃ TỐI ƯU)
    // =========================
    private void loadSavedTab() {
        showingSavedTab = true;
        updateTabUI();

        // BƯỚC 1: Hiển thị dữ liệu từ Local NGAY LẬP TỨC (Không chờ mạng)
        showSavedFromLocal();

        // BƯỚC 2: Nếu có mạng & User, chạy Sync ngầm để lấy dữ liệu mới
        if (userId != null) {
            savedRepo.syncFromRemote(userId, () -> {
                // Sync xong -> Load lại từ local để cập nhật danh sách mới nhất
                // (Chạy trên UI Thread để an toàn)
                runOnUiThread(this::showSavedFromLocal);
            });
        }
    }

    private void showSavedFromLocal() {
        List<Article> list = savedRepo.getSavedArticlesLocal();
        showSavedUI(list);
    }

    private void showSavedUI(List<Article> list) {
        if (list == null || list.isEmpty()) {
            rvArticles.setVisibility(View.GONE);
            layoutEmptySaved.setVisibility(View.VISIBLE);
            layoutEmptyHistory.setVisibility(View.GONE);
        } else {
            rvArticles.setVisibility(View.VISIBLE);
            layoutEmptySaved.setVisibility(View.GONE);
            layoutEmptyHistory.setVisibility(View.GONE);

            // Tạo Adapter mới
            adapter = new ArticleAdapter(
                    this,
                    list,
                    ArticleAdapter.Mode.NORMAL
            );
            rvArticles.setAdapter(adapter);
        }
    }

    // =========================
    // LOAD HISTORY
    // =========================
    private void loadHistoryTab() {
        showingSavedTab = false;
        updateTabUI();

        // Đảm bảo historyDAO không null (dù đã init, check cho chắc)
        if (historyDAO == null) historyDAO = new ReadHistoryDAO(this);

        List<Article> list = historyDAO.getReadHistory();

        if (list == null || list.isEmpty()) {
            rvArticles.setVisibility(View.GONE);
            layoutEmptySaved.setVisibility(View.GONE);
            layoutEmptyHistory.setVisibility(View.VISIBLE);
        } else {
            rvArticles.setVisibility(View.VISIBLE);
            layoutEmptySaved.setVisibility(View.GONE);
            layoutEmptyHistory.setVisibility(View.GONE);

            adapter = new ArticleAdapter(
                    this,
                    list,
                    ArticleAdapter.Mode.HISTORY
            );
            rvArticles.setAdapter(adapter);
        }
    }

    // =========================
    // TAB UI
    // =========================
    private void updateTabUI() {
        // Dùng ContextCompat để lấy màu (thay cho getColor bị lỗi thời)
        int colorBlack = ContextCompat.getColor(this, R.color.black); // Hoặc android.R.color.black
        int colorGray = ContextCompat.getColor(this, R.color.article_text_secondary); // Hoặc màu xám mặc định

        if (showingSavedTab) {
            tvTabSaved.setTextColor(colorBlack);
            tvTabHistory.setTextColor(colorGray);
            viewTabSavedIndicator.setVisibility(View.VISIBLE);
            viewTabHistoryIndicator.setVisibility(View.INVISIBLE);
        } else {
            tvTabSaved.setTextColor(colorGray);
            tvTabHistory.setTextColor(colorBlack);
            viewTabSavedIndicator.setVisibility(View.INVISIBLE);
            viewTabHistoryIndicator.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload dữ liệu mỗi khi quay lại màn hình
        if (showingSavedTab) {
            loadSavedTab();
        } else {
            loadHistoryTab();
        }
    }
}