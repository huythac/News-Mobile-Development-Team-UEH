package phivu.ueh.edu.vn.news_app;

public class Article {
    private String title;
    private String description;

    public Article(String title, String description) {
        this.title = title;
        this.description = description;
    }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}