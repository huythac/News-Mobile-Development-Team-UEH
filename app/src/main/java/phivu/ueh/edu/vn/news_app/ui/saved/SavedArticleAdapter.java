package phivu.ueh.edu.vn.news_app.ui.saved;

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

import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.data.local.article.ArticleDAO;
import phivu.ueh.edu.vn.news_app.data.local.history.ReadHistoryDAO;
import phivu.ueh.edu.vn.news_app.data.local.saved.SavedArticleDAO;
import phivu.ueh.edu.vn.news_app.data.repository.ArticleRepository;
import phivu.ueh.edu.vn.news_app.data.repository.CommentRepository;
import phivu.ueh.edu.vn.news_app.ui.main.ArticleDetailActivity;
import phivu.ueh.edu.vn.news_app.model.Article;
import phivu.ueh.edu.vn.news_app.utils.DateUtils;

public class SavedArticleAdapter extends RecyclerView.Adapter<SavedArticleAdapter.SavedArticleViewHolder> {

    private List<Article> articleList;
    private Context context;
    private ReadHistoryDAO readHistoryDAO;
    private SavedArticleDAO savedArticleDAO;
    private ArticleDAO articleDAO;
    private ArticleRepository articleRepo;
    private CommentRepository commentRepo;
    private boolean isHistoryTab;
    private OnItemDeletedListener deleteListener;
    private ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    
    // Cache for saved states and comment counts
    private Map<String, Boolean> savedStates = new HashMap<>();
    private Map<String, Integer> commentCounts = new HashMap<>();

    public interface OnItemDeletedListener {
        void onItemDeleted(String articleId);
    }

    public SavedArticleAdapter(Context context, List<Article> articleList, boolean isHistoryTab) {
        this.context = context;
        this.articleList = articleList;
        this.isHistoryTab = isHistoryTab;
        this.savedArticleDAO = new SavedArticleDAO(context);
        this.articleDAO = new ArticleDAO(context);
        this.articleRepo = new ArticleRepository(context);
        this.commentRepo = new CommentRepository();
        if (isHistoryTab) {
            this.readHistoryDAO = new ReadHistoryDAO(context);
        }
        
        // Load saved states and comment counts
        loadSavedStates();
        loadCommentCounts();
    }

