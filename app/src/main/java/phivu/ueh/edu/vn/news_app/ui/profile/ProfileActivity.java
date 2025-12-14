package phivu.ueh.edu.vn.news_app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.data.remote.UserFirebaseDAO;
import phivu.ueh.edu.vn.news_app.model.User;
import phivu.ueh.edu.vn.news_app.ui.main.HomeActivity;
import phivu.ueh.edu.vn.news_app.ui.saved.SavedActivity;
import phivu.ueh.edu.vn.news_app.ui.search.SearchActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvBio, tvWriteBio;
    private LinearLayout layoutEmpty;

    private UserFirebaseDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userDAO = new UserFirebaseDAO();

        tvName = findViewById(R.id.tvProfileName);
        tvBio = findViewById(R.id.tvUserBio);
        tvWriteBio = findViewById(R.id.tvWriteBio);
        layoutEmpty = findViewById(R.id.layoutEmptyState);

        ImageView btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        View.OnClickListener goToEditScreen = v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        };

        findViewById(R.id.btnEditProfile).setOnClickListener(goToEditScreen);
        findViewById(R.id.fabEditBio).setOnClickListener(goToEditScreen);
        tvWriteBio.setOnClickListener(goToEditScreen);

        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    // =========================
    // LOAD USER FROM FIRESTORE
    // =========================
    private void loadUserData() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        userDAO.getUser(uid, new UserFirebaseDAO.SingleListener() {
            @Override
            public void onLoaded(User user) {

                if (user.getFullName() != null) {
                    tvName.setText(user.getFullName());
                }

                if (user.getBio() != null && !user.getBio().isEmpty()) {
                    // Có bio
                    tvBio.setText(user.getBio());
                    tvBio.setVisibility(View.VISIBLE);
                    layoutEmpty.setVisibility(View.GONE);
                } else {
                    // Chưa có bio
                    tvBio.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String err) {
                Toast.makeText(
                        ProfileActivity.this,
                        "Không tải được dữ liệu profile",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    // =========================
    // BOTTOM NAVIGATION
    // =========================
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_account);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_account) {
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_search) {
                startActivity(new Intent(this, SearchActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_saved) {
                startActivity(new Intent(this, SavedActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }
}
