package phivu.ueh.edu.vn.news_app.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.ui.login.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    // ⚠️ CHỈ BẬT 1 LẦN DUY NHẤT
    private static final boolean ENABLE_MIGRATION = false;

    // UID ADMIN (UID đang có dữ liệu realtime)
    private static final String ADMIN_UID = "OABcicgk7BdnTHN0oMWOChsIKnz1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        new Handler().postDelayed(() -> {
            startActivity(
                    new Intent(SplashActivity.this, LoginActivity.class)
            );
            finish();
        }, 2000);
    }
}
