package phivu.ueh.edu.vn.news_app.ui.main;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.data.repository.FollowCategoryRepository;
import phivu.ueh.edu.vn.news_app.model.Category;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private List<Category> categoryList;

    private final FollowCategoryRepository followRepo;
    private final String userId = "123";

    private HashMap<String, Boolean> followMap;

    // CONSTRUCTOR
    public CategoryAdapter(Context context, List<Category> categoryList, HashMap<String, Boolean> followMap) {
        this.context = context;
        this.categoryList = categoryList;
        this.followMap = (followMap != null) ? followMap : new HashMap<>();
        this.followRepo = new FollowCategoryRepository(context);
    }

    // HÀM QUAN TRỌNG - CẬP NHẬT FOLLOW MAP SAU KHI QUAY LẠI
    public void updateFollowMap(HashMap<String, Boolean> newMap) {
        this.followMap = newMap;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category c = categoryList.get(position);

        holder.tvCategoryTitle.setText(c.getName());
        holder.tvCategoryDescription.setText(c.getDescription());
        holder.imgCategory.setImageResource(R.drawable.ic_category_placeholder);

        // ===== ĐẾM SỐ BÀI VIẾT =====
        DatabaseReference articleRef =
                FirebaseDatabase.getInstance().getReference("articles");

        articleRef.orderByChild("categoryId")
                .equalTo(c.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long count = snapshot.getChildrenCount();
                        holder.tvCategoryDescription.setText(count + " bài viết");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        holder.tvCategoryDescription.setText("0 bài viết");
                    }
                });

        boolean isFollowed = followMap.containsKey(c.getId());
        updateFollowButton(holder, isFollowed);

        holder.btnFollow.setOnClickListener(v -> {
            boolean currentlyFollowed = followMap.containsKey(c.getId());

            if (currentlyFollowed) {
                followRepo.unfollow(userId, c.getId());
                followMap.remove(c.getId());
                updateFollowButton(holder, false);
            } else {
                followRepo.follow(userId, c.getId());
                followMap.put(c.getId(), true);
                updateFollowButton(holder, true);
            }

            notifyItemChanged(holder.getAdapterPosition());
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CategoryDetailActivity.class);
            intent.putExtra("categoryId", c.getId());
            intent.putExtra("categoryName", c.getName());
            intent.putExtra("categoryDesc", c.getDescription());
            context.startActivity(intent);
        });
    }


    private void updateFollowButton(CategoryViewHolder holder, boolean followed) {
        if (followed) {
            holder.btnFollow.setText("Đã theo dõi");
            holder.btnFollow.setBackgroundResource(R.drawable.bg_follow_button_selected);
        } else {
            holder.btnFollow.setText("Theo dõi");
            holder.btnFollow.setBackgroundResource(R.drawable.bg_follow_button);
        }
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
