package phivu.ueh.edu.vn.news_app.UI.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.data.remote.UserFirebaseDAO;
import phivu.ueh.edu.vn.news_app.model.User;

public class MainActivity extends AppCompatActivity {

    private GoogleSignInClient googleClient;
    private static final int RC_SIGN_IN = 100;
    private FirebaseAuth mAuth;
    private UserFirebaseDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        userDAO = new UserFirebaseDAO();

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
            return;
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.btnGoogle).setOnClickListener(v -> signIn());
    }

    private void signIn() {
        Intent intent = googleClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        if (firebaseUser != null) {
                            checkAndSaveUser(firebaseUser); // Gọi hàm kiểm tra riêng
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Lỗi xác thực Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // HÀM KIỂM TRA & LƯU USER VÀO DATABASE
    private void checkAndSaveUser(FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();

        // Kiểm tra xem User này đã có trong Database chưa
        userDAO.getUser(uid, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // TRƯỜNG HỢP 1: User CHƯA CÓ -> Tạo mới
                    String email = firebaseUser.getEmail();
                    String name = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "No Name";

                    User newUser = new User(uid, name, email, "user");
                    userDAO.createUser(newUser);

                    Toast.makeText(MainActivity.this, "Chào bạn mới!", Toast.LENGTH_SHORT).show();
                } else {
                    // TRƯỜNG HỢP 2: User ĐÃ CÓ
                    Toast.makeText(MainActivity.this, "Xin chào người anh em!", Toast.LENGTH_SHORT).show();
                }

                goToHome();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                goToHome();
            }
        });
    }

    private void goToHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}