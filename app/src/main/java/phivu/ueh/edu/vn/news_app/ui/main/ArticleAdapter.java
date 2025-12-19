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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.data.local.article.ArticleDAO;
import phivu.ueh.edu.vn.news_app.data.local.history.ReadHistoryDAO; // Nhớ import cái này
import phivu.ueh.edu.vn.news_app.data.local.saved.SavedArticleDAO;
import phivu.ueh.edu.vn.news_app.data.repository.ArticleRepository;
import phivu.ueh.edu.vn.news_app.data.repository.CommentRepository;
import phivu.ueh.edu.vn.news_app.data.repository.SavedArticleRepository;
import phivu.ueh.edu.vn.news_app.model.Article;
import phivu.ueh.edu.vn.news_app.utils.DateUtils;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {

    private Context context;
    private List<Article> articleList;
    private Mode mode;
    private String userId;

    // --- CÁC REPOSITORY & DAO ---
    private SavedArticleRepository savedRepo;
    private ReadHistoryDAO historyDAO; // Dùng cho lịch sử
    private ArticleDAO articleDAO;
    private CommentRepository commentRepo;
    private SavedArticleDAO savedArticleDAO; // Dùng để check nhanh local

    // --- CÔNG CỤ HỖ TRỢ ---
    private ExecutorService executor;
    private Map<String, Boolean> savedStates;
    private Map<String, Integer> commentCounts;

    public enum Mode {
        NORMAL,
        HISTORY
    }

    // =========================================================
    // HÀM KHỞI TẠO (CONSTRUCTOR) - ĐÃ CHUẨN HÓA
    // =========================================================
    public ArticleAdapter(Context context, List<Article> articleList, Mode mode) {
        this.context = context;
        this.articleList = articleList;
        this.mode = mode;

        // 1. Khởi tạo User
        this.userId = FirebaseAuth.getInstance().getUid();

        // 2. Khởi tạo các Repository (BẮT BUỘC PHẢI NEW)
        this.articleDAO = new ArticleDAO(context);
        this.commentRepo = new CommentRepository();
        this.savedRepo = new SavedArticleRepository(context);
        this.historyDAO = new ReadHistoryDAO(context); // Khởi tạo DAO Lịch sử
        this.savedArticleDAO = new SavedArticleDAO(context);

        // 3. Khởi tạo công cụ hỗ trợ (BẮT BUỘC)
        this.executor = Executors.newSingleThreadExecutor();
        this.savedStates = new HashMap<>();
        this.commentCounts = new HashMap<>();

        // 4. Load dữ liệu ban đầu
        if (this.mode == Mode.NORMAL) {
            loadSavedStates();
        }
        loadCommentCounts();
    }

    // Constructor phụ (Mặc định là NORMAL)
    public ArticleAdapter(Context context, List<Article> articleList) {
        this(context, articleList, Mode.NORMAL);
    }

    // =========================================================
    // CÁC HÀM LOAD DỮ LIỆU
    // =========================================================
    public void updateData(List<Article> newList) {
        this.articleList = newList;
        this.savedStates.clear();
        this.commentCounts.clear();
        notifyDataSetChanged();

        if (this.mode == Mode.NORMAL) {
            loadSavedStates();
        }
        loadCommentCounts();
    }

    private void loadSavedStates() {
        if (articleList == null || articleList.isEmpty()) return;

        executor.execute(() -> {
            Map<String, Boolean> newSavedStates = new HashMap<>();
            for (Article article : articleList) {
                if (article != null && article.getId() != null) {
                    try {
                        // Check trực tiếp từ DAO cho nhanh
                        boolean saved = savedArticleDAO.isSaved(article.getId());
                        newSavedStates.put(article.getId(), saved);
                    } catch (Exception e) {
                        newSavedStates.put(article.getId(), false);
                    }
                }
            }
            // Update UI
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).runOnUiThread(() -> {
                    savedStates.putAll(newSavedStates);
                    notifyDataSetChanged();
                });
            }
        });
    }

    private void loadCommentCounts() {
        if (articleList == null || articleList.isEmpty() || commentRepo == null) return;

        for (Article article : articleList) {
            if (article != null && article.getId() != null) {
                String articleId = article.getId();
                if (commentCounts.containsKey(articleId)) continue;

                commentRepo.getCommentCount(articleId, count -> {
                    commentCounts.put(articleId, count);
                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            // Chỉ update item cụ thể để tối ưu
                            int pos = findArticlePosition(articleId);
                            if (pos >= 0) notifyItemChanged(pos);
                        });
                    }
                });
            }
        }
    }

    private int findArticlePosition(String articleId) {
        if (articleList == null || articleId == null) return -1;
        for (int i = 0; i < articleList.size(); i++) {
            if (articleList.get(i).getId().equals(articleId)) return i;
        }
        return -1;
    }

    // =========================================================
    // VIEW HOLDER & BINDING
    // =========================================================
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
        if (article == null) return;

        String articleId = article.getId();

        // 1. Gán dữ liệu Text
        holder.tvAuthorName.setText(TextUtils.isEmpty(article.getAuthorName()) ? "Tác giả" : article.getAuthorName());
        holder.tvTitle.setText(TextUtils.isEmpty(article.getTitle()) ? "" : article.getTitle());

        if (!TextUtils.isEmpty(article.getDescription())) {
            holder.tvDescription.setText(article.getDescription());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        holder.tvDate.setText(DateUtils.formatPublishDate(article.getPublishDate()));

        // 2. Gán Comment Count
        holder.layoutCommentCount.setVisibility(View.VISIBLE);
        Integer cachedCount = commentCounts.get(articleId);
        holder.tvCommentCount.setText(cachedCount != null ? String.valueOf(cachedCount) : "0");

        // 3. Load Ảnh (Picasso)
        if (!TextUtils.isEmpty(article.getImage())) {
            try {
                Picasso.get().load(article.getImage())
                        .resize(200, 140)
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_report_image)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(holder.imgThumb);
            } catch (Exception e) {
                holder.imgThumb.setImageResource(android.R.drawable.ic_menu_report_image);
            }
        } else {
            holder.imgThumb.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        // 4. Xử lý Logic theo Tab (Lưu hay Lịch sử)
        if (mode == Mode.HISTORY) {
            // --- TAB LỊCH SỬ ---
            holder.imgBookmark.setVisibility(View.GONE);
            holder.imgDelete.setVisibility(View.VISIBLE);

            // Nút Xóa lịch sử
            holder.imgDelete.setOnClickListener(v -> {
                if (historyDAO != null) {
                    historyDAO.deleteHistory(articleId);
                    articleList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, articleList.size());
                    Toast.makeText(context, "Đã xóa khỏi lịch sử", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            // --- TAB BÌNH THƯỜNG / ĐÃ LƯU ---
            holder.imgBookmark.setVisibility(View.VISIBLE);
            holder.imgDelete.setVisibility(View.GONE);

            Boolean isSaved = savedStates.get(articleId);
            updateBookmarkIcon(holder.imgBookmark, isSaved != null && isSaved);

            holder.imgBookmark.setOnClickListener(v -> {
                toggleBookmark(article, holder.imgBookmark, position);
            });
        }

        // 5. SỰ KIỆN CLICK VÀO BÀI BÁO (QUAN TRỌNG)
        holder.itemView.setOnClickListener(v -> {
            // Tránh click nhầm vào bookmark
            if (holder.imgBookmark != null && v == holder.imgBookmark) return;

            if (!TextUtils.isEmpty(articleId)) {
                // A. LƯU LỊCH SỬ (Chạy ngầm)
                if (executor != null && historyDAO != null) {
                    executor.execute(() -> historyDAO.addToHistory(articleId));
                }

                // B. CHUYỂN MÀN HÌNH CHI TIẾT
                Intent intent = new Intent(context, ArticleDetailActivity.class); // Thay bằng tên Activity chi tiết của bạn
                intent.putExtra("articleId", articleId);
                // Truyền thêm url nếu cần: intent.putExtra("articleUrl", article.getUrl());
                context.startActivity(intent);
            }
        });
    }

    // =========================================================
    // HÀM XỬ LÝ LƯU BÀI VIẾT (ĐÃ FIX CRASH)
    // =========================================================
    private void toggleBookmark(Article article, ImageView bookmarkIcon, int position) {
        String currentUserId = FirebaseAuth.getInstance().getUid();

        if (currentUserId == null) {
            Toast.makeText(context, "Vui lòng đăng nhập để lưu tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- CẦU CHÌ BẢO VỆ: Tự khởi tạo nếu bị null ---
        if (savedRepo == null) {
            android.util.Log.e("FIX_BUG", "savedRepo NULL -> Tự động khởi tạo...");
            if (context != null) savedRepo = new SavedArticleRepository(context);
            else return;
        }

        String articleId = article.getId();
        Boolean currentState = savedStates.get(articleId);
        boolean isCurrentlySaved = currentState != null && currentState;
        boolean newStatus = !isCurrentlySaved;

        if (newStatus) {
            // LƯU
            updateBookmarkIcon(bookmarkIcon, true);
            android.util.Log.e("CHECK_FLOW", ">>> Đang gọi SAVE...");
            savedRepo.save(currentUserId, articleId);
        } else {
            // BỎ LƯU
            updateBookmarkIcon(bookmarkIcon, false);
            savedRepo.unsave(currentUserId, articleId);
        }

        savedStates.put(articleId, newStatus);
    }

    private void updateBookmarkIcon(ImageView icon, boolean isSaved) {
        if (icon == null) return;
        try {
            if (isSaved) {
                icon.setImageResource(R.drawable.bookmarks);
                icon.setColorFilter(context.getResources().getColor(R.color.bookmark_saved));
            } else {
                icon.setImageResource(R.drawable.bookmark_simple);
                icon.setColorFilter(context.getResources().getColor(R.color.bookmark_default));
            }
        } catch (Exception e) { /* Ignore */ }
    }

    @Override
    public int getItemCount() {
        return articleList == null ? 0 : articleList.size();
    }

    // =========================================================
    // VIEW HOLDER
    // =========================================================
    public static class ArticleViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatarSmall, imgThumb, imgComment, imgBookmark, imgDelete;
        TextView tvAuthorName, tvTitle, tvDescription, tvDate, tvCommentCount;
        LinearLayout layoutCommentCount;

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