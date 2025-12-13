package phivu.ueh.edu.vn.news_app.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.data.repository.CategoryRepository; // Dùng Repo của bạn
import phivu.ueh.edu.vn.news_app.model.Category; // Dùng Model của bạn
import phivu.ueh.edu.vn.news_app.utils.SearchHistoryManager;


public class SearchInputActivity extends AppCompatActivity {

    private EditText edtSearch;
    private ImageView btnClear;
    private LinearLayout containerTopics;
    private LinearLayout containerHistory;

    private CategoryRepository categoryRepo;
    private SearchHistoryManager historyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_input);

        // 1. Ánh xạ
        edtSearch = findViewById(R.id.edtSearchInput);
        ImageView btnBack = findViewById(R.id.btnBack);
        btnClear = findViewById(R.id.btnClear); // Nút X (Clear)

        containerTopics = findViewById(R.id.containerTopics); // Layout chứa các nút chủ đề
        containerHistory = findViewById(R.id.containerHistory); // Layout chứa lịch sử

        // 2. Khởi tạo
        categoryRepo = new CategoryRepository(this);
        historyManager = new SearchHistoryManager(this);

        // 3. Tự động focus
        edtSearch.requestFocus();

        // 4. Sự kiện nút Back
        btnBack.setOnClickListener(v -> finish());

        // 5. Sự kiện nút X (Clear)
        btnClear.setOnClickListener(v -> {
            edtSearch.setText(""); // Xóa trắng ô nhập
            edtSearch.requestFocus();
        });

        // 6. Ẩn/Hiện nút X khi nhập liệu
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) btnClear.setVisibility(View.VISIBLE);
                else btnClear.setVisibility(View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // 7. Sự kiện bấm nút Search trên bàn phím
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(edtSearch.getText().toString().trim());
                return true;
            }
            return false;
        });

        // 8. Tải dữ liệu
        loadCategories(); // <-- Dùng repo của bạn
        loadHistory();
    }

    // --- HÀM LOAD CATEGORY TỪ FIREBASE ---
    private void loadCategories() {
        // Gọi hàm getCategories có sẵn trong Repo của bạn
        categoryRepo.getCategories(new CategoryRepository.Callback() {
            @Override
            public void onSuccess(List<Category> list) {
                containerTopics.removeAllViews(); // Xóa dữ liệu mẫu cũ

                for (Category cat : list) {
                    // Tạo nút bấm (TextView) động
                    TextView chip = new TextView(SearchInputActivity.this);
                    chip.setText(cat.getName());
                    chip.setTextColor(getResources().getColor(R.color.black));
                    chip.setTextSize(14);
                    chip.setBackgroundResource(R.drawable.bg_chip_block); // Đảm bảo bạn đã có file này
                    chip.setPadding(40, 20, 40, 20); // Padding cho nút

                    // Margin giữa các nút
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 0, 24, 0);
                    chip.setLayoutParams(params);

                    // Bấm vào nút -> Điền vào ô tìm kiếm và tìm luôn
                    chip.setOnClickListener(v -> {
                        edtSearch.setText(cat.getName());
                        performSearch(cat.getName());
                    });

                    containerTopics.addView(chip);
                }
            }

            @Override
            public void onError(String err) {
                // Có thể log lỗi hoặc bỏ qua
            }
        });
    }

    // --- HÀM LOAD LỊCH SỬ TỪ MÁY ---
    private void loadHistory() {
        List<String> history = historyManager.getHistory();
        containerHistory.removeAllViews();

        for (String key : history) {
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setPadding(0, 30, 0, 30);
            item.setGravity(Gravity.CENTER_VERTICAL);

            // Icon đồng hồ
            ImageView icon = new ImageView(this);
            icon.setImageResource(android.R.drawable.ic_menu_recent_history);
            icon.setColorFilter(0xFF9E9E9E); // Màu xám
            icon.setLayoutParams(new LinearLayout.LayoutParams(50, 50));

            // Text từ khóa
            TextView tv = new TextView(this);
            tv.setText(key);
            tv.setTextSize(16);
            tv.setTextColor(0xFF333333);
            tv.setPadding(30, 0, 0, 0);

            item.addView(icon);
            item.addView(tv);

            // Bấm vào lịch sử -> Tìm lại
            item.setOnClickListener(v -> {
                edtSearch.setText(key);
                performSearch(key);
            });

            containerHistory.addView(item);
        }
    }

    private void performSearch(String query) {
        if (query.isEmpty()) return;
        historyManager.addHistory(query); // Lưu lịch sử

        Intent intent = new Intent(SearchInputActivity.this, SearchResultActivity.class);
        intent.putExtra("SEARCH_QUERY", query);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory(); // Load lại lịch sử khi quay lại
    }
}