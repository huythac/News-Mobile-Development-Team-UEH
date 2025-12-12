package phivu.ueh.edu.vn.news_app.UI.profile;

// SettingsActivity.java cơ bản

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.UI.login.LoginActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings); // Layout Settings của bạn

        // Nút Back
        findViewById(R.id.btnBackSettings).setOnClickListener(v -> finish());

        // Nút Đăng xuất (id là btnRowLogout hoặc button logout tùy layout)
        findViewById(R.id.btnRowLogout).setOnClickListener(v -> performLogout());
    }

    private void performLogout() {
        // 1. Đăng xuất khỏi Firebase
        FirebaseAuth.getInstance().signOut();

        // 2. Đăng xuất khỏi Google Client (Để lần sau nó hỏi lại chọn tài khoản)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient googleClient = GoogleSignIn.getClient(this, gso);

        googleClient.signOut().addOnCompleteListener(this, task -> {
            // 3. Sau khi đăng xuất xong, quay về màn hình Login
            Toast.makeText(SettingsActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            // Xóa hết lịch sử activity để user không bấm Back quay lại được
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}