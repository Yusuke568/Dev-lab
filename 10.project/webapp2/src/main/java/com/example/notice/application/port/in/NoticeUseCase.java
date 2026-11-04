package com.example.notice.application.port.in;

import com.example.notice.domain.model.Notice;
import com.example.notice.domain.model.NoticeDepartment;

import java.time.LocalDate;
import java.util.List;

/**
 * 上長からのお知らせ（作成・削除・表示）のユースケース。
 */
public interface NoticeUseCase {

    /** 社員に表示するお知らせ。 */
    List<Notice> getNoticesFor(String staffId);

    /** 上長が出したお知らせ。 */
    List<Notice> getMyNotices(String authorStaffId);

    /** 上長がお知らせを出せる部署。空なら出せない。 */
    List<NoticeDepartment> getTargetDepartments(String authorStaffId);

    /**
     * お知らせを出す。
     * @throws IllegalArgumentException 本文が空・長すぎる、宛先が出せない部署、期限が過去のとき
     */
    void post(String authorStaffId, int departmentId, String body, LocalDate expiresOn);

    /** 自分が出したお知らせを消す。 */
    void delete(String authorStaffId, int noticeId);
}
