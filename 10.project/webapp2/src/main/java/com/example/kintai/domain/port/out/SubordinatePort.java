package com.example.kintai.domain.port.out;

import com.example.kintai.domain.model.attendance.Subordinate;

import java.util.List;

/**
 * 組織図上の配下の社員を調べるためのポート。
 */
public interface SubordinatePort {

    /** 社員が、承認者の配下（自分自身は除く）に所属しているか。 */
    boolean isManagedBy(String employeeId, String approverId);

    /** 承認者の配下の社員（自分自身は除く）を、部署の並び順で返す。 */
    List<Subordinate> findManagedStaff(String approverId);
}
