package phivu.ueh.edu.vn.news_app.UI.login;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.UI.main.HomeActivity;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient googleClient;
    private final int RC_SIGN_IN = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.btnGoogle).setOnClickListener(v -> {
            Intent intent = googleClient.getSignInIntent();
            startActivityForResult(intent, RC_SIGN_IN);
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

                FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnSuccessListener(authResult -> {

                            // ĐĂNG NHẬP XONG → CHUYỂN SANG HOME
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish(); // Đóng LoginActivity để không quay lại được

                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Lỗi Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });

            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In thất bại: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
