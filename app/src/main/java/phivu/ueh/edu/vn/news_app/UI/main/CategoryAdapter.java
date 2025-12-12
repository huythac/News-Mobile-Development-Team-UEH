package phivu.ueh.edu.vn.news_app.UI.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.model.Category;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList;
    private Context context;

    public CategoryAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_category, parent, false); // đổi sang item_category.xml
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category c = categoryList.get(position);

        holder.tvCategoryTitle.setText(c.getName());
        holder.tvCategoryDescription.setText(c.getDescription());

        // ICON — bạn có thể chỉnh sau
        holder.imgCategory.setImageResource(R.drawable.ic_category_placeholder);

        // FOLLOW BUTTON
        holder.btnFollow.setOnClickListener(v -> {
            holder.btnFollow.setText("Đã theo dõi");
            holder.btnFollow.setBackgroundResource(R.drawable.bg_follow_button_selected);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {

        ImageView imgCategory;
        TextView tvCategoryTitle, tvCategoryDescription, btnFollow;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            imgCategory = itemView.findViewById(R.id.imgCategory);
            tvCategoryTitle = itemView.findViewById(R.id.tvCategoryTitle);
            tvCategoryDescription = itemView.findViewById(R.id.tvCategoryDescription);
            btnFollow = itemView.findViewById(R.id.btnFollow);
        }
    }
}
