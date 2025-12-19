package phivu.ueh.edu.vn.news_app.ui.main;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.model.Comment;
import phivu.ueh.edu.vn.news_app.model.User;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;
    private Context context;

    public CommentAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    public void updateData(List<Comment> newList) {
        this.commentList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        if (comment == null) {
            return;
        }

        User user = comment.getUser();

        if (holder.tvCommentName != null) {
            String fullName = "Người dùng";
            if (user != null && !TextUtils.isEmpty(user.getFullName())) {
                fullName = user.getFullName();
            }
            holder.tvCommentName.setText(fullName);
        }

        if (holder.tvCommentDate != null) {
            long timestamp = comment.getCreatedAt();
            if (timestamp > 0) {
                holder.tvCommentDate.setText(formatCommentDate(timestamp));
            } else {
                holder.tvCommentDate.setText("—");
            }
        }

        String content = comment.getContent();
        if (holder.tvCommentContent != null) {
            holder.tvCommentContent.setText(
                    !TextUtils.isEmpty(content) ? content : ""
            );
        }

        if (holder.imgCommentAvatar != null) {
            String avatarUrl = "";
            if (user != null) {
                avatarUrl = user.getAvatar();
            }

            // Kiểm tra và load ảnh
            if (!TextUtils.isEmpty(avatarUrl) &&
                    (avatarUrl.startsWith("http://") || avatarUrl.startsWith("https://"))) {
                try {
                    int avatarSize = 100;
                    try {
                        avatarSize = context.getResources().getDimensionPixelSize(R.dimen.comments_item_avatar_size);
                    } catch (Exception ignored) {}

                    Picasso.get()
                            .load(avatarUrl)
                            .resize(avatarSize, avatarSize)
                            .onlyScaleDown()
                            .centerCrop()
                            .placeholder(android.R.drawable.ic_menu_report_image)
                            .error(android.R.drawable.ic_menu_report_image)
                            .into(holder.imgCommentAvatar);
                } catch (Exception e) {
                    holder.imgCommentAvatar.setImageResource(android.R.drawable.ic_menu_report_image);
                }
            } else {
                holder.imgCommentAvatar.setImageResource(android.R.drawable.ic_menu_report_image);
            }
        }
    }

    @Override
    public int getItemCount() {
        return commentList != null ? commentList.size() : 0;
    }

    private String formatCommentDate(long timestamp) {
        try {
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("d 'tháng' M, yyyy", new Locale("vi", "VN"));
            return sdf.format(date);
        } catch (Exception e) {
            return "—";
        }
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCommentAvatar;
        TextView tvCommentName;
        TextView tvCommentDate;
        TextView tvCommentContent;
        ImageView btnCommentOptions;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCommentAvatar = itemView.findViewById(R.id.imgCommentAvatar);
            tvCommentName = itemView.findViewById(R.id.tvCommentName);
            tvCommentDate = itemView.findViewById(R.id.tvCommentDate);
            tvCommentContent = itemView.findViewById(R.id.tvCommentContent);
            btnCommentOptions = itemView.findViewById(R.id.btnCommentOptions);
        }
    }
}