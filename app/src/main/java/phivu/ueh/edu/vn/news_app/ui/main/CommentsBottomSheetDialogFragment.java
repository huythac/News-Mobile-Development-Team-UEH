package phivu.ueh.edu.vn.news_app.ui.main;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import phivu.ueh.edu.vn.news_app.R;
import phivu.ueh.edu.vn.news_app.data.repository.CommentRepository;
import phivu.ueh.edu.vn.news_app.model.Comment;
import phivu.ueh.edu.vn.news_app.model.User;

public class CommentsBottomSheetDialogFragment extends DialogFragment {

    private static final String ARG_ARTICLE_ID = "articleId";
    private static final String ARG_FOCUS_INPUT = "focusInput";

    public interface CommentsCountListener {
        void onCommentsCountChanged(int count);
    }

    private String articleId;
    private boolean shouldFocusInput = false;
    private CommentRepository commentRepository;
    private ListenerRegistration commentsListener;
    private CommentsCountListener countListener;

    // Views
    private TextView tvCommentsTitle;
    private ImageView btnCloseComments;
    private RecyclerView rvComments;
    private LinearLayout layoutCommentsEmpty;
    private LinearLayout layoutCommentsError;
    private TextView tvCommentsError;
    private MaterialButton btnCommentsRetry;
    private ProgressBar progressComments;
    private ImageView imgInputAvatar;
    private TextInputEditText edtCommentInput;
    private ImageButton btnSendComment;
    private ImageView btnClearComment;
    private LinearLayout layoutCommentInput;

    private CommentAdapter commentAdapter;
    private List<Comment> commentList = new ArrayList<>();
    private FirebaseUser currentUser;

    public static CommentsBottomSheetDialogFragment newInstance(String articleId) {
        return newInstance(articleId, false);
    }

    public static CommentsBottomSheetDialogFragment newInstance(String articleId, boolean focusInput) {
        return newInstance(articleId, focusInput, null);
    }

