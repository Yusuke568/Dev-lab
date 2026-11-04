package com.example.notice.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 上長からのお知らせ。宛先の部署と、その下の部署に所属する社員に表示する。
 */
public class Notice {

    private static final DateTimeFormatter CREATED = DateTimeFormatter.ofPattern("M/d HH:mm");

    private final int id;
    private final int authorStaffId;
    private final String authorName;
    private final int departmentId;
    private final String departmentName;
    private final String body;
    private final LocalDate expiresOn;
    private final LocalDateTime createdAt;

    public Notice(int id, int authorStaffId, String authorName, int departmentId, String departmentName,
                  String body, LocalDate expiresOn, LocalDateTime createdAt) {
        this.id = id;
        this.authorStaffId = authorStaffId;
        this.authorName = authorName;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.body = body;
        this.expiresOn = expiresOn;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getAuthorStaffId() { return authorStaffId; }
    public String getAuthorName() { return authorName; }
    public int getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public String getBody() { return body; }
    public LocalDate getExpiresOn() { return expiresOn; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** 画面表示用の作成日時（11/5 09:30） */
    public String getCreatedAtLabel() {
        return createdAt == null ? "" : createdAt.format(CREATED);
    }
}
