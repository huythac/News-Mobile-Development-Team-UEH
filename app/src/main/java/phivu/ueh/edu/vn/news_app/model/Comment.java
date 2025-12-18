package phivu.ueh.edu.vn.news_app.model;

import java.io.Serializable;

public class Comment implements Serializable {

    private String id;              // Firebase document ID
    private String articleId;       // ID của bài viết
    private String userId;          // ID của user comment
    private String userName;        // Tên user
    private String userAvatar;     // URL avatar user
    private String content;         // Nội dung comment
    private long createdAt;        // Timestamp (milliseconds)

    // 🔹 BẮT BUỘC: constructor rỗng cho Firebase
    public Comment() {
    }

    // Constructor đầy đủ
    public Comment(String id, String articleId, String userId, String userName,
                   String userAvatar, String content, long createdAt) {
        this.id = id;
        this.articleId = articleId;
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
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