    public static CommentsBottomSheetDialogFragment newInstance(String articleId, boolean focusInput, CommentsCountListener listener) {
        CommentsBottomSheetDialogFragment fragment = new CommentsBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ARTICLE_ID, articleId);
        args.putBoolean(ARG_FOCUS_INPUT, focusInput);
        fragment.setArguments(args);
        fragment.countListener = listener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);

        Bundle args = getArguments();
        if (args != null) {
            articleId = args.getString(ARG_ARTICLE_ID);
            shouldFocusInput = args.getBoolean(ARG_FOCUS_INPUT, false);
        }

        if (TextUtils.isEmpty(articleId)) {
            Toast.makeText(getContext(), "Lỗi: Không có ID bài viết", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        commentRepository = new CommentRepository();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_comments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupInputBar();
        setupClickListeners();
        loadComments();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }
        }

        if (shouldFocusInput && edtCommentInput != null) {
            edtCommentInput.post(() -> {
                if (edtCommentInput != null && getContext() != null) {
                    edtCommentInput.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(edtCommentInput, InputMethodManager.SHOW_IMPLICIT);
                    }
                    if (layoutCommentInput != null) {
                        layoutCommentInput.post(() -> {
                            if (layoutCommentInput != null) {
                                layoutCommentInput.requestFocus();
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (commentsListener != null) {
            commentsListener.remove();
            commentsListener = null;
        }
    }

    private void initViews(View view) {
        tvCommentsTitle = view.findViewById(R.id.tvCommentsTitle);
        btnCloseComments = view.findViewById(R.id.btnCloseComments);
        rvComments = view.findViewById(R.id.rvComments);
        layoutCommentsEmpty = view.findViewById(R.id.layoutCommentsEmpty);
        layoutCommentsError = view.findViewById(R.id.layoutCommentsError);
        tvCommentsError = view.findViewById(R.id.tvCommentsError);
        btnCommentsRetry = view.findViewById(R.id.btnCommentsRetry);
        progressComments = view.findViewById(R.id.progressComments);
        imgInputAvatar = view.findViewById(R.id.imgInputAvatar);
        edtCommentInput = view.findViewById(R.id.edtCommentInput);
        btnSendComment = view.findViewById(R.id.btnSendComment);
        btnClearComment = view.findViewById(R.id.btnClearComment);
        layoutCommentInput = view.findViewById(R.id.layoutCommentInput);

        if (currentUser != null && imgInputAvatar != null) {
            String photoUrl = currentUser.getPhotoUrl() != null ?
                    currentUser.getPhotoUrl().toString() : null;
            if (!TextUtils.isEmpty(photoUrl)) {
                try {
                    int avatarSize = getResources()
                            .getDimensionPixelSize(R.dimen.comments_sheet_avatar_size);
                    Picasso.get()
                            .load(photoUrl)
                            .resize(avatarSize, avatarSize)
                            .onlyScaleDown()
                            .centerCrop()
                            .placeholder(android.R.drawable.ic_menu_report_image)
                            .error(android.R.drawable.ic_menu_report_image)
                            .into(imgInputAvatar);
                } catch (Exception e) {
                    imgInputAvatar.setImageResource(android.R.drawable.ic_menu_report_image);
                }
            } else {
                imgInputAvatar.setImageResource(android.R.drawable.ic_menu_report_image);
            }
        } else if (imgInputAvatar != null) {
            imgInputAvatar.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        if (currentUser == null) {
            if (edtCommentInput != null) {
                edtCommentInput.setEnabled(false);
                edtCommentInput.setHint(getString(R.string.comments_require_login));
            }
            if (btnSendComment != null) {
                btnSendComment.setEnabled(false);
                btnSendComment.setBackgroundResource(R.drawable.sendbutton_default);
                btnSendComment.setImageResource(R.drawable.ic_paper_plane_gray);
                btnSendComment.setAlpha(1.0f); // Still visible
            }
        } else {
            if (edtCommentInput != null) {
                updateInputUI(edtCommentInput.getText() != null ?
                        edtCommentInput.getText().toString().trim() : "");
            }
        }
    }

    private void setupRecyclerView() {
        if (rvComments == null) {
            return;
        }

        commentAdapter = new CommentAdapter(getContext(), commentList);
        rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
        rvComments.setAdapter(commentAdapter);
    }

    private void setupInputBar() {
        if (edtCommentInput != null) {
            edtCommentInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updateInputUI(s != null ? s.toString().trim() : "");
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        if (btnClearComment != null) {
            btnClearComment.setOnClickListener(v -> {
                if (edtCommentInput != null) {
                    edtCommentInput.setText("");
                }
            });
        }
    }

    private void updateInputUI(String text) {
        boolean hasText = !TextUtils.isEmpty(text);

        if (btnClearComment != null) {
            btnClearComment.setVisibility(hasText ? View.VISIBLE : View.GONE);
        }

        if (btnSendComment != null) {
            int backgroundRes = hasText ?
                    R.drawable.sendbutton_active :
                    R.drawable.sendbutton_default;
            btnSendComment.setBackgroundResource(backgroundRes);

            int iconRes = hasText ?
                    R.drawable.ic_paper_plane :
                    R.drawable.ic_paper_plane_gray;
            btnSendComment.setImageResource(iconRes);

            btnSendComment.setEnabled(hasText);
            btnSendComment.setAlpha(1.0f);
        }
    }

    private void setupClickListeners() {
        if (btnCloseComments != null) {
            btnCloseComments.setOnClickListener(v -> dismiss());
        }

        if (btnSendComment != null) {
            btnSendComment.setOnClickListener(v -> sendComment());
        }

        if (btnCommentsRetry != null) {
            btnCommentsRetry.setOnClickListener(v -> {
                hideErrorState();
                loadComments();
            });
        }
    }

    private void loadComments() {
        if (TextUtils.isEmpty(articleId)) {
            showErrorState("Không có ID bài viết");
            return;
        }

        showLoadingState();

        commentsListener = commentRepository.observeComments(articleId,
                new CommentRepository.ListCallback() {
                    @Override
                    public void onSuccess(List<Comment> list) {
                        hideLoadingState();
                        hideErrorState();

                        if (list == null) {
                            list = new ArrayList<>();
                        }

                        commentList.clear();
                        commentList.addAll(list);
                        commentAdapter.updateData(commentList);

                        int count = list.size();
                        updateTitle(count);
                        updateEmptyState(list.isEmpty());

                        if (countListener != null) {
                            countListener.onCommentsCountChanged(count);
                        }
                    }

                    @Override
                    public void onError(String err) {
                        hideLoadingState();
                        if (commentList.isEmpty()) {
                            showErrorState(err != null ? err : "Không tải được bình luận");
                        } else {
                            Toast.makeText(getContext(),
                                    "Không thể cập nhật bình luận",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendComment() {
        if (currentUser == null) {
            Toast.makeText(getContext(),
                    getString(R.string.comments_require_login),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (edtCommentInput == null) {
            return;
        }

        String content = edtCommentInput.getText() != null ?
                edtCommentInput.getText().toString().trim() : "";

        if (TextUtils.isEmpty(content)) {
            Toast.makeText(getContext(),
                    getString(R.string.comments_empty_content),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(articleId)) {
            Toast.makeText(getContext(),
                    "Lỗi: Không có ID bài viết",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (btnSendComment != null) {
            btnSendComment.setEnabled(false);
            btnSendComment.setAlpha(0.5f);
        }

        String userId = currentUser.getUid();
        String userName = currentUser.getDisplayName();
        if (TextUtils.isEmpty(userName)) userName = "Người dùng";

        String userAvatar = "";
        if (currentUser.getPhotoUrl() != null) {
            userAvatar = currentUser.getPhotoUrl().toString();
        }

        User sender = new User(userId, userName, currentUser.getEmail(), "USER", userAvatar);

        commentRepository.addComment(articleId, sender, content,
                new CommentRepository.SingleCallback() {
                    @Override
                    public void onSuccess(Comment comment) {
                        if (edtCommentInput != null) edtCommentInput.setText("");
                        hideKeyboard();
                        if (btnSendComment != null) btnSendComment.setEnabled(true);
                        if (rvComments != null) rvComments.smoothScrollToPosition(0);
                    }

                    @Override
                    public void onError(String err) {
                        Toast.makeText(getContext(), "Lỗi gửi: " + err, Toast.LENGTH_SHORT).show();
                        if (btnSendComment != null) btnSendComment.setEnabled(true);
                    }
                });
    }

    private void updateTitle(int count) {
        if (tvCommentsTitle != null) {
            tvCommentsTitle.setText(getString(R.string.comments_title) + " (" + count + ")");
        }
    }

    private void updateEmptyState(boolean isEmpty) {
        if (layoutCommentsEmpty != null) {
            layoutCommentsEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        if (rvComments != null) {
            rvComments.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private void showLoadingState() {
        if (progressComments != null) {
            progressComments.setVisibility(View.VISIBLE);
        }
        if (rvComments != null) {
            rvComments.setVisibility(View.GONE);
        }
        if (layoutCommentsEmpty != null) {
            layoutCommentsEmpty.setVisibility(View.GONE);
        }
        if (layoutCommentsError != null) {
            layoutCommentsError.setVisibility(View.GONE);
        }
    }

    private void hideLoadingState() {
        if (progressComments != null) {
            progressComments.setVisibility(View.GONE);
        }
        if (rvComments != null) {
            rvComments.setVisibility(View.VISIBLE);
        }
    }

    private void showErrorState(String errorMessage) {
        if (progressComments != null) {
            progressComments.setVisibility(View.GONE);
        }
        if (rvComments != null) {
            rvComments.setVisibility(View.GONE);
        }
        if (layoutCommentsEmpty != null) {
            layoutCommentsEmpty.setVisibility(View.GONE);
        }
        if (layoutCommentsError != null) {
            layoutCommentsError.setVisibility(View.VISIBLE);
        }
        if (tvCommentsError != null && errorMessage != null) {
            tvCommentsError.setText(errorMessage);
        }
    }

    private void hideErrorState() {
        if (layoutCommentsError != null) {
            layoutCommentsError.setVisibility(View.GONE);
        }
    }

    private void hideKeyboard() {
        if (getContext() == null || edtCommentInput == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(edtCommentInput.getWindowToken(), 0);
        }
    }
}

