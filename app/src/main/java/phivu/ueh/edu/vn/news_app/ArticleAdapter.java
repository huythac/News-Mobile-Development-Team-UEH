package phivu.ueh.edu.vn.news_app;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import phivu.ueh.edu.vn.news_app.article.Article;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {

    private List<Article> articleList;
    private Context context;

    public ArticleAdapter(List<Article> articleList, Context context) {
        this.articleList = articleList;
        this.context = context;
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

        // 1. Gán dữ liệu Text
        holder.tvTitle.setText(article.getTitle());
        holder.tvDescription.setText(article.getDescription());
        holder.tvDate.setText(article.getCreatedAt());

        // Tạm thời hardcode tên tác giả (hoặc lấy từ model nếu có)
        holder.tvAuthorName.setText("Pham Thanh Dat");

        // Set số comment (Giả lập hoặc lấy từ DB)
        // Lưu ý: Icon comment đã được set bằng drawableStart trong XML
        holder.tvCommentCount.setText("120");

        // 2. Xử lý hình ảnh (Nếu chưa có thư viện ảnh thì set ảnh mặc định)
        holder.imgArticleThumb.setImageResource(R.drawable.ic_launcher_background);

        // 3. QUAN TRỌNG: Sự kiện bấm vào bài viết -> Mở màn hình Chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ArticleDetailActivity.class);
            intent.putExtra("ARTICLE_ID", article.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }

    public static class ArticleViewHolder extends RecyclerView.ViewHolder {
        // Khai báo đúng các View có trong item_article.xml MỚI
        TextView tvTitle, tvDescription, tvAuthorName, tvDate, tvCommentCount;
        ImageView imgArticleThumb, imgAvatarSmall, imgBookmark;

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ ID chuẩn
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAuthorName = itemView.findViewById(R.id.tvAuthorName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount); // Đã thêm ID này
            imgArticleThumb = itemView.findViewById(R.id.imgArticleThumb);
            imgAvatarSmall = itemView.findViewById(R.id.imgAvatarSmall);
            imgBookmark = itemView.findViewById(R.id.imgBookmark);

            // Đã XÓA dòng tvCategoryTag gây lỗi
        }
    }
}