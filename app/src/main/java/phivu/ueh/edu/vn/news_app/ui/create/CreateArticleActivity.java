package phivu.ueh.edu.vn.news_app.ui.create;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import phivu.ueh.edu.vn.news_app.R;

public class CreateArticleActivity extends AppCompatActivity {

    private EditText edtTitle, edtDesc, edtContent;
    private TextView tvTitleCount, tvDescCount, btnNext;
    private ImageView imgPreview, btnClose;
    private LinearLayout layoutUploadImage, layoutPlaceholder;

    // Biến lưu Link ảnh (Mặc định rỗng)
    private String selectedImageUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_article);

        initViews();

        // Đóng màn hình
        btnClose.setOnClickListener(v -> finish());

        // Bấm vào vùng ảnh -> Hiện Dialog nhập Link
        layoutUploadImage.setOnClickListener(v -> showUrlInputDialog());

        // Bộ đếm ký tự
        setupCharCounter(edtTitle, tvTitleCount, 120);
        setupCharCounter(edtDesc, tvDescCount, 240);

        // Nút Tiếp theo
        btnNext.setOnClickListener(v -> {
            if (validateInput()) {
                goToPreview();
            }
        });
    }

    // --- HÀM HIỆN DIALOG (ĐÃ CẬP NHẬT KIỂM TRA ĐUÔI FILE) ---
    private void showUrlInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_input_url, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        EditText edtUrl = view.findViewById(R.id.edtUrlInput);
        Button btnCancel = view.findViewById(R.id.btnCancelDialog);
        Button btnConfirm = view.findViewById(R.id.btnConfirmDialog);

        // Nếu đã có link cũ thì điền lại cho user sửa
        if (!selectedImageUrl.isEmpty()) {
            edtUrl.setText(selectedImageUrl);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // --- SỰ KIỆN XÁC NHẬN ---
        btnConfirm.setOnClickListener(v -> {
            String url = edtUrl.getText().toString().trim();

            // 1. Kiểm tra rỗng
            if (url.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đường dẫn!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Kiểm tra định dạng URL (http/https)
            if (!android.util.Patterns.WEB_URL.matcher(url).matches()) {
                Toast.makeText(this, "Đường dẫn không hợp lệ! (Phải có http:// hoặc https://)", Toast.LENGTH_LONG).show();
                return;
            }

            // 3. KIỂM TRA ĐUÔI FILE ẢNH (BẮT BUỘC - MỨC ĐỘ 2)
            if (!isImageFile(url)) {
                Toast.makeText(this, "Link phải là file ảnh (.jpg, .png, .jpeg, .webp)!", Toast.LENGTH_LONG).show();
                return; // CHẶN LƯU NẾU KHÔNG ĐÚNG ĐUÔI FILE
            }

            // --- NẾU TẤT CẢ ĐỀU HỢP LỆ ---
            selectedImageUrl = url;

            imgPreview.setVisibility(View.VISIBLE);
            layoutPlaceholder.setVisibility(View.GONE);

            // Load ảnh bằng Glide
            Glide.with(CreateArticleActivity.this)
                    .load(selectedImageUrl)
                    .placeholder(R.drawable.bg_upload_gray)
                    .error(android.R.drawable.stat_notify_error)
                    .into(imgPreview);

            dialog.dismiss();
        });

        dialog.show();
    }

    // Hàm kiểm tra đuôi file
    private boolean isImageFile(String url) {
        String cleanUrl = url.toLowerCase(java.util.Locale.getDefault());
        return cleanUrl.endsWith(".jpg") ||
                cleanUrl.endsWith(".jpeg") ||
                cleanUrl.endsWith(".png") ||
                cleanUrl.endsWith(".webp") ||
                cleanUrl.endsWith(".gif") ||
                cleanUrl.endsWith(".bmp");
    }

    private void goToPreview() {
        Intent intent = new Intent(this, CreatePreviewActivity.class);
        intent.putExtra("title", edtTitle.getText().toString().trim());
        intent.putExtra("desc", edtDesc.getText().toString().trim());
        intent.putExtra("content", edtContent.getText().toString().trim());

        // Gửi String URL sang màn hình Preview
        intent.putExtra("imageUrl", selectedImageUrl);

        startActivity(intent);
    }

    private void initViews() {
        btnClose = findViewById(R.id.btnClose);
        btnNext = findViewById(R.id.btnNext);
        edtTitle = findViewById(R.id.edtTitle);
        tvTitleCount = findViewById(R.id.tvTitleCount);
        edtDesc = findViewById(R.id.edtDesc);
        tvDescCount = findViewById(R.id.tvDescCount);
        layoutUploadImage = findViewById(R.id.layoutUploadImage);
        imgPreview = findViewById(R.id.imgPreview);
        layoutPlaceholder = findViewById(R.id.layoutPlaceholder);
        edtContent = findViewById(R.id.edtContent);
    }

    private boolean validateInput() {
        if (edtTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show();
            return false;
        }
        // Kiểm tra biến String URL
        if (selectedImageUrl.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập link ảnh!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (edtDesc.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mô tả", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (edtContent.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void setupCharCounter(EditText editText, TextView textView, int max) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                textView.setText(s.length() + "/" + max + " ký tự");
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
}