package phivu.ueh.edu.vn.news_app.model;

import java.io.Serializable;

public class Comment implements Serializable {

    private String id;              // Firebase document ID
    private String articleId;       // ID của bài viết

    // 🔹 THAY ĐỔI LỚN: Dùng đối tượng User thay vì lưu lẻ tẻ
    private User user;

    private String content;         // Nội dung comment
    private long createdAt;         // Timestamp (milliseconds)

    public Comment() {
    }

    // Constructor cập nhật
    public Comment(String id, String articleId, User user, String content, long createdAt) {
        this.id = id;
        this.articleId = articleId;
        this.user = user;
        this.content = content;
        this.createdAt = createdAt;
    }

    // ===== GETTERS / SETTERS =====

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    // 🔹 Getter cho User object
    public User getUser() {
        return user;
    }

    // 🔹 Setter cho User object
    public void setUser(User user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}