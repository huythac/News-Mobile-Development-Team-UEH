package phivu.ueh.edu.vn.news_app.model;

import java.io.Serializable;

public class Article implements Serializable {

    private String id;           // Firebase key (String!)
    private String title;
    private String image;
    private String content;      // nội dung dài của bài báo
    private String description;  // mô tả tóm tắt bài báo

    private String categoryId;

    public Article() { }

    public Article(String id, String title, String image, String content, String description) {
        this.id = id;
        this.title = title;
        this.image = image;
        this.content = content;
        this.description = description;
    }

    // GETTERS - SETTERS
    public String getId() {
        return id;
    }

    public void setId(String id) {   // REQUIRED by Firebase DAO
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


}
