package phivu.ueh.edu.vn.news_app.UI.main;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import phivu.ueh.edu.vn.news_app.R;

public class MainActivity extends AppCompatActivity {

    private LinearLayout layoutButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layoutButtons = findViewById(R.id.layoutButtons);

        // Khi nhấn nút Email → chuyển sang HomeActivity để test bài báo
        findViewById(R.id.btnEmail).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
        });
    }
}
