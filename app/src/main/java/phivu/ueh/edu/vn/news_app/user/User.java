package phivu.ueh.edu.vn.news_app.user;

public class User {
    private int id;
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String role;

    public User(int id, String username, String email, String fullName, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
}
