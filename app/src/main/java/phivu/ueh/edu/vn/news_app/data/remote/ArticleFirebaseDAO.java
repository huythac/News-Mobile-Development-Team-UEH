package phivu.ueh.edu.vn.news_app.data.remote;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import phivu.ueh.edu.vn.news_app.model.Article;

public class ArticleFirebaseDAO {

    private final FirebaseFirestore db;

    public ArticleFirebaseDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public interface ListListener {
        void onLoaded(List<Article> list);
        void onError(String err);
    }

    public interface SingleListener {
        void onLoaded(Article a);
        void onError(String err);
    }

    // =========================
    // INSERT
    // =========================
    public void insert(Article a) {
        db.collection("articles")
                .add(a)
                .addOnSuccessListener(docRef -> {
                    a.setId(docRef.getId());
                });
    }

    // =========================
    // FETCH ALL (new → old)
    // =========================
    public void fetchAll(final ListListener listener) {
        db.collection("articles")
                .orderBy("publishDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    List<Article> list = new ArrayList<>();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Article a = doc.toObject(Article.class);
                        if (a != null) {
                            a.setId(doc.getId());
                            list.add(a);
                        }
                    }

                    listener.onLoaded(list);
                })
                .addOnFailureListener(e ->
                        listener.onError(e.getMessage())
                );
    }

    // =========================
    // FETCH BY ID
    // =========================
    public void fetchById(String id, final SingleListener listener) {
        db.collection("articles")
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Article a = doc.toObject(Article.class);
                        if (a != null) {
                            a.setId(doc.getId());
                            listener.onLoaded(a);
                        } else {
                            listener.onError("Parse error");
                        }
                    } else {
                        listener.onError("Not found");
                    }
                })
                .addOnFailureListener(e ->
                        listener.onError(e.getMessage())
                );
    }

    // =========================
    // FETCH BY CATEGORY
    // =========================
    public void fetchByCategory(String categoryId, final ListListener listener) {
        db.collection("articles")
                .whereEqualTo("categoryId", categoryId)
                .orderBy("publishDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    List<Article> list = new ArrayList<>();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Article a = doc.toObject(Article.class);
                        if (a != null) {
                            a.setId(doc.getId());
                            list.add(a);
                        }
                    }

                    listener.onLoaded(list);
                })
                .addOnFailureListener(e ->
                        listener.onError(e.getMessage())
                );
    }

    // =========================
    // FETCH BY AUTHOR
    // =========================
    /**
     * Fetch articles by authorId, excluding current article, limited to maxCount
     * @param authorId Author ID to filter by
     * @param excludeId Article ID to exclude (current article)
     * @param maxCount Maximum number of articles to return (default: 10)
     * @param listener Callback for results
     */
    public void fetchByAuthor(String authorId, String excludeId, int maxCount, final ListListener listener) {
        if (authorId == null || authorId.trim().isEmpty()) {
            if (listener != null) {
                listener.onError("Author ID is null or empty");
            }
            return;
        }

        // Try query with orderBy first (requires composite index)
        Query query = db.collection("articles")
                .whereEqualTo("authorId", authorId.trim())
                .orderBy("publishDate", Query.Direction.DESCENDING)
                .limit(maxCount > 0 ? maxCount + 1 : 11); // Get one extra to account for excluded article

        query.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Article> list = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        // Exclude current article
                        String docId = doc.getId();
                        if (excludeId != null && excludeId.equals(docId)) {
                            continue;
                        }

                        Article a = doc.toObject(Article.class);
                        if (a != null) {
                            a.setId(docId);
                            list.add(a);
                        }
                        
                        // Limit results after excluding current article
                        if (list.size() >= (maxCount > 0 ? maxCount : 10)) {
                            break;
                        }
                    }

                    if (listener != null) {
                        listener.onLoaded(list);
                    }
                })
                .addOnFailureListener(e -> {
                    // Fallback: Query without orderBy if index is missing
                    // Then sort in code
                    db.collection("articles")
                            .whereEqualTo("authorId", authorId.trim())
                            .limit(50) // Get more to ensure we have enough after filtering
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                List<Article> list = new ArrayList<>();

                                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                    // Exclude current article
                                    String docId = doc.getId();
                                    if (excludeId != null && excludeId.equals(docId)) {
                                        continue;
                                    }

                                    Article a = doc.toObject(Article.class);
                                    if (a != null) {
                                        a.setId(docId);
                                        list.add(a);
                                    }
                                }

                                // Sort by publishDate descending in code
                                list.sort((a1, a2) -> {
                                    long date1 = a1.getPublishDate();
                                    long date2 = a2.getPublishDate();
                                    return Long.compare(date2, date1); // Descending
                                });

                                // Limit to maxCount
                                if (list.size() > (maxCount > 0 ? maxCount : 10)) {
                                    list = list.subList(0, maxCount > 0 ? maxCount : 10);
                                }

                                if (listener != null) {
                                    listener.onLoaded(list);
                                }
                            })
                            .addOnFailureListener(e2 -> {
                                if (listener != null) {
                                    listener.onError(e2 != null ? e2.getMessage() : "Failed to fetch articles");
                                }
                            });
                });
    }

    // =========================
    // UPDATE
    // =========================
    public void update(Article article) {
        if (article.getId() == null) return;

        db.collection("articles")
                .document(article.getId())
                .set(article);
    }

    // =========================
    // DELETE
    // =========================
    public void delete(String id) {
        db.collection("articles")
                .document(id)
                .delete();
    }
}
