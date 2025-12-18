package phivu.ueh.edu.vn.news_app.ui.saved;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.data.local.history.ReadHistoryDAO;
import phivu.ueh.edu.vn.news_app.data.local.saved.SavedArticleDAO;
import phivu.ueh.edu.vn.news_app.ui.main.ArticleDetailActivity;
import phivu.ueh.edu.vn.news_app.ui.main.HomeActivity;
import phivu.ueh.edu.vn.news_app.ui.profile.ProfileActivity;
import phivu.ueh.edu.vn.news_app.ui.search.SearchActivity;
import phivu.ueh.edu.vn.news_app.model.Article;

public class SavedActivity extends AppCompatActivity {

    // Tab views
    private LinearLayout tabSaved, tabHistory;
    private TextView tvTabSaved, tvTabHistory;
    private View viewTabSavedIndicator, viewTabHistoryIndicator;

    // Content views
    private RecyclerView rvSavedArticles;
    private LinearLayout layoutEmptyStateSaved, layoutEmptyStateHistory;
    private TextView btnStartExplore, btnStartExploreHistory;

    // Data
    private SavedArticleDAO savedArticleDAO;
    private ReadHistoryDAO readHistoryDAO;
    private SavedArticleAdapter adapter;
    private List<Article> currentList = new ArrayList<>();
    
