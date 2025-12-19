package phivu.ueh.edu.vn.news_app.model;

import java.io.Serializable;

public class User implements Serializable {

    private String id;
    private String fullName;
    private String email;
    private String role;
    private String bio;
    private String avatar; // Đảm bảo có trường này

    public User() {}

    // 🔹 Constructor 1: Dùng cho Google Login (5 tham số)
    public User(String id, String fullName, String email, String role, String avatar) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.bio = ""; // Mặc định bio rỗng
        this.avatar = (avatar != null) ? avatar : "";
    }

    // 🔹 Constructor 2: Đầy đủ (Dùng khi load từ Firebase)
    public User(String id, String fullName, String email, String role, String bio, String avatar) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.bio = bio;
        this.avatar = avatar;
    }

    // Getter + Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}