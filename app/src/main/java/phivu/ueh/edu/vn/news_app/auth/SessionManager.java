package phivu.ueh.edu.vn.news_app.auth;


public class SessionManager {

    private static String role;

    public static void setRole(String r) {
        role = r;
    }

    public static boolean isAdmin() {
        return "admin".equals(role);
    }

    public static void clear() {
        role = null;
    }
}
