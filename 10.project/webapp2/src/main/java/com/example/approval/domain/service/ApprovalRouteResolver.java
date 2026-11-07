package com.example.approval.domain.service;

import com.example.approval.domain.model.ApprovalRoute;
import com.example.approval.domain.model.OrganizationSnapshot;
import com.example.approval.domain.model.RequestType;
import com.example.approval.domain.model.ResolvedRoute;
import com.example.approval.domain.model.RouteAssignment;
import com.example.master.domain.model.Department;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 申請の承認ルートを決める。
 *
 * 決め方（上から順に、最初に見つかったもの）
 *   1. 申請者に割り当てた承認ルート
 *   2. 申請の部署（兼任なら申請時に選んだ部署、なければ主所属）に割り当てた承認ルート。なければ上位部署の割り当てを使う
 *   3. 組織図：申請の部署から上位部署へたどり、各部署の主承認者と副承認者をその段の候補者にする（承認ルート設定の段数まで）
 *
 * 共通の決まり
 *   ・候補者から申請者本人を除く。前の段に出てきた人も除く。候補者がいなくなった段は飛ばす
 *   ・全部の段が空になり、申請者本人が承認者の一人だった（最上位の承認者が自分で申請した）ときは自動承認
 *   ・それ以外で承認者がいなければ申請できない（例外）
 */
public class ApprovalRouteResolver {

    public ResolvedRoute resolve(int applicantStaffId, Integer departmentId, RequestType requestType,
                                 OrganizationSnapshot org) {
        // 1. 申請者に割り当てたルート
        Optional<ApprovalRoute> staffRoute = findRoute(org, requestType, RouteAssignment.STAFF, applicantStaffId);
        if (staffRoute.isPresent()) {
            return fromRoute(staffRoute.get(), applicantStaffId, "（社員に割り当て）");
        }

        if (departmentId == null) {
            throw new IllegalStateException("所属部署が設定されていないため申請できません。管理者に組織図の設定を依頼してください。");
        }

        // 2. 部署に割り当てたルート（なければ上位部署の割り当て）
        Set<Integer> visited = new HashSet<>();
        Integer current = departmentId;
        while (current != null && visited.add(current)) {
            Optional<ApprovalRoute> deptRoute = findRoute(org, requestType, RouteAssignment.DEPARTMENT, current);
            if (deptRoute.isPresent()) {
                Department dept = org.departments().get(current);
                String where = dept != null ? dept.getName() : String.valueOf(current);
                return fromRoute(deptRoute.get(), applicantStaffId, "（部署「" + where + "」に割り当て）");
            }
            Department dept = org.departments().get(current);
            current = dept != null ? dept.getParentId() : null;
        }

        // 3. 組織図
        return fromOrganization(applicantStaffId, departmentId, org);
    }

    /** 割り当てが合うルート。申請種別を指定したルートを、種別を指定していないルートより優先する */
    private static Optional<ApprovalRoute> findRoute(OrganizationSnapshot org, RequestType type, String targetType, int targetId) {
        return org.routes().stream()
                .filter(r -> r.appliesTo(type) && r.isAssignedTo(targetType, targetId))
                .min(Comparator.comparing((ApprovalRoute r) -> r.getRequestType() == null ? 1 : 0)
                        .thenComparing(r -> r.getId() != null ? r.getId() : Integer.MAX_VALUE));
    }

    private static ResolvedRoute fromRoute(ApprovalRoute route, int applicant, String how) {
        String source = "承認ルート「" + route.getName() + "」" + how;
        return finish(route.getLevels(), applicant, source,
                "承認ルート「" + route.getName() + "」に、申請者以外の承認者が設定されていません。管理者に承認ルートの設定を依頼してください。");
    }

    private static ResolvedRoute fromOrganization(int applicant, int departmentId, OrganizationSnapshot org) {
        List<List<Integer>> levels = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        Integer current = departmentId;
        while (current != null && visited.add(current)) {
            Department dept = org.departments().get(current);
            if (dept == null) {
                break;
            }
            List<Integer> candidates = new ArrayList<>();
            if (dept.getApproverStaffId() != null) {
                candidates.add(dept.getApproverStaffId());
            }
            candidates.addAll(org.subApprovers().getOrDefault(current, List.of()));
            levels.add(candidates);
            current = dept.getParentId();
        }
        ResolvedRoute all = finish(levels, applicant, "組織図",
                "承認者が設定されていません。管理者に組織図の承認者の設定を依頼してください。");
        // 組織図では、承認ルート設定の段数まで
        int max = Math.max(1, org.maxLevels());
        if (all.getLevels().size() > max) {
            return new ResolvedRoute(new ArrayList<>(all.getLevels().subList(0, max)), false, all.getSource());
        }
        return all;
    }

    /** 本人と、前の段に出てきた人を除き、空の段を飛ばす。空になれば自動承認かエラー */
    private static ResolvedRoute finish(List<List<Integer>> rawLevels, int applicant, String source, String emptyMessage) {
        boolean applicantIsApprover = false;
        Set<Integer> used = new HashSet<>();
        List<List<Integer>> levels = new ArrayList<>();
        for (List<Integer> raw : rawLevels) {
            Set<Integer> candidates = new LinkedHashSet<>();
            for (Integer staffId : raw) {
                if (staffId == null) {
                    continue;
                }
                if (staffId == applicant) {
                    applicantIsApprover = true;
                } else if (!used.contains(staffId)) {
                    candidates.add(staffId);
                }
            }
            if (!candidates.isEmpty()) {
                used.addAll(candidates);
                levels.add(new ArrayList<>(candidates));
            }
        }
        if (levels.isEmpty()) {
            if (applicantIsApprover) {
                return new ResolvedRoute(new ArrayList<>(), true, source + "（上位の承認者がいないため自動承認）");
            }
            throw new IllegalStateException(emptyMessage);
        }
        return new ResolvedRoute(levels, false, source);
    }
}
