package phivu.ueh.edu.vn.news_app.ui.base;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import phivu.ueh.edu.vn.news_app.utils.LocaleHelper;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        // Wrap context với locale đã lưu
        Context context = LocaleHelper.wrapContext(newBase);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đảm bảo locale được apply (double check)
        LocaleHelper.applySavedLocale(this);
    }
}

