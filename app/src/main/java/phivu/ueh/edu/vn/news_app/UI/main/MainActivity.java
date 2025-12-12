package phivu.ueh.edu.vn.news_app.UI.main;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.UI.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private GoogleSignInClient googleClient;
    private static final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleClient = GoogleSignIn.getClient(this, gso);

        // Nút Google
        findViewById(R.id.btnGoogle).setOnClickListener(v -> {
            Intent intent = googleClient.getSignInIntent();
            startActivityForResult(intent, RC_SIGN_IN);
        });
    }

    // Nhận kết quả đăng nhập từ Google
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    // Xử lý kết quả
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Đăng nhập thành công
            String email = account.getEmail();
            Toast.makeText(this, "Đăng nhập thành công: " + email, Toast.LENGTH_SHORT).show();

            // CHUYỂN SANG HomeActivity
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);

            finish(); // Không cho quay lại màn login

        } catch (ApiException e) {
            Toast.makeText(this, "Đăng nhập thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}



// NÚT GOOGLE → mở LoginActivity
//findViewById(R.id.btnGoogle).setOnClickListener(v -> {
//Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//startActivity(intent);
//        });