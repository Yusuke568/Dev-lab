package com.example.approval.domain.service;

import com.example.approval.domain.model.OrganizationSnapshot;
import com.example.approval.domain.model.RequestType;
import com.example.approval.domain.model.ResolvedRoute;
import com.example.master.domain.model.Department;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 組織図だけから承認ルート（1段に1人）を組み立てる。
 * 副承認者や承認ルートマスタを使わない場合の決め方で、中身は ApprovalRouteResolver の組織図の決め方と同じ。
 *
 * 申請者の所属部署から親部署へ順にたどり、各部署の承認者を集める。
 * 承認者が未設定の部署・申請者本人・すでに並んだ承認者は飛ばし、先頭から maxLevels 人までをルートにする。
 * 申請者本人が承認者で、それより上に別の承認者がいない（最上位の承認者が自分で申請した）ときは空のルート（自動承認）。
 */
public class ApprovalRouteBuilder {

    private final ApprovalRouteResolver resolver = new ApprovalRouteResolver();

    /**
     * @return 承認者の社員IDの並び（1段目から）。最上位の承認者本人の申請なら空（自動承認）
     * @throws IllegalStateException 所属部署がない、または本人以外の承認者も本人も承認者になっていない場合
     */
    public List<Integer> build(int applicantStaffId, Integer departmentId, Map<Integer, Department> departments, int maxLevels) {
        ResolvedRoute route = resolver.resolve(applicantStaffId, departmentId, RequestType.OVERTIME,
                new OrganizationSnapshot(departments, Map.of(), List.of(), maxLevels));
        List<Integer> approvers = new ArrayList<>();
        for (List<Integer> level : route.getLevels()) {
            approvers.add(level.get(0));
        }
        return approvers;
    }
}
