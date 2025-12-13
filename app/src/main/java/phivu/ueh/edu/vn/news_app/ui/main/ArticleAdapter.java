package phivu.ueh.edu.vn.news_app.ui.main;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.model.Article;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {

    private List<Article> articleList;
    private Context context;

    public ArticleAdapter(Context context, List<Article> articleList) {
        this.context = context;
        this.articleList = articleList;
    }

    public void updateData(List<Article> newList) {
        this.articleList = newList;
        notifyDataSetChanged();
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

        holder.tvTitle.setText(article.getTitle());
        holder.tvDescription.setText(article.getDescription());

        // ✅ HIỂN THỊ NGÀY ĐĂNG
        long publishDate = article.getPublishDate();
        if (publishDate > 0) {
            Date date = new Date(publishDate);
            SimpleDateFormat sdf =
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvDate.setText(sdf.format(date));
        } else {
            holder.tvDate.setText("");
        }

        // Image
        if (article.getImage() != null && !article.getImage().isEmpty()) {
            Picasso.get()
                    .load(article.getImage())
                    .placeholder(android.R.drawable.ic_menu_report_image)
                    .into(holder.imgThumb);
        } else {
            holder.imgThumb.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ArticleDetailActivity.class);
            intent.putExtra("articleId", article.getId());
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return articleList == null ? 0 : articleList.size();
    }

    public static class ArticleViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvDate;
        ImageView imgThumb;


        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            imgThumb = itemView.findViewById(R.id.imgArticleThumb);

        }
    }
}
