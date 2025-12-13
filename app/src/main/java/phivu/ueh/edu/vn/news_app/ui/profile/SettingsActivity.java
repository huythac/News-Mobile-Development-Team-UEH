package phivu.ueh.edu.vn.news_app.ui.profile;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.ui.main.SplashActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Nút Back
        findViewById(R.id.btnBackSettings).setOnClickListener(v -> finish());

        // Nút Đăng xuất -> Gọi hàm hiển thị Dialog
        findViewById(R.id.btnRowLogout).setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void showLogoutConfirmationDialog() {
        // 1. Khởi tạo Dialog
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_logout); // Gắn layout vừa tạo

        // Làm trong suốt nền mặc định của Dialog để bo góc đẹp hơn
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // 2. Ánh xạ nút trong Dialog
        Button btnConfirm = dialog.findViewById(R.id.btnConfirmLogout);
        Button btnCancel = dialog.findViewById(R.id.btnCancelLogout);

        // 3. Xử lý sự kiện
        // Nút "Quay lại" -> Đóng dialog
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Nút "Đăng xuất" -> Thực hiện logout thật
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            performLogout(); // Gọi hàm logout cũ
        });

        // 4. Hiển thị
        dialog.show();
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

            Intent intent = new Intent(SettingsActivity.this, SplashActivity.class);
            // Xóa hết lịch sử activity để user không bấm Back quay lại được
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}