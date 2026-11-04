package com.example.notice.domain.port.out;

import com.example.notice.domain.model.Notice;
import com.example.notice.domain.model.NoticeDepartment;

import java.time.LocalDate;
import java.util.List;

/**
 * お知らせの永続化ポート。
 */
public interface NoticePort {

    /** 社員の所属部署とその上の部署あてで、掲載期限（today）が切れていないお知らせ（新しい順）。 */
    List<Notice> findVisibleFor(int staffId, LocalDate today);

    /** 上長が出したお知らせ（新しい順）。 */
    List<Notice> findByAuthor(int authorStaffId);

    /** 上長がお知らせを出せる部署（承認者になっている部署とその下の部署）。 */
    List<NoticeDepartment> findManagedDepartments(int approverStaffId);

    void insert(int authorStaffId, int departmentId, String body, LocalDate expiresOn);

    /** 自分が出したお知らせを消す。消せたら true。 */
    boolean delete(int noticeId, int authorStaffId);
}
