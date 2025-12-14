package phivu.ueh.edu.vn.news_app.model;

import java.io.Serializable;

public class Article implements Serializable {

    private String id;           // Firebase key
    private String title;
    private String image;
    private String content;      // nội dung dài
    private String description;  // mô tả tóm tắt
    private String categoryId;

    private long publishDate;    // 🔹 THÊM MỚI: timestamp ngày đăng
    private String authorId;
    private String authorName;

    // 🔹 BẮT BUỘC: constructor rỗng cho Firebase
    public Article() { }

    // 🔹 Constructor cũ (giữ lại để không lỗi code đang dùng)
    public Article(String id, String title, String image,
                   String content, String description, String authorId, String authorName) {
        this.id = id;
        this.title = title;
        this.image = image;
        this.content = content;
        this.description = description;

    }

    // 🔹 Constructor đầy đủ (dùng khi cần)
    public Article(String id, String title, String image,
                   String content, String description,
                   String categoryId, long publishDate,
                   String authorId, String authorName) {
        this.id = id;
        this.title = title;
        this.image = image;
        this.content = content;
        this.description = description;
        this.categoryId = categoryId;
        this.publishDate = publishDate;
        this.authorId = authorId;
        this.authorName = authorName;

    }

    // ===== GETTERS / SETTERS =====

    public String getId() {
        return id;
    }

    public void setId(String id) {   // REQUIRED by Firebase
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    // 🔹 GET / SET publishDate
    public long getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(long publishDate) {
        this.publishDate = publishDate;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String id) {   // REQUIRED by Firebase
        this.authorId= authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String name) {
        this.authorName = authorName;
    }

}
