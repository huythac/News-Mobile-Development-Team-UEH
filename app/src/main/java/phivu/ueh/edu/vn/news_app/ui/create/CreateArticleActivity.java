package phivu.ueh.edu.vn.news_app.ui.create;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import phivu.ueh.edu.vn.news_app.R;

public class CreateArticleActivity extends AppCompatActivity {

    private EditText edtTitle, edtDesc, edtContent;
    private TextView tvTitleCount, tvDescCount, btnNext;
    private ImageView imgPreview, btnClose;
    private LinearLayout layoutUploadImage, layoutPlaceholder;

    private Uri selectedImageUri = null;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_article); // Layout cũ của bạn

        initViews();

        // Sự kiện Đóng -> Finish activity này
        btnClose.setOnClickListener(v -> finish());

        // Chọn ảnh
        layoutUploadImage.setOnClickListener(v -> openFileChooser());

        // Đếm ký tự (Logic UI)
        setupCharCounter(edtTitle, tvTitleCount, 120);
        setupCharCounter(edtDesc, tvDescCount, 240);

        // BẤM NÚT TIẾP THEO -> CHUYỂN SANG PREVIEW
        btnNext.setOnClickListener(v -> {
            if (validateInput()) {
                goToPreview();
            }
        });
    }

    private void goToPreview() {
        Intent intent = new Intent(this, CreatePreviewActivity.class);

        // Gửi dữ liệu thô (Raw Data) sang màn hình kia
        intent.putExtra("title", edtTitle.getText().toString().trim());
        intent.putExtra("desc", edtDesc.getText().toString().trim());
        intent.putExtra("content", edtContent.getText().toString().trim());

        if (selectedImageUri != null) {
            intent.putExtra("imageUri", selectedImageUri.toString());
        }

        startActivity(intent);
        // KHÔNG GỌI finish() để khi user bấm Back ở màn hình kia, form này vẫn còn nguyên
    }

    // ... (Giữ nguyên các hàm initViews, openFileChooser, onActivityResult, setupCharCounter cũ) ...

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

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imgPreview.setImageURI(selectedImageUri);
            imgPreview.setVisibility(View.VISIBLE);
            layoutPlaceholder.setVisibility(View.GONE);
            imgPreview.setClipToOutline(true);
            imgPreview.setBackgroundResource(R.drawable.bg_upload_gray);
        }
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

    private boolean validateInput() {
        if (edtTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show();
            return false;
        }
        // Tạm bỏ qua check ảnh để test cho nhanh nếu muốn
        return true;
    }
}