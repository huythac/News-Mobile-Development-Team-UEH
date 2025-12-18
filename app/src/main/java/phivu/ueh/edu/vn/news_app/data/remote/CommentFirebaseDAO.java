package phivu.ueh.edu.vn.news_app.data.remote;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import phivu.ueh.edu.vn.news_app.model.Comment;
import phivu.ueh.edu.vn.news_app.model.User;

public class CommentFirebaseDAO {

    private final FirebaseFirestore db;

    public interface ListListener {
        void onLoaded(List<Comment> list);
        void onError(String err);
    }

    public interface SingleListener {
        void onLoaded(Comment comment);
        void onError(String err);
    }

    public CommentFirebaseDAO() {
        db = FirebaseFirestore.getInstance();
    }

    // =========================
    // OBSERVE COMMENTS (Realtime)
    // =========================
    public ListenerRegistration observeComments(String articleId, ListListener listener) {
        if (articleId == null || articleId.trim().isEmpty()) {
            if (listener != null) listener.onError("Article ID is null");
            return null;
        }

        return db.collection("articles")
                .document(articleId.trim())
                .collection("comments")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        if (listener != null) listener.onError(error.getMessage());
                        return;
                    }

                    if (snapshot == null) {
                        if (listener != null) listener.onLoaded(new ArrayList<>());
                        return;
                    }

                    List<Comment> comments = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        // Firestore tự động map JSON nested "user" vào đối tượng User trong Comment
                        Comment comment = doc.toObject(Comment.class);
                        if (comment != null) {
                            comment.setId(doc.getId());
                            comments.add(comment);
                        }
                    }

                    if (listener != null) listener.onLoaded(comments);
                });
    }

    // =========================
    // ADD COMMENT (CẬP NHẬT: Nhận User object)
    // =========================
    public void addComment(String articleId, User user, String content, SingleListener listener) {
        if (articleId == null || articleId.trim().isEmpty()) {
            if (listener != null) listener.onError("Article ID is null");
            return;
        }

        if (content == null || content.trim().isEmpty()) {
            if (listener != null) listener.onError("Comment content is empty");
            return;
        }

        if (user == null) {
            if (listener != null) listener.onError("User info is missing");
            return;
        }

        // Tạo Map dữ liệu để đẩy lên Firestore
        Map<String, Object> commentData = new HashMap<>();
        commentData.put("articleId", articleId.trim());

        // 🔹 QUAN TRỌNG: Lưu nguyên đối tượng User vào field "user"
        // Firestore sẽ tự động chuyển đối tượng User thành một Map (JSON object)
        commentData.put("user", user);

        commentData.put("content", content.trim());
        commentData.put("createdAt", System.currentTimeMillis());

        db.collection("articles")
                .document(articleId.trim())
                .collection("comments")
                .add(commentData)
                .addOnSuccessListener(docRef -> {
                    // Lấy lại comment vừa tạo để trả về cho UI
                    docRef.get().addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            Comment comment = doc.toObject(Comment.class);
                            if (comment != null) {
                                comment.setId(doc.getId());
                                if (listener != null) listener.onLoaded(comment);
                            }
                        }
                    }).addOnFailureListener(e -> {
                        if (listener != null) listener.onError(e.getMessage());
                    });
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onError(e.getMessage());
                });
    }

    // =========================
    // DELETE COMMENT
    // =========================
    public void deleteComment(String articleId, String commentId) {
        if (articleId == null || commentId == null) return;

        db.collection("articles")
                .document(articleId.trim())
                .collection("comments")
                .document(commentId.trim())
                .delete();
    }

    // =========================
    // GET COMMENT COUNT
    // =========================
    public void getCommentCount(String articleId, CountListener listener) {
        if (articleId == null || articleId.trim().isEmpty()) {
            if (listener != null) listener.onCount(0);
            return;
        }

        db.collection("articles")
                .document(articleId.trim())
                .collection("comments")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (listener != null) listener.onCount(querySnapshot.size());
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onCount(0);
                });
    }

    public interface CountListener {
        void onCount(int count);
    }
}