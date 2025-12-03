package phivu.ueh.edu.vn.news_app.data.remote;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import phivu.ueh.edu.vn.news_app.data.local.article.ArticleDAO;

/**
 * SyncService: gọi từ Application hoặc từ BroadcastReceiver khi có CONNECTIVITY_CHANGE
 * Mục tiêu: khi có kết nối, đồng bộ từ Firebase -> SQLite
 */
public class SyncService {
    private Context ctx;
    private ArticleDAO articleDAO;
    private ArticleFirebaseDAO fbArticle;

    public SyncService(Context ctx) {
        this.ctx = ctx.getApplicationContext();
        this.articleDAO = new ArticleDAO(ctx);
        this.fbArticle = new ArticleFirebaseDAO();
    }

    // kiểm tra mạng
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }

    // sync nếu online
    public void syncIfOnline() {
        if (!isOnline()) return;
        fbArticle.fetchAll(new ArticleFirebaseDAO.ListListener() {
            @Override
            public void onLoaded(java.util.List<phivu.ueh.edu.vn.news_app.model.Article> list) {
                long now = System.currentTimeMillis();
                for (phivu.ueh.edu.vn.news_app.model.Article a : list) {
                    articleDAO.upsert(a, now);
                }
            }
            @Override public void onError(String err) { /* log */ }
        });
    }
}
