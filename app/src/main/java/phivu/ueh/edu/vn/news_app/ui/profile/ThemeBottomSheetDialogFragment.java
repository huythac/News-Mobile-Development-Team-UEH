package phivu.ueh.edu.vn.news_app.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.utils.ThemeHelper;

/**
 * BottomSheetDialogFragment để chọn theme (Light/Dark mode)
 */
public class ThemeBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface ThemeChangeListener {
        void onThemeChanged(String newThemeMode);
    }

    private ThemeChangeListener listener;
    private String currentThemeMode;
    private String selectedThemeMode;

    private ImageView radioLight;
    private ImageView radioDark;
    private MaterialButton btnSaveTheme;

    public static ThemeBottomSheetDialogFragment newInstance(String currentThemeMode, ThemeChangeListener listener) {
        ThemeBottomSheetDialogFragment fragment = new ThemeBottomSheetDialogFragment();
        fragment.currentThemeMode = currentThemeMode;
        fragment.selectedThemeMode = currentThemeMode;
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                               @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_theme, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        updateRadioButtons();
        updateSaveButtonState();
    }

    private void initViews(View view) {
        radioLight = view.findViewById(R.id.radioLight);
        radioDark = view.findViewById(R.id.radioDark);
        btnSaveTheme = view.findViewById(R.id.btnSaveTheme);

        View optionLight = view.findViewById(R.id.optionLight);
        View optionDark = view.findViewById(R.id.optionDark);
        ImageView btnClose = view.findViewById(R.id.btnCloseTheme);

        optionLight.setOnClickListener(v -> selectTheme(ThemeHelper.THEME_LIGHT));
        optionDark.setOnClickListener(v -> selectTheme(ThemeHelper.THEME_DARK));
        btnClose.setOnClickListener(v -> dismiss());
        btnSaveTheme.setOnClickListener(v -> saveTheme());
    }

    private void selectTheme(String themeMode) {
        selectedThemeMode = themeMode;
        updateRadioButtons();
        updateSaveButtonState();
    }

    private void updateRadioButtons() {
        if (radioLight == null || radioDark == null) return;

        if (ThemeHelper.THEME_LIGHT.equals(selectedThemeMode)) {
            radioLight.setVisibility(View.VISIBLE);
            radioDark.setVisibility(View.GONE);
        } else {
            radioLight.setVisibility(View.GONE);
            radioDark.setVisibility(View.VISIBLE);
        }
    }

    private void updateSaveButtonState() {
        if (btnSaveTheme == null) return;
        boolean hasChanged = !selectedThemeMode.equals(currentThemeMode);
        btnSaveTheme.setEnabled(hasChanged);
    }

    private void saveTheme() {
        if (!selectedThemeMode.equals(currentThemeMode)) {
            // 1. Lưu vào SharedPreferences
            ThemeHelper.saveThemeMode(requireContext(), selectedThemeMode);
            
            // 2. Áp dụng theme mode mới
            ThemeHelper.applyThemeMode(selectedThemeMode);
            
            // 3. Thông báo cho UI
            if (listener != null) {
                listener.onThemeChanged(selectedThemeMode);
            }
            
            // 4. Đóng sheet
            dismiss();
            
            // 5. Recreate activity để apply theme ngay lập tức
            if (getActivity() != null) {
                getActivity().recreate();
            }
        }
    }
}

