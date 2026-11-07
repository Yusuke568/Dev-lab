package com.example.approval.domain.port.out;

import com.example.approval.domain.model.ApprovalRoute;
import com.example.approval.domain.model.RequestType;
import com.example.master.domain.model.Department;

import java.util.List;
import java.util.Map;

/**
 * 承認ルートを決めるための組織の情報を読む出力ポート。
 */
public interface OrganizationPort {

    /** 全部署（ID → 部署）。 */
    Map<Integer, Department> findAllDepartments();

    /** 社員の主所属の部署ID（未所属なら null）。 */
    Integer findDepartmentIdOfStaff(int staffId);

    /** 社員が所属するすべての部署ID（主所属が先頭、続けて兼任部署）。 */
    List<Integer> findDepartmentIdsOfStaff(int staffId);

    /** 部署ID → 副承認者の社員ID。 */
    Map<Integer, List<Integer>> findSubApprovers();

    /** 承認ルートマスタ（段と割り当て先を含む）。 */
    List<ApprovalRoute> findApprovalRoutes();

    /** 組織図で決めるときの最大段数。 */
    int findMaxLevels(RequestType requestType);
}
