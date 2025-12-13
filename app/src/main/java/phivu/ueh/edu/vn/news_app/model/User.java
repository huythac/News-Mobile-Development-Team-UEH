package phivu.ueh.edu.vn.news_app.model;

public class User {

    private String id;
    private String fullName;
    private String email;
    private String role;
    private String bio;
    // 🔥 BẮT BUỘC phải có constructor rỗng cho Firebase
    public User() {}

    // 🔥 Constructor đầy đủ dùng khi tạo user mới
    public User(String id, String fullName, String email, String role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.bio = "";
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
}
