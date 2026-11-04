package com.example.notice.application.service;

import com.example.notice.application.port.in.NoticeUseCase;
import com.example.notice.domain.model.Notice;
import com.example.notice.domain.model.NoticeDepartment;
import com.example.notice.domain.port.out.NoticePort;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * 上長からのお知らせ。宛先にできるのは、組織図で自分が承認者になっている部署とその下の部署だけ。
 */
public class NoticeService implements NoticeUseCase {

    /** 本文の最大文字数 */
    public static final int MAX_BODY_LENGTH = 500;

    private final NoticePort noticePort;
    private final Clock clock;

    public NoticeService(NoticePort noticePort, Clock clock) {
        this.noticePort = noticePort;
        this.clock = clock;
    }

    @Override
    public List<Notice> getNoticesFor(String staffId) {
        return noticePort.findVisibleFor(Integer.parseInt(staffId), LocalDate.now(clock));
    }

    @Override
    public List<Notice> getMyNotices(String authorStaffId) {
        return noticePort.findByAuthor(Integer.parseInt(authorStaffId));
    }

    @Override
    public List<NoticeDepartment> getTargetDepartments(String authorStaffId) {
        return noticePort.findManagedDepartments(Integer.parseInt(authorStaffId));
    }

    @Override
    public void post(String authorStaffId, int departmentId, String body, LocalDate expiresOn) {
        int author = Integer.parseInt(authorStaffId);
        String text = body == null ? "" : body.strip();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("お知らせの本文を入力してください。");
        }
        if (text.length() > MAX_BODY_LENGTH) {
            throw new IllegalArgumentException("お知らせの本文は" + MAX_BODY_LENGTH + "文字までです。");
        }
        boolean managed = noticePort.findManagedDepartments(author).stream().anyMatch(d -> d.getId() == departmentId);
        if (!managed) {
            throw new IllegalArgumentException("この部署にはお知らせを出せません。組織図で自分が承認者になっている部署（とその下の部署）を選んでください。");
        }
        if (expiresOn != null && expiresOn.isBefore(LocalDate.now(clock))) {
            throw new IllegalArgumentException("掲載期限には今日以降の日付を入れてください。");
        }
        noticePort.insert(author, departmentId, text, expiresOn);
    }

    @Override
    public void delete(String authorStaffId, int noticeId) {
        if (!noticePort.delete(noticeId, Integer.parseInt(authorStaffId))) {
            throw new IllegalArgumentException("お知らせが見つからないか、自分が出したものではありません。");
        }
    }
}
