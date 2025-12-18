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

import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.data.local.article.ArticleDAO;
import phivu.ueh.edu.vn.news_app.data.local.saved.SavedArticleDAO;
import phivu.ueh.edu.vn.news_app.data.repository.ArticleRepository;
import phivu.ueh.edu.vn.news_app.model.Article;

public class RelatedArticlesAdapter extends RecyclerView.Adapter<RelatedArticlesAdapter.RelatedArticleViewHolder> {

    private List<Article> articleList;
    private Context context;
    private ArticleDAO articleDAO;
    private SavedArticleDAO savedArticleDAO;
    private ArticleRepository articleRepo;
    private FirebaseFirestore firestore;
    private ExecutorService backgroundExecutor;
    
    // Cache for saved states and comment counts
    private Map<String, Boolean> savedStates = new HashMap<>();
    private Map<String, Integer> commentCounts = new HashMap<>();

    public RelatedArticlesAdapter(Context context, List<Article> articleList) {
        this.context = context;
        this.articleList = articleList;
        this.articleDAO = new ArticleDAO(context);
        this.savedArticleDAO = new SavedArticleDAO(context);
        this.articleRepo = new ArticleRepository(context);
        this.firestore = FirebaseFirestore.getInstance();
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
        
        // Load saved states for all articles
        loadSavedStates();
    }

