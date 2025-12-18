package phivu.ueh.edu.vn.news_app.ui.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.data.local.article.ArticleDAO;
import phivu.ueh.edu.vn.news_app.data.local.saved.SavedArticleDAO;
import phivu.ueh.edu.vn.news_app.data.local.history.ReadHistoryDAO;
import phivu.ueh.edu.vn.news_app.data.repository.ArticleRepository;
import phivu.ueh.edu.vn.news_app.data.repository.CategoryRepository;
import phivu.ueh.edu.vn.news_app.data.repository.CommentRepository;
import phivu.ueh.edu.vn.news_app.model.Article;
import phivu.ueh.edu.vn.news_app.model.Category;
import phivu.ueh.edu.vn.news_app.model.Comment;

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

    // Comments Preview
    private LinearLayout layoutCommentsPreview;
    private TextView tvCommentsPreviewTitle;
    private TextView tvViewAllComments;
    private RecyclerView rvCommentsPreview;
    private LinearLayout layoutCommentInputPreview;
    private ImageView imgPreviewInputAvatar;
    private TextView tvCommentInputPreview;

    // Data
    private ArticleRepository articleRepo;
    private CategoryRepository categoryRepo;
    private CommentRepository commentRepo;
    private ArticleDAO articleDAO;
    private SavedArticleDAO savedArticleDAO;
    private ReadHistoryDAO readHistoryDAO;
    private Article currentArticle;
    private boolean isSaved = false;
    private String currentArticleId;
    private boolean isShowingCachedData = false;

    // Comments
    private CommentAdapter commentPreviewAdapter;
    private List<Comment> commentList = new ArrayList<>();
    private ListenerRegistration commentsListener;
    private static final int MAX_PREVIEW_COMMENTS = 3;

    // Related Articles
    private LinearLayout layoutRelatedArticles;
    private TextView tvRelatedArticlesTitle;
    private RecyclerView rvRelatedArticles;
    private ProgressBar progressRelatedArticles;
    private TextView tvRelatedArticlesEmpty;
    private LinearLayout layoutRelatedArticlesError;
    private TextView tvRelatedArticlesError;
    private MaterialButton btnRelatedArticlesRetry;
    private RelatedArticlesAdapter relatedArticlesAdapter;
    private List<Article> relatedArticlesList = new ArrayList<>();

    // Lifecycle safety
    private boolean isDestroyed = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        // Hardening: Validate articleId from Intent
        Intent intent = getIntent();
        if (intent == null) {
            showErrorState("Lỗi: Intent không hợp lệ");
            return;
        }

        currentArticleId = intent.getStringExtra("articleId");
        if (currentArticleId == null || currentArticleId.trim().isEmpty()) {
            showErrorState("Thiếu ID bài báo");
            return;
        }

        // Sanitize articleId
        currentArticleId = currentArticleId.trim();

        initViews();
        initRepositories();
        setupToolbar();
        setupBottomBar();
        setupRetryButton();
        setupCommentsPreview();
        setupRelatedArticles();

        // Load data: cache first, then sync
        loadArticleData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;

        // Remove comments listener
        if (commentsListener != null) {
            commentsListener.remove();
            commentsListener = null;
        }

        if (backgroundExecutor != null) {
            backgroundExecutor.shutdown();
        }
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

        // Comments Preview
        layoutCommentsPreview = findViewById(R.id.layoutCommentsPreview);
        tvCommentsPreviewTitle = findViewById(R.id.tvCommentsPreviewTitle);
        tvViewAllComments = findViewById(R.id.tvViewAllComments);
        rvCommentsPreview = findViewById(R.id.rvCommentsPreview);
        layoutCommentInputPreview = findViewById(R.id.layoutCommentInputPreview);
        imgPreviewInputAvatar = findViewById(R.id.imgPreviewInputAvatar);
        tvCommentInputPreview = findViewById(R.id.tvCommentInputPreview);

        // Related Articles
        layoutRelatedArticles = findViewById(R.id.layoutRelatedArticles);
        tvRelatedArticlesTitle = findViewById(R.id.tvRelatedArticlesTitle);
        rvRelatedArticles = findViewById(R.id.rvRelatedArticles);
        progressRelatedArticles = findViewById(R.id.progressRelatedArticles);
        tvRelatedArticlesEmpty = findViewById(R.id.tvRelatedArticlesEmpty);
        layoutRelatedArticlesError = findViewById(R.id.layoutRelatedArticlesError);
        tvRelatedArticlesError = findViewById(R.id.tvRelatedArticlesError);
        btnRelatedArticlesRetry = findViewById(R.id.btnRelatedArticlesRetry);

        // Safety check
        if (imgHeader == null || tvTitle == null || tvAuthor == null || tvDate == null ||
            chipCategory == null || tvDescription == null || dividerContent == null || tvContent == null ||
            btnComment == null || btnSave == null || btnShare == null || iconSave == null ||
            progressLoading == null || layoutError == null || tvError == null || btnRetry == null) {
            Toast.makeText(this, "Lỗi khởi tạo giao diện", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void initRepositories() {
        try {
            articleRepo = new ArticleRepository(this);
            categoryRepo = new CategoryRepository(this);
            articleDAO = new ArticleDAO(this);
            savedArticleDAO = new SavedArticleDAO(this);
            readHistoryDAO = new ReadHistoryDAO(this);
            commentRepo = new CommentRepository(); // Fixed: initialize commentRepo
        } catch (Exception e) {
            showErrorState("Lỗi khởi tạo dữ liệu");
        }
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.detailToolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (!isDestroyed) {
                    finish();
                }
            });
        }
    }

    private void setupBottomBar() {
        if (btnComment != null) {
            btnComment.setOnClickListener(v -> {
                if (!isDestroyed) {
                    showCommentBottomSheet();
                }
            });
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                if (!isDestroyed) {
                    toggleSave();
                }
            });
        }

        if (btnShare != null) {
            btnShare.setOnClickListener(v -> {
                if (!isDestroyed) {
                    shareArticle();
                }
            });
        }
    }

    private void setupRetryButton() {
        if (btnRetry != null) {
            btnRetry.setOnClickListener(v -> {
                if (!isDestroyed) {
                    hideErrorState();
                    loadArticleData();
                }
            });
        }
    }

    private void setupCommentsPreview() {
        // Setup RecyclerView
        if (rvCommentsPreview != null) {
            commentPreviewAdapter = new CommentAdapter(this, new ArrayList<>());
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            rvCommentsPreview.setLayoutManager(layoutManager);
            rvCommentsPreview.setAdapter(commentPreviewAdapter);
            rvCommentsPreview.setHasFixedSize(false);
            rvCommentsPreview.setNestedScrollingEnabled(false);
            // Initially visible, will be hidden if no comments
            rvCommentsPreview.setVisibility(View.VISIBLE);
        }

        // Setup "View All" click
        if (tvViewAllComments != null) {
            tvViewAllComments.setOnClickListener(v -> {
                if (!isDestroyed) {
                    showCommentBottomSheet(false);
                }
            });
        }

        // Setup input preview click -> open sheet with focus
        if (layoutCommentInputPreview != null) {
            layoutCommentInputPreview.setOnClickListener(v -> {
                if (!isDestroyed) {
                    showCommentBottomSheet(true);
                }
            });
        }

        // Setup preview avatar
        setupPreviewAvatar();
    }

    private void setupRelatedArticles() {
        // Setup RecyclerView
        if (rvRelatedArticles != null) {
            relatedArticlesAdapter = new RelatedArticlesAdapter(this, new ArrayList<>());
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            rvRelatedArticles.setLayoutManager(layoutManager);
            rvRelatedArticles.setAdapter(relatedArticlesAdapter);
            rvRelatedArticles.setHasFixedSize(false);
            rvRelatedArticles.setNestedScrollingEnabled(false);
        }

        // Setup retry button
        if (btnRelatedArticlesRetry != null) {
            btnRelatedArticlesRetry.setOnClickListener(v -> {
                if (!isDestroyed && currentArticle != null) {
                    loadRelatedArticles();
                }
            });
        }
    }

    private void loadRelatedArticles() {
        if (currentArticle == null || articleRepo == null || isDestroyed) {
            // Hide section if no article data
            runOnUiThreadSafe(() -> {
                if (isDestroyed || layoutRelatedArticles == null) return;
                layoutRelatedArticles.setVisibility(View.GONE);
            });
            return;
        }

        String authorId = currentArticle.getAuthorId();
        String authorName = currentArticle.getAuthorName();

        // Debug log
        android.util.Log.d("RelatedArticles", "Loading related articles - authorId: " + authorId + ", articleId: " + currentArticleId);

        // Validate authorId
        if (TextUtils.isEmpty(authorId)) {
            android.util.Log.d("RelatedArticles", "No authorId, hiding section");
            // Hide section if no authorId
            runOnUiThreadSafe(() -> {
                if (isDestroyed || layoutRelatedArticles == null) return;
                layoutRelatedArticles.setVisibility(View.GONE);
            });
            return;
        }

        // Always show section when we have authorId
        runOnUiThreadSafe(() -> {
            if (isDestroyed) return;
            
            // Show section
            if (layoutRelatedArticles != null) {
                layoutRelatedArticles.setVisibility(View.VISIBLE);
            }
            
            // Update title with author name
            if (tvRelatedArticlesTitle != null) {
                String titleText = getString(R.string.related_articles_title, 
                        !TextUtils.isEmpty(authorName) ? authorName : "tác giả");
                tvRelatedArticlesTitle.setText(titleText);
            }
            
            // Show loading state
            if (progressRelatedArticles != null) {
                progressRelatedArticles.setVisibility(View.VISIBLE);
            }
            if (rvRelatedArticles != null) {
                rvRelatedArticles.setVisibility(View.GONE);
            }
            if (tvRelatedArticlesEmpty != null) {
                tvRelatedArticlesEmpty.setVisibility(View.GONE);
            }
            if (layoutRelatedArticlesError != null) {
                layoutRelatedArticlesError.setVisibility(View.GONE);
            }
        });

        // Fetch related articles
        articleRepo.getArticlesByAuthor(authorId, currentArticleId, 10, 
                new ArticleRepository.ListCallback() {
                    @Override
                    public void onSuccess(List<Article> list) {
                        android.util.Log.d("RelatedArticles", "Success - Found " + (list != null ? list.size() : 0) + " articles");
                        runOnUiThreadSafe(() -> {
                            if (isDestroyed) return;

                            // Hide loading
                            if (progressRelatedArticles != null) {
                                progressRelatedArticles.setVisibility(View.GONE);
                            }

                            if (list == null || list.isEmpty()) {
                                android.util.Log.d("RelatedArticles", "Empty list, showing empty state");
                                // Show empty state
                                if (rvRelatedArticles != null) {
                                    rvRelatedArticles.setVisibility(View.GONE);
                                }
                                if (tvRelatedArticlesEmpty != null) {
                                    tvRelatedArticlesEmpty.setVisibility(View.VISIBLE);
                                }
                                if (layoutRelatedArticlesError != null) {
                                    layoutRelatedArticlesError.setVisibility(View.GONE);
                                }
                            } else {
                                android.util.Log.d("RelatedArticles", "Showing " + list.size() + " articles");
                                // Show list
                                relatedArticlesList = list;
                                if (relatedArticlesAdapter != null) {
                                    relatedArticlesAdapter.updateData(list);
                                }
                                if (rvRelatedArticles != null) {
                                    rvRelatedArticles.setVisibility(View.VISIBLE);
                                }
                                if (tvRelatedArticlesEmpty != null) {
                                    tvRelatedArticlesEmpty.setVisibility(View.GONE);
                                }
                                if (layoutRelatedArticlesError != null) {
                                    layoutRelatedArticlesError.setVisibility(View.GONE);
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String err) {
                        android.util.Log.e("RelatedArticles", "Error loading articles: " + err);
                        runOnUiThreadSafe(() -> {
                            if (isDestroyed) return;

                            // Hide loading
                            if (progressRelatedArticles != null) {
                                progressRelatedArticles.setVisibility(View.GONE);
                            }

                            // Show error state with message
                            if (tvRelatedArticlesError != null) {
                                String errorMsg = err != null ? err : getString(R.string.related_articles_error);
                                // Check if error is about missing index
                                if (err != null && err.contains("index")) {
                                    errorMsg = "Đang tải..."; // Silently retry with fallback
                                }
                                tvRelatedArticlesError.setText(errorMsg);
                            }
                            if (rvRelatedArticles != null) {
                                rvRelatedArticles.setVisibility(View.GONE);
                            }
                            if (tvRelatedArticlesEmpty != null) {
                                tvRelatedArticlesEmpty.setVisibility(View.GONE);
                            }
                            if (layoutRelatedArticlesError != null) {
                                layoutRelatedArticlesError.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
    }

    private void setupPreviewAvatar() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && imgPreviewInputAvatar != null) {
            String photoUrl = currentUser.getPhotoUrl() != null ?
                    currentUser.getPhotoUrl().toString() : null;
            if (!TextUtils.isEmpty(photoUrl)) {
                try {
                    int avatarSize = getResources()
                            .getDimensionPixelSize(R.dimen.comments_sheet_avatar_size);
                    Picasso.get()
                            .load(photoUrl)
                            .resize(avatarSize, avatarSize)
                            .onlyScaleDown()
                            .centerCrop()
                            .placeholder(android.R.drawable.ic_menu_report_image)
                            .error(android.R.drawable.ic_menu_report_image)
                            .into(imgPreviewInputAvatar);
                } catch (Exception e) {
                    imgPreviewInputAvatar.setImageResource(android.R.drawable.ic_menu_report_image);
                }
            } else {
                imgPreviewInputAvatar.setImageResource(android.R.drawable.ic_menu_report_image);
            }
        } else if (imgPreviewInputAvatar != null) {
            imgPreviewInputAvatar.setImageResource(android.R.drawable.ic_menu_report_image);
        }
    }

    private void loadComments() {
        if (TextUtils.isEmpty(currentArticleId) || commentRepo == null) {
            return;
        }

        // Remove existing listener if any
        if (commentsListener != null) {
            commentsListener.remove();
            commentsListener = null;
        }

        // Observe comments in realtime
        commentsListener = commentRepo.observeComments(currentArticleId,
                new CommentRepository.ListCallback() {
                    @Override
                    public void onSuccess(List<Comment> list) {
                        if (isDestroyed) return;

                        // Make list effectively final
                        final List<Comment> finalList = (list != null) ? list : new ArrayList<>();

                        runOnUiThreadSafe(() -> {
                            commentList.clear();
                            commentList.addAll(finalList);

                            // Update preview
                            updateCommentsPreview(finalList);
                        });
                    }

                    @Override
                    public void onError(String err) {
                        if (isDestroyed) return;
                        // Silent fail for preview - don't show error
                    }
                });
    }

    private void updateCommentsPreview(List<Comment> allComments) {
        if (isDestroyed) return;

        int totalCount = allComments != null ? allComments.size() : 0;

        // Update title with count
        if (tvCommentsPreviewTitle != null) {
            tvCommentsPreviewTitle.setText(getString(R.string.comments_title) + " (" + totalCount + ")");
        }

        // Show/hide "View All" button
        if (tvViewAllComments != null) {
            tvViewAllComments.setVisibility(totalCount > MAX_PREVIEW_COMMENTS ? View.VISIBLE : View.GONE);
        }

        // Update preview list (max 3 items)
        if (commentPreviewAdapter == null || rvCommentsPreview == null) {
            // Re-initialize if null
            if (rvCommentsPreview != null && commentPreviewAdapter == null) {
                commentPreviewAdapter = new CommentAdapter(this, new ArrayList<>());
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                rvCommentsPreview.setLayoutManager(layoutManager);
                rvCommentsPreview.setAdapter(commentPreviewAdapter);
                rvCommentsPreview.setHasFixedSize(false);
                rvCommentsPreview.setNestedScrollingEnabled(false);
            }
        }

        if (commentPreviewAdapter != null && rvCommentsPreview != null) {
            List<Comment> previewList = new ArrayList<>();
            if (allComments != null && !allComments.isEmpty()) {
                int previewCount = Math.min(allComments.size(), MAX_PREVIEW_COMMENTS);
                for (int i = 0; i < previewCount; i++) {
                    previewList.add(allComments.get(i));
                }
            }
            
            // Always update adapter (even if empty)
            commentPreviewAdapter.updateData(previewList);
            
            // Show RecyclerView if there are comments, hide if empty
            if (previewList.isEmpty()) {
                rvCommentsPreview.setVisibility(View.GONE);
            } else {
                rvCommentsPreview.setVisibility(View.VISIBLE);
                // Force layout update to ensure items are displayed
                rvCommentsPreview.post(() -> {
                    if (rvCommentsPreview != null && commentPreviewAdapter != null && !isDestroyed) {
                        rvCommentsPreview.requestLayout();
                        rvCommentsPreview.invalidate();
                        // Ensure adapter is still attached
                        if (rvCommentsPreview.getAdapter() == null) {
                            rvCommentsPreview.setAdapter(commentPreviewAdapter);
                        }
                    }
                });
            }
        }

        // Always show preview section (even if empty, to show input field)
        if (layoutCommentsPreview != null) {
            layoutCommentsPreview.setVisibility(View.VISIBLE);
        }
    }

    private void loadArticleData() {
        if (isDestroyed || currentArticleId == null || articleRepo == null) {
            return;
        }

        showLoadingState();

        // Step 1: Load from cache first (offline-friendly) - Run on background thread
        backgroundExecutor.execute(() -> {
            try {
                Article cachedArticle = articleRepo.getArticleFromCache(currentArticleId);
                if (cachedArticle != null) {
                    // Render cached data immediately on UI thread
                    runOnUiThreadSafe(() -> {
                        if (isDestroyed) return;
                        isShowingCachedData = true;
                        bindArticleData(cachedArticle);
                        hideLoadingState();
                        Toast.makeText(ArticleDetailActivity.this, "Đang hiển thị bản lưu", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // No cache
                    runOnUiThreadSafe(() -> {
                        if (isDestroyed) return;
                        isShowingCachedData = false;
                    });
                }

                // Record read history (background thread)
                try {
                    if (readHistoryDAO != null) {
                        readHistoryDAO.recordRead(currentArticleId);
                    }
                } catch (Exception e) {
                    // Ignore database errors
                }

                // Check if article is saved (background thread)
                try {
                    boolean saved = savedArticleDAO != null && savedArticleDAO.isSaved(currentArticleId);
                    runOnUiThreadSafe(() -> {
                        if (isDestroyed) return;
                        isSaved = saved;
                        updateSaveButtonState();
                    });
                } catch (Exception e) {
                    // Ignore database errors
                }
            } catch (Exception e) {
                runOnUiThreadSafe(() -> {
                    if (isDestroyed) return;
                    if (!isShowingCachedData) {
                        showErrorState("Lỗi khi tải dữ liệu từ bộ nhớ");
                    }
                });
            }
        });

        // Step 2: Sync from Firestore (update UI when done)
        articleRepo.syncArticleFromRemote(currentArticleId, new ArticleRepository.SingleCallback() {
            @Override
            public void onSuccess(Article article) {
                runOnUiThreadSafe(() -> {
                    if (isDestroyed) return;

                    // Hardening: Validate article data
                    if (article == null) {
                        if (!isShowingCachedData) {
                            showErrorState("Không tải được dữ liệu bài viết");
                        }
                        return;
                    }

                    // Validate article ID matches
                    if (article.getId() == null || !article.getId().equals(currentArticleId)) {
                        if (!isShowingCachedData) {
                            showErrorState("Dữ liệu bài viết không khớp");
                        }
                        return;
                    }

                    // Update UI with fresh data
                    isShowingCachedData = false;
                    bindArticleData(article);
                    hideLoadingState();

                    // Load category name if categoryId exists
                    String categoryId = article.getCategoryId();
                    if (categoryId != null && !categoryId.trim().isEmpty()) {
                        loadCategoryName(categoryId.trim());
                    }

                    // Mark as viewed (background thread)
                    backgroundExecutor.execute(() -> {
                        try {
                            articleRepo.markViewed(article);
                        } catch (Exception e) {
                            // Ignore database errors
                        }
                    });
                });
            }

            @Override
            public void onError(String err) {
                runOnUiThreadSafe(() -> {
                    if (isDestroyed) return;

                    if (!isShowingCachedData) {
                        // No cache, show error
                        String errorMsg = "Không thể cập nhật";
                        if (err != null && err.contains("Not found")) {
                            errorMsg = "Bài viết không tồn tại";
                        } else if (err != null && !err.isEmpty()) {
                            errorMsg = "Không thể cập nhật: " + err;
                        }
                        showErrorState(errorMsg);
                    } else {
                        // Has cache, just show message
                        hideLoadingState();
                        Toast.makeText(ArticleDetailActivity.this,
                                "Không thể cập nhật, đang hiển thị bản lưu",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void bindArticleData(Article article) {
        if (article == null || isDestroyed) return;

        currentArticle = article;

        // Title - Hardening: Handle null/empty
        if (tvTitle != null) {
            String title = article.getTitle();
            tvTitle.setText(title != null && !title.trim().isEmpty() ? title.trim() : "Không có tiêu đề");
        }

        // Author - Hardening: Handle null/empty
        if (tvAuthor != null) {
            String authorName = article.getAuthorName();
            if (authorName == null || authorName.trim().isEmpty()) {
                try {
                    authorName = getString(R.string.article_author_default);
                } catch (Exception e) {
                    authorName = "Tác giả";
                }
            } else {
                authorName = authorName.trim();
            }
            tvAuthor.setText(authorName);
        }

        // Date - Hardening: Handle invalid dates (0, null, negative, type mismatch)
        if (tvDate != null) {
            long publishDate = 0;
            try {
                Object dateObj = article.getPublishDate();
                if (dateObj instanceof Long) {
                    publishDate = (Long) dateObj;
                } else if (dateObj instanceof Integer) {
                    publishDate = ((Integer) dateObj).longValue();
                } else if (dateObj instanceof Number) {
                    publishDate = ((Number) dateObj).longValue();
                }
            } catch (Exception e) {
                // If getPublishDate() throws exception, use 0
                publishDate = 0;
            }

            if (publishDate > 0) {
                String formattedDate = formatPublishDate(publishDate);
                tvDate.setText(formattedDate);
                tvDate.setVisibility(View.VISIBLE);
            } else {
                tvDate.setText("—");
                tvDate.setVisibility(View.VISIBLE);
            }
        }

        // Image - Hardening: Handle null/empty/invalid URLs, Picasso failures, large images
        if (imgHeader != null) {
            String imageUrl = article.getImage();
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                imageUrl = imageUrl.trim();
                try {
                    // Validate URL format (basic check)
                    if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://") || 
                        imageUrl.startsWith("file://") || imageUrl.startsWith("content://")) {
                        
                        // Get screen width to calculate max image size
                        int screenWidth = getResources().getDisplayMetrics().widthPixels;
                        int imageHeight = (int) getResources().getDimension(R.dimen.article_detail_image_height);
                        
                        // Resize image to prevent "too large bitmap" error
                        int maxWidth = screenWidth;
                        int maxHeight = imageHeight * 2; // Allow 2x for high DPI
                        
                        Picasso.get()
                                .load(imageUrl)
                                .resize(maxWidth, maxHeight)
                                .onlyScaleDown() // Only resize if image is larger
                                .centerCrop()
                                .placeholder(android.R.drawable.ic_menu_report_image)
                                .error(android.R.drawable.ic_menu_report_image)
                                .into(imgHeader);
                    } else {
                        // Invalid URL format, use placeholder
                        imgHeader.setImageResource(android.R.drawable.ic_menu_report_image);
                    }
                } catch (OutOfMemoryError e) {
                    // Handle OOM errors
                    imgHeader.setImageResource(android.R.drawable.ic_menu_report_image);
                } catch (Exception e) {
                    // Picasso or image loading failed, use placeholder
                    imgHeader.setImageResource(android.R.drawable.ic_menu_report_image);
                }
            } else {
                // No image URL, use placeholder
                imgHeader.setImageResource(android.R.drawable.ic_menu_report_image);
            }
        }

        // Description - Hardening: Handle null/empty
        if (tvDescription != null && dividerContent != null) {
            String description = article.getDescription();
            if (description != null && !description.trim().isEmpty()) {
                tvDescription.setText(description.trim());
                tvDescription.setVisibility(View.VISIBLE);
                dividerContent.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
                dividerContent.setVisibility(View.GONE);
            }
        }

        // Content - Hardening: Handle null/empty
        if (tvContent != null) {
            String content = article.getContent();
            if (content == null || content.trim().isEmpty()) {
                try {
                    content = getString(R.string.article_content_placeholder);
                } catch (Exception e) {
                    content = "Nội dung đang được cập nhật";
                }
            } else {
                content = content.trim();
            }
            tvContent.setText(content);
        }

        // Category - Hardening: Hide if categoryId not mapped
        if (chipCategory != null) {
            String categoryId = article.getCategoryId();
            if (categoryId != null && !categoryId.trim().isEmpty()) {
                // Will be set when category name is loaded
                chipCategory.setVisibility(View.GONE); // Hide first, show when name is loaded
                loadCategoryName(categoryId.trim());
            } else {
                chipCategory.setVisibility(View.GONE);
            }
        }

        // Load comments after article is loaded
        loadComments();

        // Load related articles after article is loaded
        loadRelatedArticles();
    }

    private void loadCategoryName(String categoryId) {
        if (categoryId == null || categoryId.isEmpty() || chipCategory == null || isDestroyed) {
            return;
        }

        categoryRepo.getCategories(new CategoryRepository.Callback() {
            @Override
            public void onSuccess(List<Category> categories) {
                runOnUiThreadSafe(() -> {
                    if (isDestroyed || chipCategory == null) return;

                    // Hardening: Handle null categories list, null category items
                    if (categories != null && !categories.isEmpty()) {
                        for (Category cat : categories) {
                            if (cat != null && cat.getId() != null && cat.getId().equals(categoryId)) {
                                String categoryName = cat.getName();
                                if (categoryName != null && !categoryName.trim().isEmpty()) {
                                    chipCategory.setText(categoryName.trim());
                                    chipCategory.setVisibility(View.VISIBLE);
                                    return;
                                }
                            }
                        }
                    }
                    // Category not found or name is empty
                    chipCategory.setVisibility(View.GONE);
                });
            }

            @Override
            public void onError(String err) {
                runOnUiThreadSafe(() -> {
                    if (chipCategory != null && !isDestroyed) {
                        chipCategory.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    private String formatPublishDate(long timestamp) {
        // Use shared DateUtils for consistency with card
        String formatted = phivu.ueh.edu.vn.news_app.utils.DateUtils.formatPublishDate(timestamp);
        return formatted != null && !formatted.isEmpty() ? formatted : "—";
    }

    // =========================
    // LIFECYCLE-SAFE UI UPDATES
    // =========================

    /**
     * Thread-safe UI update with lifecycle check
     */
    private void runOnUiThreadSafe(Runnable action) {
        if (action == null) return;
        
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // Already on main thread
            if (!isDestroyed && !isFinishing()) {
                action.run();
            }
        } else {
            // Post to main thread
            mainHandler.post(() -> {
                if (!isDestroyed && !isFinishing()) {
                    action.run();
                }
            });
        }
    }

    // =========================
    // UI STATE MANAGEMENT
    // =========================

    private void showLoadingState() {
        runOnUiThreadSafe(() -> {
            if (progressLoading != null) {
                progressLoading.setVisibility(View.VISIBLE);
            }
            if (nestedScrollView != null) {
                nestedScrollView.setVisibility(View.GONE);
            }
            if (layoutError != null) {
                layoutError.setVisibility(View.GONE);
            }
        });
    }

    private void hideLoadingState() {
        runOnUiThreadSafe(() -> {
            if (progressLoading != null) {
                progressLoading.setVisibility(View.GONE);
            }
            if (nestedScrollView != null) {
                nestedScrollView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showErrorState(String errorMessage) {
        runOnUiThreadSafe(() -> {
            if (progressLoading != null) {
                progressLoading.setVisibility(View.GONE);
            }
            if (nestedScrollView != null) {
                nestedScrollView.setVisibility(View.GONE);
            }
            if (layoutError != null) {
                layoutError.setVisibility(View.VISIBLE);
            }
            if (tvError != null && errorMessage != null) {
                tvError.setText(errorMessage);
            }
        });
    }

    private void hideErrorState() {
        runOnUiThreadSafe(() -> {
            if (layoutError != null) {
                layoutError.setVisibility(View.GONE);
            }
        });
    }

    // =========================
    // BOTTOM BAR ACTIONS
    // =========================

    private void showCommentBottomSheet() {
        showCommentBottomSheet(false);
    }

    private void showCommentBottomSheet(boolean focusInput) {
        if (isDestroyed || currentArticle == null || currentArticle.getId() == null) {
            Toast.makeText(this, "Không thể mở bình luận", Toast.LENGTH_SHORT).show();
            return;
        }

        String articleId = currentArticle.getId();
        CommentsBottomSheetDialogFragment bottomSheet =
                CommentsBottomSheetDialogFragment.newInstance(articleId, focusInput,
                        new CommentsBottomSheetDialogFragment.CommentsCountListener() {
                            @Override
                            public void onCommentsCountChanged(int count) {
                                // Sync count with preview when bottom sheet updates
                                if (!isDestroyed && tvCommentsPreviewTitle != null) {
                                    runOnUiThreadSafe(() -> {
                                        tvCommentsPreviewTitle.setText(
                                                getString(R.string.comments_title) + " (" + count + ")");
                                    });
                                }
                            }
                        });

        FragmentManager fragmentManager = getSupportFragmentManager();
        bottomSheet.show(fragmentManager, "CommentsBottomSheet");
    }

    private void toggleSave() {
        if (currentArticle == null || currentArticle.getId() == null || savedArticleDAO == null) {
            Toast.makeText(this, "Bài viết chưa được tải", Toast.LENGTH_SHORT).show();
            return;
        }

        String articleId = currentArticle.getId();

        // Run on background thread to avoid blocking UI
        backgroundExecutor.execute(() -> {
            try {
                if (isSaved) {
                    // Unsave: Remove from SavedArticle table
                    savedArticleDAO.unsaveArticle(articleId);
                    isSaved = false;
                    runOnUiThreadSafe(() -> {
                        Toast.makeText(ArticleDetailActivity.this, "Đã bỏ lưu bài viết", Toast.LENGTH_SHORT).show();
                        updateSaveButtonState();
                    });
                } else {
                    // Save: Ensure article exists in Article table first, then add to SavedArticle
                    // Check if article exists in Article table
                    Article existing = articleDAO.getById(articleId);
                    if (existing == null || existing.getContent() == null || existing.getContent().trim().isEmpty()) {
                        // Article not in cache or missing content, fetch from Firestore first
                        articleRepo.syncArticleFromRemote(articleId, new ArticleRepository.SingleCallback() {
                            @Override
                            public void onSuccess(Article article) {
                                if (article != null) {
                                    // Now save to SavedArticle
                                    backgroundExecutor.execute(() -> {
                                        try {
                                            savedArticleDAO.saveArticle(articleId);
                                            isSaved = true;
                                            runOnUiThreadSafe(() -> {
                                                Toast.makeText(ArticleDetailActivity.this, "Đã lưu bài viết", Toast.LENGTH_SHORT).show();
                                                updateSaveButtonState();
                                            });
                                        } catch (Exception e) {
                                            runOnUiThreadSafe(() -> {
                                                Toast.makeText(ArticleDetailActivity.this, "Lỗi khi lưu bài viết", Toast.LENGTH_SHORT).show();
                                            });
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onError(String err) {
                                runOnUiThreadSafe(() -> {
                                    Toast.makeText(ArticleDetailActivity.this, "Không thể tải bài viết để lưu", Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    } else {
                        // Article exists, just save to SavedArticle
                        savedArticleDAO.saveArticle(articleId);
                        isSaved = true;
                        runOnUiThreadSafe(() -> {
                            Toast.makeText(ArticleDetailActivity.this, "Đã lưu bài viết", Toast.LENGTH_SHORT).show();
                            updateSaveButtonState();
                        });
                    }
                }
            } catch (Exception e) {
                runOnUiThreadSafe(() -> {
                    Toast.makeText(ArticleDetailActivity.this, "Lỗi khi lưu bài viết", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateSaveButtonState() {
        if (iconSave == null || isDestroyed) return;

        try {
            if (isSaved) {
                iconSave.setImageResource(R.drawable.bookmarks);
                iconSave.setColorFilter(getColor(R.color.bottom_bar_icon_selected));
            } else {
                iconSave.setImageResource(R.drawable.bookmark_simple);
                iconSave.setColorFilter(getColor(R.color.bottom_bar_icon_default));
            }
        } catch (Exception e) {
            // Ignore errors
        }
    }

    private void shareArticle() {
        if (currentArticle == null) {
            Toast.makeText(this, "Bài viết chưa được tải", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            
            String shareText = currentArticle.getTitle() != null ? currentArticle.getTitle().trim() : "";
            String description = currentArticle.getDescription();
            if (description != null && !description.trim().isEmpty()) {
                shareText += "\n\n" + description.trim();
            }

            if (shareText.isEmpty()) {
                shareText = "Bài viết từ News App";
            }

            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, 
                    currentArticle.getTitle() != null && !currentArticle.getTitle().trim().isEmpty()
                    ? currentArticle.getTitle().trim() : "Bài viết");

            startActivity(Intent.createChooser(shareIntent, "Chia sẻ bài viết"));
        } catch (Exception e) {
            Toast.makeText(this, "Không thể chia sẻ bài viết", Toast.LENGTH_SHORT).show();
        }
    }
}
