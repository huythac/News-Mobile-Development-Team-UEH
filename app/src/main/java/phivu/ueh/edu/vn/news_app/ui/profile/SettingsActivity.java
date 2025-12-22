package phivu.ueh.edu.vn.news_app.ui.profile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.ui.base.BaseActivity;
import phivu.ueh.edu.vn.news_app.ui.main.SplashActivity;
import phivu.ueh.edu.vn.news_app.utils.LocaleHelper;
import phivu.ueh.edu.vn.news_app.utils.NotificationPreferences;
import phivu.ueh.edu.vn.news_app.utils.ThemeHelper;

public class SettingsActivity extends BaseActivity implements 
        LanguageBottomSheetDialogFragment.LanguageChangeListener,
        ThemeBottomSheetDialogFragment.ThemeChangeListener {

    private TextView tvLanguageValue;
    private TextView tvThemeValue;
    private String currentLocale;
    private SwitchMaterial swNoti;
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Nút Back
        findViewById(R.id.btnBackSettings).setOnClickListener(v -> finish());

        // Nút Ngôn ngữ -> Mở Bottom Sheet
        findViewById(R.id.btnRowLanguage).setOnClickListener(v -> showLanguageBottomSheet());

        // Nút Giao diện -> Mở Bottom Sheet
        findViewById(R.id.btnRowTheme).setOnClickListener(v -> showThemeBottomSheet());

        // Nút Đăng xuất -> Gọi hàm hiển thị Dialog
        findViewById(R.id.btnRowLogout).setOnClickListener(v -> showLogoutConfirmationDialog());

        // Initialize language value display
        tvLanguageValue = findViewById(R.id.tvLanguageValue);
        updateLanguageDisplay();
        
        // Initialize theme value display
        tvThemeValue = findViewById(R.id.tvThemeValue);
        updateThemeDisplay();
        
        // Initialize notification switch
        swNoti = findViewById(R.id.swNoti);
        setupNotificationSwitch();
    }
    
    private void setupNotificationSwitch() {
        // Load saved state
        boolean isEnabled = NotificationPreferences.isNotificationsEnabled(this);
        swNoti.setChecked(isEnabled);
        
        // Set listener
        swNoti.setOnCheckedChangeListener((buttonView, isChecked) -> {
            handleNotificationToggle(isChecked);
        });
    }
    
    private void handleNotificationToggle(boolean isChecked) {
        if (isChecked) {
            // User wants to enable notifications
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ requires POST_NOTIFICATIONS permission
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Request permission
                    showPermissionDialog();
                    // Revert switch to OFF until permission is granted
                    swNoti.setChecked(false);
                    return;
                }
            }
            // Permission granted or API < 33, enable notifications
            NotificationPreferences.setNotificationsEnabled(this, true);
            // Optional: Cancel existing notifications if any
            cancelAllNotifications();
        } else {
            // User wants to disable notifications
            NotificationPreferences.setNotificationsEnabled(this, false);
            // Cancel all existing notifications
            cancelAllNotifications();
            // Show snackbar
            showNotificationDisabledSnackbar();
        }
    }
    
    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.notification_permission_required)
                .setMessage(R.string.notification_permission_message)
                .setPositiveButton(R.string.notification_open_settings, (dialog, which) -> {
                    // Open app settings
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.notification_cancel, (dialog, which) -> {
                    dialog.dismiss();
                    // Show snackbar that permission was denied
                    showNotificationPermissionDeniedSnackbar();
                })
                .setCancelable(false)
                .show();
    }
    
    private void cancelAllNotifications() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancelAll();
    }
    
    private void showNotificationDisabledSnackbar() {
        View rootView = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(rootView, "", Snackbar.LENGTH_LONG);
        
        // Inflate custom layout
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.setPadding(0, 0, 0, 0);
        snackbarLayout.setBackground(null);
        
        LayoutInflater inflater = LayoutInflater.from(this);
        View customView = inflater.inflate(R.layout.custom_snackbar_notification, null);
        
        TextView snackbarText = customView.findViewById(R.id.snackbarText);
        snackbarText.setText(R.string.notification_disabled);
        
        ImageView btnClose = customView.findViewById(R.id.btnCloseSnackbar);
        btnClose.setOnClickListener(v -> snackbar.dismiss());
        
        snackbarLayout.addView(customView, 0);
        snackbar.show();
    }
    
    private void showNotificationPermissionDeniedSnackbar() {
        View rootView = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(rootView, "", Snackbar.LENGTH_LONG);
        
        // Inflate custom layout
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.setPadding(0, 0, 0, 0);
        snackbarLayout.setBackground(null);
        
        LayoutInflater inflater = LayoutInflater.from(this);
        View customView = inflater.inflate(R.layout.custom_snackbar_notification, null);
        
        TextView snackbarText = customView.findViewById(R.id.snackbarText);
        snackbarText.setText(R.string.notification_permission_denied);
        
        ImageView btnClose = customView.findViewById(R.id.btnCloseSnackbar);
        btnClose.setOnClickListener(v -> snackbar.dismiss());
        
        snackbarLayout.addView(customView, 0);
        snackbar.show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Check if permission was granted when returning from settings
        if (swNoti != null && !swNoti.isChecked()) {
            // User was trying to enable, check if permission is now granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, enable notifications
                    swNoti.setChecked(true);
                    NotificationPreferences.setNotificationsEnabled(this, true);
                }
            } else {
                // API < 33, no permission needed
                swNoti.setChecked(true);
                NotificationPreferences.setNotificationsEnabled(this, true);
            }
        }
    }

    private void updateLanguageDisplay() {
        currentLocale = LocaleHelper.getSavedLocale(this);
        String displayText = LocaleHelper.getLocaleDisplayName(this, currentLocale);
        if (tvLanguageValue != null) {
            tvLanguageValue.setText(displayText);
        }
    }

    private void showLanguageBottomSheet() {
        currentLocale = LocaleHelper.getSavedLocale(this);
        LanguageBottomSheetDialogFragment bottomSheet = 
            LanguageBottomSheetDialogFragment.newInstance(currentLocale, this);
        FragmentManager fragmentManager = getSupportFragmentManager();
        bottomSheet.show(fragmentManager, "LanguageBottomSheet");
    }

    @Override
    public void onLanguageChanged(String newLocaleCode) {
        // Update display
        updateLanguageDisplay();
        // Activity sẽ được recreate bởi bottom sheet fragment, nên không cần recreate ở đây
    }

    private void updateThemeDisplay() {
        String currentThemeMode = ThemeHelper.getSavedThemeMode(this);
        String displayText = ThemeHelper.getThemeModeDisplayName(this, currentThemeMode);
        if (tvThemeValue != null) {
            tvThemeValue.setText(displayText);
        }
    }

    private void showThemeBottomSheet() {
        String currentThemeMode = ThemeHelper.getSavedThemeMode(this);
        ThemeBottomSheetDialogFragment bottomSheet = 
            ThemeBottomSheetDialogFragment.newInstance(currentThemeMode, this);
        FragmentManager fragmentManager = getSupportFragmentManager();
        bottomSheet.show(fragmentManager, "ThemeBottomSheet");
    }

    @Override
    public void onThemeChanged(String newThemeMode) {
        // Update display
        updateThemeDisplay();
        // Activity sẽ được recreate bởi bottom sheet fragment, nên không cần recreate ở đây
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