    // State
    private boolean isSavedTab = true; // true = Saved tab, false = History tab
    private ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);

        initViews();
        initRepositories();
        setupTabs();
        setupRecyclerView();
        setupBottomNavigation();
        setupEmptyStateButtons();

        // Load saved articles by default
        loadSavedArticles();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning to this screen (in case articles were saved/unsaved elsewhere)
        if (isSavedTab) {
            loadSavedArticles();
        } else {
            loadReadHistory();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (backgroundExecutor != null) {
            backgroundExecutor.shutdown();
        }
    }

    private void initViews() {
        // Tabs
        tabSaved = findViewById(R.id.tabSaved);
        tabHistory = findViewById(R.id.tabHistory);
        tvTabSaved = findViewById(R.id.tvTabSaved);
        tvTabHistory = findViewById(R.id.tvTabHistory);
        viewTabSavedIndicator = findViewById(R.id.viewTabSavedIndicator);
        viewTabHistoryIndicator = findViewById(R.id.viewTabHistoryIndicator);

        // Content
        rvSavedArticles = findViewById(R.id.rvSavedArticles);
        layoutEmptyStateSaved = findViewById(R.id.layoutEmptyStateSaved);
        layoutEmptyStateHistory = findViewById(R.id.layoutEmptyStateHistory);
        btnStartExplore = findViewById(R.id.btnStartExplore);
        btnStartExploreHistory = findViewById(R.id.btnStartExploreHistory);
    }

    private void initRepositories() {
        try {
            savedArticleDAO = new SavedArticleDAO(this);
            readHistoryDAO = new ReadHistoryDAO(this);
        } catch (Exception e) {
            // Handle error
        }
    }

    private void setupTabs() {
        if (tabSaved != null) {
            tabSaved.setOnClickListener(v -> switchToSavedTab());
        }

        if (tabHistory != null) {
            tabHistory.setOnClickListener(v -> switchToHistoryTab());
        }
    }

    private void switchToSavedTab() {
        if (isSavedTab) return;

        isSavedTab = true;
        updateTabUI();
        // Update adapter mode
        if (adapter != null) {
            adapter = new SavedArticleAdapter(this, new ArrayList<>(), false);
            adapter.setOnItemDeletedListener(articleId -> {});
            rvSavedArticles.setAdapter(adapter);
        }
        loadSavedArticles();
    }

    private void switchToHistoryTab() {
        if (!isSavedTab) return;

        isSavedTab = false;
        updateTabUI();
        // Update adapter mode
        if (adapter != null) {
            adapter = new SavedArticleAdapter(this, new ArrayList<>(), true);
            adapter.setOnItemDeletedListener(articleId -> {
                // Reload history when item is deleted
                loadReadHistory();
            });
            rvSavedArticles.setAdapter(adapter);
        }
        loadReadHistory();
    }

    private void updateTabUI() {
        if (isSavedTab) {
            // Saved tab active
            if (tvTabSaved != null) {
                tvTabSaved.setTextColor(getResources().getColor(R.color.article_text_primary));
                tvTabSaved.setTextSize(16);
            }
            if (viewTabSavedIndicator != null) {
                viewTabSavedIndicator.setVisibility(View.VISIBLE);
            }
            if (tvTabHistory != null) {
                tvTabHistory.setTextColor(getResources().getColor(R.color.article_text_secondary));
                tvTabHistory.setTextSize(16);
            }
            if (viewTabHistoryIndicator != null) {
                viewTabHistoryIndicator.setVisibility(View.INVISIBLE);
            }
        } else {
            // History tab active
            if (tvTabSaved != null) {
                tvTabSaved.setTextColor(getResources().getColor(R.color.article_text_secondary));
                tvTabSaved.setTextSize(16);
            }
            if (viewTabSavedIndicator != null) {
                viewTabSavedIndicator.setVisibility(View.INVISIBLE);
            }
            if (tvTabHistory != null) {
                tvTabHistory.setTextColor(getResources().getColor(R.color.article_text_primary));
                tvTabHistory.setTextSize(16);
            }
            if (viewTabHistoryIndicator != null) {
                viewTabHistoryIndicator.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupRecyclerView() {
        if (rvSavedArticles != null) {
            adapter = new SavedArticleAdapter(this, new ArrayList<>(), false);
            adapter.setOnItemDeletedListener(articleId -> {
                // Reload history when item is deleted
                if (!isSavedTab) {
                    loadReadHistory();
                }
            });
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            rvSavedArticles.setLayoutManager(layoutManager);
            rvSavedArticles.setAdapter(adapter);
        }
    }

    private void setupEmptyStateButtons() {
        if (btnStartExplore != null) {
            btnStartExplore.setOnClickListener(v -> {
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                finish();
            });
        }

        if (btnStartExploreHistory != null) {
            btnStartExploreHistory.setOnClickListener(v -> {
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    private void loadSavedArticles() {
        if (savedArticleDAO == null) return;

        backgroundExecutor.execute(() -> {
            try {
                List<Article> articles = savedArticleDAO.getSavedArticles();
                runOnUiThread(() -> {
                    currentList = articles != null ? articles : new ArrayList<>();
                    updateUI(currentList, true);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    updateUI(new ArrayList<>(), true);
                });
            }
        });
    }

    private void loadReadHistory() {
        if (readHistoryDAO == null) return;

        backgroundExecutor.execute(() -> {
            try {
                List<Article> articles = readHistoryDAO.getReadHistory();
                runOnUiThread(() -> {
                    currentList = articles != null ? articles : new ArrayList<>();
                    updateUI(currentList, false);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    updateUI(new ArrayList<>(), false);
                });
            }
        });
    }

    private void updateUI(List<Article> articles, boolean isSaved) {
        if (articles == null) {
            articles = new ArrayList<>();
        }

        if (articles.isEmpty()) {
            // Show empty state
            if (rvSavedArticles != null) {
                rvSavedArticles.setVisibility(View.GONE);
            }
            if (isSaved) {
                if (layoutEmptyStateSaved != null) {
                    layoutEmptyStateSaved.setVisibility(View.VISIBLE);
                }
                if (layoutEmptyStateHistory != null) {
                    layoutEmptyStateHistory.setVisibility(View.GONE);
                }
            } else {
                if (layoutEmptyStateSaved != null) {
                    layoutEmptyStateSaved.setVisibility(View.GONE);
                }
                if (layoutEmptyStateHistory != null) {
                    layoutEmptyStateHistory.setVisibility(View.VISIBLE);
                }
            }
        } else {
            // Show list
            if (rvSavedArticles != null) {
                rvSavedArticles.setVisibility(View.VISIBLE);
            }
            if (layoutEmptyStateSaved != null) {
                layoutEmptyStateSaved.setVisibility(View.GONE);
            }
            if (layoutEmptyStateHistory != null) {
                layoutEmptyStateHistory.setVisibility(View.GONE);
            }

            if (adapter != null) {
                adapter.updateData(articles);
            }
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        if (bottomNav != null) {
            // Đặt mục chọn hiện tại là Saved
            bottomNav.setSelectedItemId(R.id.nav_saved);

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_saved) {
                    return true; // Đang ở Saved thì không làm gì
                }
                else if (id == R.id.nav_home) {
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }
                else if (id == R.id.nav_search) {
                    startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }
                else if (id == R.id.nav_account) {
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }
                return false;
            });
        }
    }
}
