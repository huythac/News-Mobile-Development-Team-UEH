package phivu.ueh.edu.vn.news_app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.ui.main.SplashActivity;
import phivu.ueh.edu.vn.news_app.utils.LocaleHelper;

/**
 * Sử dụng BottomSheetDialogFragment để hiển thị ở dưới cùng màn hình
 */
public class LanguageBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface LanguageChangeListener {
        void onLanguageChanged(String newLocaleCode);
    }

    private LanguageChangeListener listener;
    private String currentLocale;
    private String selectedLocale;

    private ImageView radioVietnamese;
    private ImageView radioEnglish;
    private MaterialButton btnSaveLanguage;

    public static LanguageBottomSheetDialogFragment newInstance(String currentLocale, LanguageChangeListener listener) {
        LanguageBottomSheetDialogFragment fragment = new LanguageBottomSheetDialogFragment();
        fragment.currentLocale = currentLocale;
        fragment.selectedLocale = currentLocale;
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sử dụng style mặc định của BottomSheet hoặc custom nếu cần
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_language, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        updateRadioButtons();
        updateSaveButtonState();
    }

    private void initViews(View view) {
        radioVietnamese = view.findViewById(R.id.radioVietnamese);
        radioEnglish = view.findViewById(R.id.radioEnglish);
        btnSaveLanguage = view.findViewById(R.id.btnSaveLanguage);

        View optionVietnamese = view.findViewById(R.id.optionVietnamese);
        View optionEnglish = view.findViewById(R.id.optionEnglish);
        ImageView btnClose = view.findViewById(R.id.btnCloseLanguage);

        optionVietnamese.setOnClickListener(v -> selectLanguage(LocaleHelper.LOCALE_VI));
        optionEnglish.setOnClickListener(v -> selectLanguage(LocaleHelper.LOCALE_EN));
        btnClose.setOnClickListener(v -> dismiss());
        btnSaveLanguage.setOnClickListener(v -> saveLanguage());
    }

    private void selectLanguage(String localeCode) {
        selectedLocale = localeCode;
        updateRadioButtons();
        updateSaveButtonState();
    }

    private void updateRadioButtons() {
        if (radioVietnamese == null || radioEnglish == null) return;

        if (selectedLocale.equals(LocaleHelper.LOCALE_VI)) {
            radioVietnamese.setVisibility(View.VISIBLE);
            radioEnglish.setVisibility(View.GONE);
        } else {
            radioVietnamese.setVisibility(View.GONE);
            radioEnglish.setVisibility(View.VISIBLE);
        }
    }

    private void updateSaveButtonState() {
        if (btnSaveLanguage == null) return;
        boolean hasChanged = !selectedLocale.equals(currentLocale);
        btnSaveLanguage.setEnabled(hasChanged);
    }

    private void saveLanguage() {
        if (!selectedLocale.equals(currentLocale)) {
            // 1. Lưu vào SharedPreferences (commit để đảm bảo lưu ngay)
            LocaleHelper.saveLocale(requireContext(), selectedLocale);
            
            // 2. Áp dụng locale mới qua AppCompatDelegate
            LocaleHelper.applyLocale(selectedLocale);
            
            // 3. Thông báo cho UI
            if (listener != null) {
                listener.onLanguageChanged(selectedLocale);
            }
            
            // 4. Đóng sheet
            dismiss();
            
            // 5. Restart app hoàn toàn từ SplashActivity để áp dụng locale mới
            if (getActivity() != null) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (getActivity() != null) {
                        // Force restart app bằng cách clear task và start lại từ đầu
                        Intent intent = new Intent(getActivity(), SplashActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK 
                                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        getActivity().finishAffinity();
                        // Force kill process để đảm bảo restart hoàn toàn
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                }, 200);
            }
        }
    }
}
