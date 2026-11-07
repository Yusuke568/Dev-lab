package com.example.approval.domain.service;

import com.example.master.domain.model.Department;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 組織図から承認ルート（承認者の並び）を組み立てる。
 *
 * 申請者の所属部署から親部署へ順にたどり、各部署の承認者を集める。
 * 承認者が未設定の部署・申請者本人・すでに並んだ承認者は飛ばし、先頭から maxLevels 人までをルートにする。
 *
 * 申請者本人が承認者で、それより上に別の承認者がいない（最上位の承認者が自分で申請した）ときは、
 * 空のルートを返す。空のルートの申請は自動で承認する（ApprovalWorkflowService）。
 */
public class ApprovalRouteBuilder {

    /**
     * @param applicantStaffId 申請者の社員ID
     * @param departmentId     申請者の所属部署ID（未所属なら null）
     * @param departments      全部署（ID → 部署）
     * @param maxLevels        最大段数（1 以上）
     * @return 承認者の社員IDの並び（1段目から）。最上位の承認者本人の申請なら空（自動承認）
     * @throws IllegalStateException 所属部署がない、または本人以外の承認者も本人も承認者になっていない場合
     */
    public List<Integer> build(int applicantStaffId, Integer departmentId, Map<Integer, Department> departments, int maxLevels) {
        if (departmentId == null) {
            throw new IllegalStateException("所属部署が設定されていないため申請できません。管理者に組織図の設定を依頼してください。");
        }

        Set<Integer> approvers = new LinkedHashSet<>();
        Set<Integer> visited = new HashSet<>();
        boolean applicantIsApprover = false;
        Integer current = departmentId;
        while (current != null && visited.add(current) && approvers.size() < Math.max(1, maxLevels)) {
            Department dept = departments.get(current);
            if (dept == null) {
                break;
            }
            Integer approver = dept.getApproverStaffId();
            if (approver != null && approver == applicantStaffId) {
                applicantIsApprover = true;
            } else if (approver != null) {
                approvers.add(approver);
            }
            current = dept.getParentId();
        }

        if (approvers.isEmpty() && applicantIsApprover) {
            // 最上位の承認者が自分で申請した：承認できる人がほかにいないので自動承認にする
            return new ArrayList<>();
        }
        if (approvers.isEmpty()) {
            throw new IllegalStateException("承認者が設定されていません。管理者に組織図の承認者の設定を依頼してください。");
        }
        return new ArrayList<>(approvers);
    }
}
