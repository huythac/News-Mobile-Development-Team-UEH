package phivu.ueh.edu.vn.news_app.article;

public class Article {
    private int id;
    private String title;
    private String description;
    private String imageUrl;
    private String content;
    private int mainCategoryId;
    private int authorId;
    private String status;
    private String createdAt;

    public Article(int id, String title, String description, String imageUrl, String content,
                   int mainCategoryId, int authorId, String status, String createdAt) {
        this.id = id; this.title = title; this.description = description; this.imageUrl = imageUrl;
        this.content = content; this.mainCategoryId = mainCategoryId; this.authorId = authorId;
        this.status = status; this.createdAt = createdAt;
    }

    // getters
    public int getId(){ return id; }
    public String getTitle(){ return title; }
    public String getDescription(){ return description; }
    public String getImageUrl(){ return imageUrl; }
    public String getContent(){ return content; }
    public int getMainCategoryId(){ return mainCategoryId; }
    public int getAuthorId(){ return authorId; }
    public String getStatus(){ return status; }
    public String getCreatedAt(){ return createdAt; }
}