    public void updateData(List<Article> newList) {
        this.articleList = newList;
        // Reload saved states when data changes
        loadSavedStates();
        notifyDataSetChanged();
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

    @NonNull
    @Override
    public RelatedArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_article_related, parent, false);
        return new RelatedArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RelatedArticleViewHolder holder, int position) {
        Article article = articleList.get(position);
        if (article == null) {
            return;
        }

        String articleId = article.getId();
        if (TextUtils.isEmpty(articleId)) {
            return;
        }

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

        // Date - Format: "Thứ 6, 28 tháng 11, 2025"
        if (holder.tvDate != null) {
            long publishDate = article.getPublishDate();
            if (publishDate > 0) {
                try {
                    Date date = new Date(publishDate);
                    String formattedDate = formatDateVietnamese(date);
                    holder.tvDate.setText(formattedDate);
                } catch (Exception e) {
                    holder.tvDate.setText("");
                }
            } else {
                holder.tvDate.setText("");
            }
        }

        // Comment Count - Query from Firestore (always show, even if 0)
        if (holder.layoutCommentCount != null && holder.tvCommentCount != null) {
            // Always show layout
            holder.layoutCommentCount.setVisibility(View.VISIBLE);
            
            // Check cache first
            Integer cachedCount = commentCounts.get(articleId);
            if (cachedCount != null) {
                holder.tvCommentCount.setText(String.valueOf(cachedCount));
            } else {
                // Show 0 while loading
                holder.tvCommentCount.setText("0");
                // Query from Firestore
                loadCommentCount(articleId, count -> {
                    if (holder.getAdapterPosition() == position && articleId.equals(article.getId())) {
                        commentCounts.put(articleId, count);
                        if (holder.tvCommentCount != null) {
                            holder.tvCommentCount.setText(String.valueOf(count));
                        }
                    }
                });
            }
        }

        // Bookmark Icon - Check saved state
        if (holder.imgBookmark != null) {
            Boolean isSaved = savedStates.get(articleId);
            if (isSaved == null) {
                isSaved = false;
            }
            updateBookmarkIcon(holder.imgBookmark, isSaved);
        }

        // Header Image - Resize to prevent "too large bitmap" error
        if (holder.imgHeader != null) {
            String imageUrl = article.getImage();
            if (!TextUtils.isEmpty(imageUrl) &&
                (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
                try {
                    // Get screen width for header image (200dp height in layout)
                    int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
                    int imageHeight = (int) (200 * context.getResources().getDisplayMetrics().density);
                    
                    Picasso.get()
                            .load(imageUrl)
                            .resize(screenWidth, imageHeight)
                            .onlyScaleDown()
                            .centerCrop()
                            .placeholder(android.R.drawable.ic_menu_report_image)
                            .error(android.R.drawable.ic_menu_report_image)
                            .into(holder.imgHeader);
                } catch (OutOfMemoryError e) {
                    holder.imgHeader.setImageResource(android.R.drawable.ic_menu_report_image);
                } catch (Exception e) {
                    holder.imgHeader.setImageResource(android.R.drawable.ic_menu_report_image);
                }
            } else {
                holder.imgHeader.setImageResource(android.R.drawable.ic_menu_report_image);
            }
        }

        // Bookmark click listener (set first to prevent item click)
        if (holder.imgBookmark != null) {
            holder.imgBookmark.setOnClickListener(v -> {
                v.setTag("bookmark_click");
                toggleBookmark(article, holder.imgBookmark, position);
            });
        }
        
        // Click listener - Navigate to Article Detail (but not when clicking bookmark)
        holder.itemView.setOnClickListener(v -> {
            // Check if click originated from bookmark
            if (holder.imgBookmark != null && 
                (v == holder.imgBookmark || "bookmark_click".equals(v.getTag()))) {
                return;
            }
            String id = article.getId();
            if (!TextUtils.isEmpty(id)) {
                Intent intent = new Intent(context, ArticleDetailActivity.class);
                intent.putExtra("articleId", id);
                context.startActivity(intent);
            }
        });
    }

    private void loadCommentCount(String articleId, CommentCountCallback callback) {
        if (TextUtils.isEmpty(articleId) || firestore == null) {
            if (callback != null) callback.onCount(0);
            return;
        }

        firestore.collection("articles")
                .document(articleId)
                .collection("comments")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot != null ? querySnapshot.size() : 0;
                    if (callback != null) {
                        callback.onCount(count);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onCount(0);
                    }
                });
    }

    private void toggleBookmark(Article article, ImageView bookmarkIcon, int position) {
        if (article == null || article.getId() == null) {
            return;
        }

        String articleId = article.getId();
        Boolean currentState = savedStates.get(articleId);
        boolean isCurrentlySaved = currentState != null && currentState;

        backgroundExecutor.execute(() -> {
            try {
                if (isCurrentlySaved) {
                    // Unsave: Remove from SavedArticle table
                    if (savedArticleDAO != null) {
                        savedArticleDAO.unsaveArticle(articleId);
                    }
                    savedStates.put(articleId, false);
                    
                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            updateBookmarkIcon(bookmarkIcon, false);
                            Toast.makeText(context, "Đã bỏ lưu bài viết", Toast.LENGTH_SHORT).show();
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
                        if (savedArticleDAO != null) {
                            savedArticleDAO.saveArticle(articleId);
                        }
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
                // Solid bookmark with green color
                icon.setImageResource(R.drawable.bookmarks);
                icon.setColorFilter(context.getResources().getColor(R.color.bookmark_saved));
            } else {
                // Outline bookmark with gray color
                icon.setImageResource(R.drawable.bookmark_simple);
                icon.setColorFilter(context.getResources().getColor(R.color.bookmark_default));
            }
        } catch (Exception e) {
            // Ignore errors
        }
    }

    private String formatDateVietnamese(Date date) {
        if (date == null) {
            return "";
        }
        
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            
            // Day of week names in Vietnamese
            String[] dayNames = {"Chủ nhật", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7"};
            // Month names in Vietnamese
            String[] monthNames = {"tháng 1", "tháng 2", "tháng 3", "tháng 4", "tháng 5", "tháng 6",
                    "tháng 7", "tháng 8", "tháng 9", "tháng 10", "tháng 11", "tháng 12"};
            
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);
            
            String dayName = dayNames[dayOfWeek - 1];
            String monthName = monthNames[month];
            
            return String.format(Locale.getDefault(), "%s, %d %s, %d", dayName, day, monthName, year);
        } catch (Exception e) {
            // Fallback to simple format
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
            return sdf.format(date);
        }
    }

    @Override
    public int getItemCount() {
        return articleList != null ? articleList.size() : 0;
    }

    interface CommentCountCallback {
        void onCount(int count);
    }

    static class RelatedArticleViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthorName;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvDate;
        ImageView imgHeader;
        LinearLayout layoutCommentCount;
        ImageView imgCommentIcon;
        TextView tvCommentCount;
        ImageView imgBookmark;

        public RelatedArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthorName = itemView.findViewById(R.id.tvRelatedAuthorName);
            tvTitle = itemView.findViewById(R.id.tvRelatedTitle);
            tvDescription = itemView.findViewById(R.id.tvRelatedDescription);
            tvDate = itemView.findViewById(R.id.tvRelatedDate);
            imgHeader = itemView.findViewById(R.id.imgRelatedHeader);
            layoutCommentCount = itemView.findViewById(R.id.layoutCommentCount);
            imgCommentIcon = itemView.findViewById(R.id.imgRelatedCommentIcon);
            tvCommentCount = itemView.findViewById(R.id.tvRelatedCommentCount);
            imgBookmark = itemView.findViewById(R.id.imgRelatedBookmark);
        }
    }
}