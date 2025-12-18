package phivu.ueh.edu.vn.news_app.data.repository;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

import phivu.ueh.edu.vn.news_app.data.remote.CommentFirebaseDAO;
import phivu.ueh.edu.vn.news_app.model.Comment;
import phivu.ueh.edu.vn.news_app.model.User;

public class CommentRepository {

    private final CommentFirebaseDAO remote;

    public interface ListCallback {
        void onSuccess(List<Comment> list);
        void onError(String err);
    }

    public interface SingleCallback {
        void onSuccess(Comment comment);
        void onError(String err);
    }

    public CommentRepository() {
        remote = new CommentFirebaseDAO();
    }

    /**
     * Observe comments in realtime
     */
    public ListenerRegistration observeComments(String articleId, ListCallback callback) {
        return remote.observeComments(articleId, new CommentFirebaseDAO.ListListener() {
            @Override
            public void onLoaded(List<Comment> list) {
                if (callback != null) callback.onSuccess(list);
            }

            @Override
            public void onError(String err) {
                if (callback != null) callback.onError(err);
            }
        });
    }

    /**
     * Add a new comment (CẬP NHẬT: Nhận User object)
     */
    public void addComment(String articleId, User user, String content, SingleCallback callback) {
        // Truyền thẳng đối tượng User xuống DAO
        remote.addComment(articleId, user, content, new CommentFirebaseDAO.SingleListener() {
            @Override
            public void onLoaded(Comment comment) {
                if (callback != null) callback.onSuccess(comment);
            }

            @Override
            public void onError(String err) {
                if (callback != null) callback.onError(err);
            }
        });
    }

    /**
     * Delete a comment
     */
    public void deleteComment(String articleId, String commentId) {
        remote.deleteComment(articleId, commentId);
    }

    /**
     * Get comment count for an article
     */
    public void getCommentCount(String articleId, CountCallback callback) {
        remote.getCommentCount(articleId, new CommentFirebaseDAO.CountListener() {
            @Override
            public void onCount(int count) {
                if (callback != null) callback.onCount(count);
            }
        });
    }

    public interface CountCallback {
        void onCount(int count);
    }
}