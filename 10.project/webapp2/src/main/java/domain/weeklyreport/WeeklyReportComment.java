package domain.weeklyreport;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 週報レビューコメント値オブジェクト
 * （SES特有の管理層専用メモフラグ isPrivateToMgmt をサポート）
 */
public class WeeklyReportComment {
    private final String id;
    private final String authorId;
    private final AuthorRole authorRole;
    private final String comment;
    private final boolean isPrivateToMgmt; // 本人に見せない管理層専用メモ
    private final LocalDateTime createdAt;

    public WeeklyReportComment(String id, String authorId, AuthorRole authorRole,
                               String comment, boolean isPrivateToMgmt, LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "IDは必須です。");
        this.authorId = Objects.requireNonNull(authorId, "投稿者IDは必須です。");
        this.authorRole = Objects.requireNonNull(authorRole, "役割は必須です。");
        this.comment = Objects.requireNonNull(comment, "コメントは必須です。");
        this.isPrivateToMgmt = isPrivateToMgmt;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getAuthorId() { return authorId; }
    public AuthorRole getAuthorRole() { return authorRole; }
    public String getComment() { return comment; }
    public boolean isPrivateToMgmt() { return isPrivateToMgmt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
