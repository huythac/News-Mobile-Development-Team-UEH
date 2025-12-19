package phivu.ueh.edu.vn.news_app.ui.create;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.data.repository.CategoryRepository;
import phivu.ueh.edu.vn.news_app.model.Article;
import phivu.ueh.edu.vn.news_app.model.Category;
import phivu.ueh.edu.vn.news_app.ui.main.HomeActivity;

public class CreatePreviewActivity extends AppCompatActivity {

    private ImageView btnBack, imgPreviewThumb;
    private TextView tvPreviewTitle, tvPreviewDesc, tvPreviewDate, tvAuthorName;
    private ChipGroup chipGroupTopics;
    private Button btnPostArticle;

    // Data nhận về
    private String title, desc, content, imageUrl;

    private String selectedCategoryId = null;
    private CategoryRepository categoryRepo;
    private FirebaseFirestore db;

    // --- KHAI BÁO MÀU CỨNG ---
    private int colorEnabled;  // Xanh lá
    private int colorDisabled; // Xám (gốc XML)
    private int colorTextEnabled = Color.WHITE;
    private int colorTextDisabled = Color.parseColor("#999999"); // Màu chữ xám XML

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_preview);

        // Khởi tạo màu sắc
        colorEnabled = Color.parseColor("#8BC34A");
        colorDisabled = Color.parseColor("#F5F5F5"); // Màu xám gốc trong XML của bạn

        db = FirebaseFirestore.getInstance();
        categoryRepo = new CategoryRepository(this);

        initViews();
        getDataFromIntent();
        setupUI();
        loadTopics();

        btnBack.setOnClickListener(v -> finish());

        btnPostArticle.setOnClickListener(v -> {
            if (selectedCategoryId == null) {
                Toast.makeText(this, "Vui lòng chọn 1 chủ đề!", Toast.LENGTH_SHORT).show();
            } else {
                saveToFirestore();
            }
        });
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackPreview);
        imgPreviewThumb = findViewById(R.id.imgPreviewThumb);
        tvPreviewTitle = findViewById(R.id.tvPreviewTitle);
        tvPreviewDesc = findViewById(R.id.tvPreviewDesc);
        tvPreviewDate = findViewById(R.id.tvPreviewDate);
        tvAuthorName = findViewById(R.id.tvAuthorNamePreview);
        chipGroupTopics = findViewById(R.id.chipGroupTopics);
        btnPostArticle = findViewById(R.id.btnPostArticle);
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        desc = intent.getStringExtra("desc");
        content = intent.getStringExtra("content");
        imageUrl = intent.getStringExtra("imageUrl");
    }

    private void setupUI() {
        tvPreviewTitle.setText(title);
        tvPreviewDesc.setText(desc);

        String dateDisplay = new SimpleDateFormat("EEEE, dd MMMM, yyyy", new Locale("vi", "VN")).format(new Date());
        tvPreviewDate.setText(dateDisplay);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = (user != null && user.getDisplayName() != null) ? user.getDisplayName() : "Tôi";
        tvAuthorName.setText(name);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(imgPreviewThumb);
            imgPreviewThumb.setClipToOutline(true);
        }
    }

    private void loadTopics() {
        categoryRepo.getCategories(new CategoryRepository.Callback() {
            @Override
            public void onSuccess(List<Category> list) {
                chipGroupTopics.removeAllViews();

                // 1. SET TRẠNG THÁI BAN ĐẦU (DISABLED)
                btnPostArticle.setEnabled(false);
                btnPostArticle.setBackgroundTintList(ColorStateList.valueOf(colorDisabled));
                btnPostArticle.setTextColor(colorTextDisabled);

                for (Category c : list) {
                    Chip chip = new Chip(CreatePreviewActivity.this);
                    chip.setText(c.getName());
                    chip.setCheckable(true);
                    chip.setClickable(true);

                    // Style mặc định của Chip
                    chip.setChipBackgroundColorResource(android.R.color.white);
                    chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
                    chip.setChipStrokeWidth(3f);
                    chip.setTextColor(Color.BLACK);

                    chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (isChecked) {
                            // --- KHI ĐƯỢC CHỌN ---
                            selectedCategoryId = c.getId();

                            // Chip Xanh
                            chip.setChipBackgroundColor(ColorStateList.valueOf(colorEnabled));
                            chip.setTextColor(Color.WHITE);
                            chip.setChipStrokeWidth(0f);

                            // Button Xanh (Active)
                            btnPostArticle.setEnabled(true);
                            btnPostArticle.setBackgroundTintList(ColorStateList.valueOf(colorEnabled));
                            btnPostArticle.setTextColor(colorTextEnabled);

                        } else {
                            // --- KHI BỎ CHỌN ---

                            // Chip về Trắng
                            chip.setChipBackgroundColorResource(android.R.color.white);
                            chip.setTextColor(Color.BLACK);
                            chip.setChipStrokeWidth(3f);

                            // Nếu không còn cái nào được chọn
                            if (chipGroupTopics.getCheckedChipId() == View.NO_ID) {
                                selectedCategoryId = null;

                                // Button về Xám (Disabled)
                                btnPostArticle.setEnabled(false);
                                btnPostArticle.setBackgroundTintList(ColorStateList.valueOf(colorDisabled));
                                btnPostArticle.setTextColor(colorTextDisabled);
                            }
                        }
                    });
                    chipGroupTopics.addView(chip);
                }
            }

            @Override
            public void onError(String err) {
                Toast.makeText(CreatePreviewActivity.this, "Lỗi tải chủ đề", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveToFirestore() {
        btnPostArticle.setText("Đang đăng bài...");
        btnPostArticle.setEnabled(false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String authorId = (user != null) ? user.getUid() : "anonymous";
        String authorName = (user != null && user.getDisplayName() != null) ? user.getDisplayName() : "Ẩn danh";
        long currentTimestamp = System.currentTimeMillis();

        DocumentReference newRef = db.collection("articles").document();
        String tempId = newRef.getId();

        Article newArticle = new Article(
                tempId,
                title,
                imageUrl,
                content,
                desc,
                selectedCategoryId,
                currentTimestamp,
                authorId,
                authorName
        );

        newRef.set(newArticle)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreatePreviewActivity.this, "Đăng bài thành công!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(CreatePreviewActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreatePreviewActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnPostArticle.setEnabled(true);
                    btnPostArticle.setText("Đăng bài");
                });
    }
}