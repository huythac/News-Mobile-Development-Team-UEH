package phivu.ueh.edu.vn.news_app.ui.main;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

// --- CÁC THƯ VIỆN BẮT BUỘC PHẢI CÓ (Nếu thiếu sẽ báo đỏ) ---
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
// -----------------------------------------------------------

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.data.local.article.ArticleDAO;
import phivu.ueh.edu.vn.news_app.data.local.saved.SavedArticleDAO;
import phivu.ueh.edu.vn.news_app.data.repository.ArticleRepository;
import phivu.ueh.edu.vn.news_app.data.repository.CommentRepository;
import phivu.ueh.edu.vn.news_app.data.repository.SavedArticleRepository;
import phivu.ueh.edu.vn.news_app.model.Article;
import phivu.ueh.edu.vn.news_app.utils.DateUtils;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {



    private List<Article> articleList;
    private Context context;
    private SavedArticleDAO savedArticleDAO;
    private ArticleDAO articleDAO;
    private ArticleRepository articleRepo;
    private CommentRepository commentRepo;
    private ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    // Cache for saved states and comment counts
    private Map<String, Boolean> savedStates = new HashMap<>();
    private Map<String, Integer> commentCounts = new HashMap<>();

    public enum Mode {
        NORMAL,
        HISTORY
    }

    private Mode mode;
    private SavedArticleRepository savedRepo;
    private String userId;

    // Hai biến này nếu thiếu sẽ gây lỗi "Cannot resolve symbol"
    private ExecutorService executor;

//    public ArticleAdapter(Context context, List<Article> articleList, Mode mode) {
//        this.context = context;
//        this.articleList = articleList;
//        this.mode = mode;
//
//        this.articleDAO = new ArticleDAO(context);
//        this.savedRepo = new SavedArticleRepository(context);
//        this.commentRepo = new CommentRepository();
//
//        this.userId = FirebaseAuth.getInstance().getUid();
//
//        loadSavedStates();
//        loadCommentCounts();
//    }

    public ArticleAdapter(Context context, List<Article> articleList, Mode mode) {
        this.context = context;
        this.articleList = articleList;
        this.mode = mode;

        // 1. Khởi tạo User
        this.userId = FirebaseAuth.getInstance().getUid();

        // 2. Khởi tạo các Repository (QUAN TRỌNG: Phải new trước khi dùng)
        this.articleDAO = new ArticleDAO(context);
        this.commentRepo = new CommentRepository();
        this.savedRepo = new SavedArticleRepository(context);

        // 3. Khởi tạo các công cụ hỗ trợ (QUAN TRỌNG: Thiếu là CRASH)
        this.executor = Executors.newSingleThreadExecutor();
        this.savedStates = new HashMap<>();

        // 4. Sau khi đã 'new' hết các biến ở trên thì mới được gọi hàm load
        if (this.mode == Mode.NORMAL) {
            loadSavedStates();
        }
        loadCommentCounts();
    }


    public ArticleAdapter(Context context, List<Article> articleList) {
        this.context = context;
        this.articleList = articleList;
        this.savedArticleDAO = new SavedArticleDAO(context);
        this.articleDAO = new ArticleDAO(context);
        this.articleRepo = new ArticleRepository(context);
        this.commentRepo = new CommentRepository();

        // Load saved states for all articles
        loadSavedStates();
    }

    public void updateData(List<Article> newList) {
        this.articleList = newList;
        // Clear caches when data is updated
        this.savedStates.clear();
        this.commentCounts.clear();
        notifyDataSetChanged();
        // Reload saved states and comment counts
        loadSavedStates();
        loadCommentCounts();
    }

    private void loadSavedStates() {
        if (articleList == null || articleList.isEmpty()) return;

        backgroundExecutor.execute(() -> {
            Map<String, Boolean> newSavedStates = new HashMap<>();
            for (Article article : articleList) {
                if (article != null && article.getId() != null) {
                    try {
                        boolean saved = savedArticleDAO != null && savedArticleDAO.isSaved(article.getId());
                        newSavedStates.put(article.getId(), saved);
                    } catch (Exception e) {
                        newSavedStates.put(article.getId(), false);
                    }
                }
            }
            savedStates = newSavedStates;

            // Update UI on main thread
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).runOnUiThread(() -> notifyDataSetChanged());
            }
        });
    }

    private void loadCommentCounts() {
        if (articleList == null || articleList.isEmpty() || commentRepo == null) return;

        for (Article article : articleList) {
            if (article != null && article.getId() != null) {
                String articleId = article.getId();
                // Check cache first
                if (commentCounts.containsKey(articleId)) {
                    continue; // Already loaded
                }

                // Load from Firestore
                commentRepo.getCommentCount(articleId, new CommentRepository.CountCallback() {
                    @Override
                    public void onCount(int count) {
                        commentCounts.put(articleId, count);
                        // Update specific item
                        if (context instanceof android.app.Activity) {
                            ((android.app.Activity) context).runOnUiThread(() -> {
                                int position = findArticlePosition(articleId);
                                if (position >= 0) {
                                    notifyItemChanged(position);
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    private int findArticlePosition(String articleId) {
        if (articleList == null || articleId == null) return -1;
        for (int i = 0; i < articleList.size(); i++) {
            Article article = articleList.get(i);
            if (article != null && articleId.equals(article.getId())) {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_article, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Article article = articleList.get(position);
        if (article == null) {
            return;
        }

        String articleId = article.getId();

        // Author Name
        String authorName = article.getAuthorName();
        if (holder.tvAuthorName != null) {
            holder.tvAuthorName.setText(
                    !TextUtils.isEmpty(authorName) ? authorName : "Tác giả"
            );
        }

        // Title
        String title = article.getTitle();
        if (holder.tvTitle != null) {
            holder.tvTitle.setText(
                    !TextUtils.isEmpty(title) ? title : ""
            );
        }

        // Description
        String description = article.getDescription();
        if (holder.tvDescription != null) {
            if (!TextUtils.isEmpty(description)) {
                holder.tvDescription.setText(description);
                holder.tvDescription.setVisibility(View.VISIBLE);
            } else {
                holder.tvDescription.setVisibility(View.GONE);
            }
        }

        // Date - Use shared DateUtils for consistency
        if (holder.tvDate != null) {
            long publishDate = article.getPublishDate();
            String formattedDate = DateUtils.formatPublishDate(publishDate);
            holder.tvDate.setText(formattedDate);
        }

        // Comment Count - Load from CommentRepository
        if (holder.layoutCommentCount != null && holder.tvCommentCount != null) {
            holder.layoutCommentCount.setVisibility(View.VISIBLE);

            // Check cache first
            Integer cachedCount = commentCounts.get(articleId);
            if (cachedCount != null) {
                holder.tvCommentCount.setText(String.valueOf(cachedCount));
            } else {
                holder.tvCommentCount.setText("0"); // Default to 0 while loading
                // Load asynchronously (already handled in loadCommentCounts, but also load here if missed)
                if (commentRepo != null) {
                    commentRepo.getCommentCount(articleId, new CommentRepository.CountCallback() {
                        @Override
                        public void onCount(int count) {
                            commentCounts.put(articleId, count);
                            if (holder.getAdapterPosition() == position && articleId.equals(article.getId())) {
                                if (holder.tvCommentCount != null) {
                                    holder.tvCommentCount.setText(String.valueOf(count));
                                }
                            }
                        }
                    });
                }
            }
        }

        // Bookmark Icon - Check saved state
        if (holder.imgBookmark != null) {
            holder.imgBookmark.setVisibility(View.VISIBLE);

            // Load initial saved state from cache
            Boolean cachedState = savedStates.get(articleId);
            boolean isSaved = cachedState != null && cachedState;
            updateBookmarkIcon(holder.imgBookmark, isSaved);

            holder.imgBookmark.setOnClickListener(v -> {
                toggleBookmark(article, holder.imgBookmark, position);
            });
        }

        // Thumbnail Image - Resize to prevent "too large bitmap" error
        if (holder.imgThumb != null) {
            String imageUrl = article.getImage();
            if (!TextUtils.isEmpty(imageUrl) &&
                (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
                try {
                    // Resize image for thumbnail (100dp x 70dp from layout)
                    int thumbWidth = 200; // pixels
                    int thumbHeight = 140; // pixels

                    Picasso.get()
                            .load(imageUrl)
                            .resize(thumbWidth, thumbHeight)
                            .onlyScaleDown()
                            .centerCrop()
                            .placeholder(android.R.drawable.ic_menu_report_image)
                            .error(android.R.drawable.ic_menu_report_image)
                            .into(holder.imgThumb);
                } catch (OutOfMemoryError e) {
                    holder.imgThumb.setImageResource(android.R.drawable.ic_menu_report_image);
                } catch (Exception e) {
                    holder.imgThumb.setImageResource(android.R.drawable.ic_menu_report_image);
                }
            } else {
                holder.imgThumb.setImageResource(android.R.drawable.ic_menu_report_image);
            }
        }

        // Author Avatar (if available)
        if (holder.imgAvatarSmall != null) {
            // Use person icon as default placeholder if no avatar available
            holder.imgAvatarSmall.setImageResource(android.R.drawable.ic_menu_myplaces);
            // TODO: Load author avatar from Article model if available in the future
        }

        // Click listener - Navigate to Article Detail
        holder.itemView.setOnClickListener(v -> {
            // Don't navigate if clicking on bookmark
            if (holder.imgBookmark != null && v == holder.imgBookmark) {
                return;
            }
            if (!TextUtils.isEmpty(articleId)) {
                Intent intent = new Intent(context, ArticleDetailActivity.class);
                intent.putExtra("articleId", articleId);
                context.startActivity(intent);
            }
        });
    }

// File: ArticleAdapter.java

    private void toggleBookmark(Article article, ImageView bookmarkIcon, int position) {
        // 1. Kiểm tra User ID
        String currentUserId = FirebaseAuth.getInstance().getUid();

        android.util.Log.e("CHECK_USER_ID", "1. Tại Adapter - UserID là: " + currentUserId);

        if (currentUserId == null) {
            Toast.makeText(context, "Vui lòng đăng nhập để lưu tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- [ĐOẠN CODE CỨU CÁNH - CHỐNG CRASH] ---
        // Nếu savedRepo chưa có (bị null), ta tạo mới ngay lập tức
        if (savedRepo == null) {
            android.util.Log.e("FIX_BUG", "savedRepo đang NULL -> Đang tự động khởi tạo lại...");
            if (context != null) {
                savedRepo = new SavedArticleRepository(context);
            } else {
                android.util.Log.e("FIX_BUG", "Lỗi nặng: Context cũng NULL, không thể lưu!");
                return;
            }
        }
        // ------------------------------------------

        String articleId = article.getId();

        // 2. Lấy trạng thái hiện tại (Thêm getOrDefault để tránh null)
        Boolean currentState = savedStates.get(articleId);
        boolean isCurrentlySaved = currentState != null && currentState;

        // 3. Đảo ngược trạng thái
        boolean newStatus = !isCurrentlySaved;

        if (newStatus) {
            // --- LƯU BÀI ---
            bookmarkIcon.setImageResource(R.drawable.bookmarks);
            // Lưu ý: check kỹ màu sắc, nếu không có màu bookmark_saved thì dùng màu đỏ/xanh cứng
            try {
                bookmarkIcon.setColorFilter(context.getResources().getColor(R.color.bookmark_saved));
            } catch (Exception e) { /* Bỏ qua lỗi màu nếu chưa định nghĩa */ }

            android.util.Log.e("CHECK_FLOW", ">>> Đang gọi lệnh SAVE...");
            savedRepo.save(currentUserId, articleId); // Giờ chắc chắn savedRepo không null nữa

        } else {
            // --- BỎ LƯU ---
            bookmarkIcon.setImageResource(R.drawable.bookmark_simple);
            try {
                bookmarkIcon.setColorFilter(context.getResources().getColor(R.color.bookmark_default));
            } catch (Exception e) { /* Bỏ qua */ }

            savedRepo.unsave(currentUserId, articleId);
        }

        // 4. Cập nhật lại bộ nhớ đệm
        savedStates.put(articleId, newStatus);
    }
    private void updateBookmarkIcon(ImageView icon, boolean isSaved) {
        if (icon == null) return;

        try {
            if (isSaved) {
                icon.setImageResource(R.drawable.bookmarks); // Solid icon
                icon.setColorFilter(context.getResources().getColor(R.color.bookmark_saved));
            } else {
                icon.setImageResource(R.drawable.bookmark_simple); // Outline icon
                icon.setColorFilter(context.getResources().getColor(R.color.bookmark_default));
            }
        } catch (Exception e) {
            // Ignore errors
        }
    }

    @Override
    public int getItemCount() {
        return articleList == null ? 0 : articleList.size();
    }

    public static class ArticleViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatarSmall;
        TextView tvAuthorName;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvDate;
        ImageView imgThumb;
        LinearLayout layoutCommentCount;
        ImageView imgComment;
        TextView tvCommentCount;
        ImageView imgBookmark;
        ImageView imgDelete; // For history tab only

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatarSmall = itemView.findViewById(R.id.imgAvatarSmall);
            tvAuthorName = itemView.findViewById(R.id.tvAuthorName);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            imgThumb = itemView.findViewById(R.id.imgArticleThumb);
            layoutCommentCount = itemView.findViewById(R.id.layoutCommentCount);
            imgComment = itemView.findViewById(R.id.imgComment);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            imgBookmark = itemView.findViewById(R.id.imgBookmark);
            imgDelete = itemView.findViewById(R.id.imgDelete);
        }
    }
}
