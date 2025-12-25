package phivu.ueh.edu.vn.news_app.ui.saved;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.data.repository.SavedArticleRepository;
import phivu.ueh.edu.vn.news_app.data.local.history.ReadHistoryDAO;
import phivu.ueh.edu.vn.news_app.model.Article;
import phivu.ueh.edu.vn.news_app.ui.main.HomeActivity;
import phivu.ueh.edu.vn.news_app.ui.profile.ProfileActivity;
import phivu.ueh.edu.vn.news_app.ui.search.SearchActivity;

public class SavedActivity extends AppCompatActivity {

    // Tabs
    private LinearLayout tabSaved, tabHistory;
    private TextView tvTabSaved, tvTabHistory;
    private View viewTabSavedIndicator, viewTabHistoryIndicator;

    // Content
    private RecyclerView rvArticles;
    private LinearLayout layoutEmptySaved, layoutEmptyHistory;

    // Adapter
    private SavedArticleAdapter adapter;

    // Data layer
    private SavedArticleRepository savedRepo;
    private ReadHistoryDAO historyDAO;

    // State
    private boolean isSavedTab = true;
    private String userId;

    private ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);

        userId = FirebaseAuth.getInstance().getUid();

        initViews();
        initData();
        initRecyclerView();
        initTabs();
        setupBottomNavigation();

        loadSavedTab(); // mặc định mở tab Saved
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isSavedTab) {
            loadSavedTab();
        } else {
            loadHistoryTab();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        backgroundExecutor.shutdown();
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
        savedRepo = new SavedArticleRepository(this);
        historyDAO = new ReadHistoryDAO(this);
    }

    private void initRecyclerView() {
        rvArticles.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SavedArticleAdapter(this, new ArrayList<>(), false);
        rvArticles.setAdapter(adapter);
    }

    private void initTabs() {
        tabSaved.setOnClickListener(v -> loadSavedTab());
        tabHistory.setOnClickListener(v -> loadHistoryTab());
    }

    // =========================
    // LOAD SAVED (ĐÚNG FLOW)
    // =========================
    private void loadSavedTab() {
        isSavedTab = true;
        updateTabUI();

        // 1. Hiển thị local ngay (có thể trống)
        showSavedFromLocal();

        // 2. Sync từ Firebase → SQLite → load lại UI
        if (userId != null) {
            savedRepo.syncFromRemote(userId, () -> {
                runOnUiThread(this::showSavedFromLocal);
            });
        }
    }

    private void showSavedFromLocal() {
        backgroundExecutor.execute(() -> {
            List<Article> list = savedRepo.getSavedArticlesLocal();
            runOnUiThread(() -> updateUI(list, true));
        });
    }

    // =========================
    // LOAD HISTORY
    // =========================
    private void loadHistoryTab() {
        isSavedTab = false;
        updateTabUI();

        backgroundExecutor.execute(() -> {
            List<Article> list = historyDAO.getReadHistory();
            runOnUiThread(() -> updateUI(list, false));
        });
    }

    // =========================
    // UPDATE UI
    // =========================
    private void updateUI(List<Article> list, boolean isSaved) {
        if (list == null || list.isEmpty()) {
            rvArticles.setVisibility(View.GONE);
            layoutEmptySaved.setVisibility(isSaved ? View.VISIBLE : View.GONE);
            layoutEmptyHistory.setVisibility(isSaved ? View.GONE : View.VISIBLE);
        } else {
            rvArticles.setVisibility(View.VISIBLE);
            layoutEmptySaved.setVisibility(View.GONE);
            layoutEmptyHistory.setVisibility(View.GONE);

            adapter = new SavedArticleAdapter(this, list, !isSaved);
            rvArticles.setAdapter(adapter);
        }
    }

    // =========================
    // TAB UI
    // =========================
    private void updateTabUI() {
        if (isSavedTab) {
            tvTabSaved.setTextColor(getResources().getColor(R.color.article_text_primary));
            tvTabHistory.setTextColor(getResources().getColor(R.color.article_text_secondary));
            viewTabSavedIndicator.setVisibility(View.VISIBLE);
            viewTabHistoryIndicator.setVisibility(View.INVISIBLE);
        } else {
            tvTabSaved.setTextColor(getResources().getColor(R.color.article_text_secondary));
            tvTabHistory.setTextColor(getResources().getColor(R.color.article_text_primary));
            viewTabSavedIndicator.setVisibility(View.INVISIBLE);
            viewTabHistoryIndicator.setVisibility(View.VISIBLE);
        }
    }

    // =========================
    // BOTTOM NAV
    // =========================
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_saved);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_search) {
                startActivity(new Intent(this, SearchActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_account) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            return true;
        });
    }
}
