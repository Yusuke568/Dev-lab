package com.example.approval.domain.port.out;

import com.example.approval.domain.model.RequestType;
import com.example.master.domain.model.Department;

import java.util.Map;

/**
 * 承認ルートを組み立てるために、組織図と承認ルート設定を読み込むポート。
 */
public interface OrganizationPort {

    /** 全部署（ID → 部署）。 */
    Map<Integer, Department> findAllDepartments();

    /** 社員の所属部署ID。未所属なら null。 */
    Integer findDepartmentIdOfStaff(int staffId);

    /** 申請種別ごとの最大段数。未設定なら 1。 */
    int findMaxLevels(RequestType requestType);
}
