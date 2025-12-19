package phivu.ueh.edu.vn.news_app.ui.create;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import phivu.ueh.edu.vn.news_app.R;

public class CreatePreviewActivity extends AppCompatActivity {

    private ImageView btnBack, imgPreviewThumb;
    private TextView tvPreviewTitle, tvPreviewDesc, tvPreviewDate;
    private ChipGroup chipGroupTopics;
    private Button btnPostArticle;

    private String selectedTopic = null; // Lưu chủ đề đang chọn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_preview);

        initViews();
        displayData();
        mockTopics(); // Tạo chủ đề giả

        // Sự kiện Back: Đóng màn hình này, quay lại màn hình nhập liệu
        btnBack.setOnClickListener(v -> finish());

        // Sự kiện Đăng bài: Chỉ check logic, không gọi DB
        btnPostArticle.setOnClickListener(v -> handlePostAction());
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackPreview);
        imgPreviewThumb = findViewById(R.id.imgPreviewThumb);
        tvPreviewTitle = findViewById(R.id.tvPreviewTitle);
        tvPreviewDesc = findViewById(R.id.tvPreviewDesc);
        tvPreviewDate = findViewById(R.id.tvPreviewDate);
        chipGroupTopics = findViewById(R.id.chipGroupTopics);
        btnPostArticle = findViewById(R.id.btnPostArticle);
    }

    private void displayData() {
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String desc = intent.getStringExtra("desc");
        String uriStr = intent.getStringExtra("imageUri");

        // Set Text
        tvPreviewTitle.setText(title);
        tvPreviewDesc.setText(desc);

        // Set Ngày hiện tại
        String date = new SimpleDateFormat("EEEE, dd MMMM, yyyy", new Locale("vi", "VN")).format(new Date());
        tvPreviewDate.setText(date);

        // Set Ảnh
        if (uriStr != null) {
            imgPreviewThumb.setImageURI(Uri.parse(uriStr));
            imgPreviewThumb.setClipToOutline(true); // Bo góc ảnh
        }
    }

    // Tạo danh sách chủ đề giả để test UI
    private void mockTopics() {
        List<String> dummyTopics = Arrays.asList(
                "Chủ đề 1", "Chủ đề 2", "Chủ đề 3",
                "Chủ đề 4", "Chủ đề 5", "Chủ đề 6",
                "Chủ đề 7", "Chủ đề 8"
        );

        for (String topicName : dummyTopics) {
            Chip chip = new Chip(this);
            chip.setText(topicName);
            chip.setCheckable(true);
            chip.setClickable(true);

            // Style mặc định: Nền trắng, Viền xám
            chip.setChipBackgroundColorResource(android.R.color.white);
            chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
            chip.setChipStrokeWidth(3f); // Viền dày tí cho dễ nhìn
            chip.setTextColor(Color.BLACK);

            // Sự kiện chọn Chip
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedTopic = topicName;

                    // Đổi màu style khi chọn: Đen
                    chip.setChipBackgroundColorResource(android.R.color.black);
                    chip.setTextColor(Color.WHITE);
                    chip.setChipStrokeWidth(0f);

                    // Active nút Đăng bài
                    enablePostButton(true);
                } else {
                    // Reset style
                    chip.setChipBackgroundColorResource(android.R.color.white);
                    chip.setTextColor(Color.BLACK);
                    chip.setChipStrokeWidth(3f);

                    // Nếu bỏ chọn hết -> Disable nút
                    if (chipGroupTopics.getCheckedChipId() == View.NO_ID) {
                        selectedTopic = null;
                        enablePostButton(false);
                    }
                }
            });

            chipGroupTopics.addView(chip);
        }
    }

    private void enablePostButton(boolean enable) {
        btnPostArticle.setEnabled(enable);
        if (enable) {
            btnPostArticle.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK)); // Nút đen
            btnPostArticle.setTextColor(Color.WHITE);
        } else {
            btnPostArticle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F5F5F5"))); // Nút xám
            btnPostArticle.setTextColor(Color.parseColor("#999999"));
        }
    }

    private void handlePostAction() {
        // LOGIC TEST GIẢ LẬP

        // 1. Kiểm tra điều kiện (Ví dụ: phải chọn chủ đề)
        if (selectedTopic == null) {
            Toast.makeText(this, "Test: Lỗi - Vui lòng chọn chủ đề!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Giả lập kết nối DB (Random thành công hoặc lỗi để test)
        // Ở đây mình cho thành công 100% để bạn test luồng
        boolean mockDbConnection = true;

        if (mockDbConnection) {
            String msg = "Test: Đăng bài thành công!\n" +
                    "Tiêu đề: " + tvPreviewTitle.getText() + "\n" +
                    "Chủ đề: " + selectedTopic;

            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

            // Đăng xong thì làm gì? -> Quay về trang chủ
            // Intent intent = new Intent(this, HomeActivity.class);
            // startActivity(intent);
        } else {
            Toast.makeText(this, "Test: Lỗi kết nối Database (Giả lập)", Toast.LENGTH_SHORT).show();
        }
    }
}