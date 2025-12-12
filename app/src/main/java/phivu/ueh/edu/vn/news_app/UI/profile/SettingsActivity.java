package phivu.ueh.edu.vn.news_app.UI.profile;

// SettingsActivity.java cơ bản

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import phivu.ueh.edu.vn.news_app.R;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Xử lý nút Back để quay lại Profile
        findViewById(R.id.btnBackSettings).setOnClickListener(v -> finish());
    }
}