    public void setOnItemDeletedListener(OnItemDeletedListener listener) {
        this.deleteListener = listener;
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
        if (articleList == null || articleList.isEmpty() || savedArticleDAO == null) return;
        
        backgroundExecutor.execute(() -> {
            Map<String, Boolean> newSavedStates = new HashMap<>();
            for (Article article : articleList) {
                if (article != null && article.getId() != null) {
                    try {
                        boolean saved = savedArticleDAO.isSaved(article.getId());
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
    public SavedArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_article, parent, false);
        return new SavedArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedArticleViewHolder holder, int position) {
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
                // Load asynchronously
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

        // Bookmark Icon - Only show for "Lưu" tab
        if (holder.imgBookmark != null) {
            if (isHistoryTab) {
                // History tab: Hide bookmark
                holder.imgBookmark.setVisibility(View.GONE);
            } else {
                // Saved tab: Show bookmark with save/unsave logic
                holder.imgBookmark.setVisibility(View.VISIBLE);
                
                // Load initial saved state from cache
                Boolean cachedState = savedStates.get(articleId);
                boolean isSaved = cachedState != null && cachedState;
                updateBookmarkIcon(holder.imgBookmark, isSaved);
                
                holder.imgBookmark.setOnClickListener(v -> {
                    toggleBookmark(article, holder.imgBookmark, position);
                });
            }
        }

        // Delete Icon - Only show for "Lịch sử đọc" tab
        if (holder.imgDelete != null) {
            if (isHistoryTab) {
                // History tab: Show delete icon
                holder.imgDelete.setVisibility(View.VISIBLE);
                holder.imgDelete.setOnClickListener(v -> {
                    if (!TextUtils.isEmpty(articleId) && readHistoryDAO != null) {
                        deleteFromHistory(articleId, position);
                    }
                });
            } else {
                // Saved tab: Hide delete icon
                holder.imgDelete.setVisibility(View.GONE);
            }
        }

        // Thumbnail Image
        if (holder.imgThumb != null) {
            String imageUrl = article.getImage();
            if (!TextUtils.isEmpty(imageUrl) &&
                (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
                try {
                    int thumbWidth = 200;
                    int thumbHeight = 140;
                    
                    Picasso.get()
                            .load(imageUrl)
                            .resize(thumbWidth, thumbHeight)
                            .onlyScaleDown()
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
        }

        // Author Avatar
        if (holder.imgAvatarSmall != null) {
            // Use person icon as default placeholder if no avatar available
            holder.imgAvatarSmall.setImageResource(android.R.drawable.ic_menu_myplaces);
            // TODO: Load author avatar from Article model if available in the future
        }

        // Click listener - Navigate to Article Detail
        holder.itemView.setOnClickListener(v -> {
            // Don't navigate if clicking on bookmark or delete
            if ((holder.imgBookmark != null && v == holder.imgBookmark) ||
                (holder.imgDelete != null && v == holder.imgDelete)) {
                return;
            }
            if (!TextUtils.isEmpty(articleId)) {
                Intent intent = new Intent(context, ArticleDetailActivity.class);
                intent.putExtra("articleId", articleId);
                context.startActivity(intent);
            }
        });
    }

    private void toggleBookmark(Article article, ImageView bookmarkIcon, int position) {
        if (article == null || article.getId() == null || savedArticleDAO == null) {
            return;
        }

        String articleId = article.getId();
        Boolean currentState = savedStates.get(articleId);
        boolean isCurrentlySaved = currentState != null && currentState;

        backgroundExecutor.execute(() -> {
            try {
                if (isCurrentlySaved) {
                    // Unsave: Remove from SavedArticle table
                    savedArticleDAO.unsaveArticle(articleId);
                    savedStates.put(articleId, false);

                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            updateBookmarkIcon(bookmarkIcon, false);
                            Toast.makeText(context, "Đã bỏ lưu bài viết", Toast.LENGTH_SHORT).show();
                            // Remove from list if in Saved tab
                            if (!isHistoryTab) {
                                articleList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, articleList.size());
                            }
                        });
                    }
                } else {
                    // Save: Ensure article exists in Article table first, then add to SavedArticle
                    Article existing = articleDAO.getById(articleId);
                    if (existing == null || existing.getContent() == null || existing.getContent().trim().isEmpty()) {
                        // Article not in cache or missing content, fetch from Firestore first
                        articleRepo.syncArticleFromRemote(articleId, new ArticleRepository.SingleCallback() {
                            @Override
                            public void onSuccess(Article syncedArticle) {
                                if (syncedArticle != null && savedArticleDAO != null) {
                                    backgroundExecutor.execute(() -> {
                                        try {
                                            savedArticleDAO.saveArticle(articleId);
                                            savedStates.put(articleId, true);
                                            if (context instanceof android.app.Activity) {
                                                ((android.app.Activity) context).runOnUiThread(() -> {
                                                    updateBookmarkIcon(bookmarkIcon, true);
                                                    Toast.makeText(context, "Đã lưu bài viết", Toast.LENGTH_SHORT).show();
                                                });
                                            }
                                        } catch (Exception e) {
                                            if (context instanceof android.app.Activity) {
                                                ((android.app.Activity) context).runOnUiThread(() -> {
                                                    Toast.makeText(context, "Lỗi khi lưu bài viết", Toast.LENGTH_SHORT).show();
                                                });
                                            }
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onError(String err) {
                                if (context instanceof android.app.Activity) {
                                    ((android.app.Activity) context).runOnUiThread(() -> {
                                        Toast.makeText(context, "Không thể tải bài viết để lưu", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            }
                        });
                    } else {
                        // Article exists, just save to SavedArticle
                        savedArticleDAO.saveArticle(articleId);
                        savedStates.put(articleId, true);

                        if (context instanceof android.app.Activity) {
                            ((android.app.Activity) context).runOnUiThread(() -> {
                                updateBookmarkIcon(bookmarkIcon, true);
                                Toast.makeText(context, "Đã lưu bài viết", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                }
            } catch (Exception e) {
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "Lỗi khi lưu bài viết", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
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

    private void deleteFromHistory(String articleId, int position) {
        if (readHistoryDAO == null || TextUtils.isEmpty(articleId)) {
            return;
        }

        // Delete from history on background thread
        backgroundExecutor.execute(() -> {
            try {
                readHistoryDAO.deleteHistory(articleId);
                // Remove from list
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        articleList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, articleList.size());
                        
                        if (deleteListener != null) {
                            deleteListener.onItemDeleted(articleId);
                        }
                        
                        Toast.makeText(context, "Đã xóa khỏi lịch sử", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "Lỗi khi xóa", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return articleList != null ? articleList.size() : 0;
    }

    static class SavedArticleViewHolder extends RecyclerView.ViewHolder {
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
        ImageView imgDelete;

        public SavedArticleViewHolder(@NonNull View itemView) {
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
