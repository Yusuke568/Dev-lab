package com.example.approval.domain.model;

import com.example.master.domain.model.Department;

import java.util.List;
import java.util.Map;

/**
 * 承認ルートを決めるのに使う組織の情報（その時点の写し）。
 *
 * @param departments  全部署（ID → 部署）。主承認者は Department.getApproverStaffId()
 * @param subApprovers 部署ID → 副承認者の社員ID
 * @param routes       承認ルートマスタ（割り当て先を含む）
 * @param maxLevels    組織図で決めるときの最大段数（承認ルート設定）
 */
public record OrganizationSnapshot(Map<Integer, Department> departments,
                                   Map<Integer, List<Integer>> subApprovers,
                                   List<ApprovalRoute> routes,
                                   int maxLevels) {
}
