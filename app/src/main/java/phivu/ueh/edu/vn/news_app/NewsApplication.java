package phivu.ueh.edu.vn.news_app;

import android.app.Application;
import android.content.Context;

import phivu.ueh.edu.vn.news_app.utils.LocaleHelper;
import phivu.ueh.edu.vn.news_app.utils.ThemeHelper;

public class NewsApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Áp dụng locale đã lưu ngay khi app khởi động
        LocaleHelper.applySavedLocale(this);
        // Áp dụng theme mode đã lưu ngay khi app khởi động
        ThemeHelper.applySavedThemeMode(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        // Wrap context với locale đã lưu
        Context context = LocaleHelper.wrapContext(base);
        super.attachBaseContext(context);
    }
}

