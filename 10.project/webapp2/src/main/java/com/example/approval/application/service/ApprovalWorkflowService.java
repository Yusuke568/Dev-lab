package com.example.approval.application.service;

import com.example.approval.domain.model.ApprovalLevel;
import com.example.approval.domain.model.ApprovalOutcome;
import com.example.approval.domain.model.ApprovalStep;
import com.example.approval.domain.model.OrganizationSnapshot;
import com.example.approval.domain.model.RequestFacts;
import com.example.approval.domain.model.RequestType;
import com.example.approval.domain.model.ResolvedRoute;
import com.example.approval.domain.model.RoutePreview;
import com.example.approval.domain.port.out.ApprovalStepPort;
import com.example.approval.domain.port.out.OrganizationPort;
import com.example.approval.domain.service.ApprovalRouteResolver;
import com.example.master.domain.model.Department;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 多段階承認の進め方。残業事前申請と休暇申請で共通に使う。
 *
 * 申請の状態（申請中／承認／却下、いま何段目か）は各申請のテーブルが持ち、
 * ここでは承認ルートの作成と、1段分の判定の記録・次の状態の判定だけを行う。
 * ルートの決め方は ApprovalRouteResolver（承認ルートマスタ → 組織図）。
 * 1つの段に候補者が複数いるときは、誰か1人の判定でその段が終わる。
 */
public class ApprovalWorkflowService {

    private final ApprovalStepPort approvalStepPort;
    private final OrganizationPort organizationPort;
    private final ApprovalRouteResolver resolver = new ApprovalRouteResolver();

    public ApprovalWorkflowService(ApprovalStepPort approvalStepPort, OrganizationPort organizationPort) {
        this.approvalStepPort = approvalStepPort;
        this.organizationPort = organizationPort;
    }

    /** 主所属の部署として承認ルートを作る。 */
    public boolean startRoute(RequestType requestType, int requestId, String applicantStaffId) {
        return startRoute(requestType, requestId, applicantStaffId, null);
    }

    /**
     * 承認ルートを決めて保存する。
     * 最上位の承認者本人の申請（ほかに承認者がいない）は、本人を1段目として承認済みで記録する（自動承認）。
     *
     * @param departmentId どの部署の業務としての申請か（兼任者が選ぶ）。null なら主所属
     * @return 自動承認したら true。呼び出し側は申請を承認済みにすること
     * @throws IllegalStateException 所属部署や承認者が設定されていない場合
     */
    public boolean startRoute(RequestType requestType, int requestId, String applicantStaffId, Integer departmentId) {
        return startRoute(requestType, requestId, applicantStaffId, departmentId, null);
    }

    /**
     * @param facts 申請の中身（当月の時間外・深夜か）。承認ルートの条件つきの段を足すかの判定に使う。null なら足さない
     */
    public boolean startRoute(RequestType requestType, int requestId, String applicantStaffId, Integer departmentId,
                              RequestFacts facts) {
        int applicant = Integer.parseInt(applicantStaffId);
        ResolvedRoute route = resolve(requestType, applicant, departmentId, facts);
        if (route.isAutoApproved()) {
            approvalStepPort.replaceSteps(requestType, requestId, List.of(List.of(applicant)));
            approvalStepPort.recordDecision(requestType, requestId, 1, applicant, ApprovalStep.APPROVED);
            return true;
        }
        approvalStepPort.replaceSteps(requestType, requestId, route.getLevels());
        return false;
    }

    /** 申請を保存する前に、承認ルートが組めるか確かめる（組めなければ例外）。 */
    public void checkRouteAvailable(RequestType requestType, String applicantStaffId) {
        checkRouteAvailable(requestType, applicantStaffId, null);
    }

    public void checkRouteAvailable(RequestType requestType, String applicantStaffId, Integer departmentId) {
        checkRouteAvailable(requestType, applicantStaffId, departmentId, null);
    }

    public void checkRouteAvailable(RequestType requestType, String applicantStaffId, Integer departmentId, RequestFacts facts) {
        resolve(requestType, Integer.parseInt(applicantStaffId), departmentId, facts);
    }

    /** この社員がこの部署の業務として申請したら、どのルートになるか（マスタ画面の確認用）。 */
    public ResolvedRoute previewRoute(RequestType requestType, int applicantStaffId, Integer departmentId) {
        return resolve(requestType, applicantStaffId, departmentId, null);
    }

    /**
     * 社員それぞれが（主所属の業務として）申請したらどのルートになるか。組織の情報は1回だけ読む。
     * @param primaryDepartments 社員ID → 主所属の部署ID（未所属なら null）
     */
    public Map<Integer, RoutePreview> previewAll(RequestType requestType, Map<Integer, Integer> primaryDepartments) {
        OrganizationSnapshot org = new OrganizationSnapshot(
                organizationPort.findAllDepartments(),
                organizationPort.findSubApprovers(),
                organizationPort.findApprovalRoutes(),
                organizationPort.findMaxLevels(requestType));
        Map<Integer, RoutePreview> result = new LinkedHashMap<>();
        primaryDepartments.forEach((staffId, deptId) -> {
            try {
                ResolvedRoute route = resolver.resolve(staffId, deptId, requestType, org);
                result.put(staffId, new RoutePreview(route.getLevels(), route.getSource(), route.isAutoApproved(), null));
            } catch (RuntimeException e) {
                result.put(staffId, new RoutePreview(List.of(), null, false, e.getMessage()));
            }
        });
        return result;
    }

    /** 社員が所属するすべての部署ID（主所属が先頭）。申請画面で兼任先を選ばせるのに使う。 */
    public List<Integer> getDepartmentIdsOfStaff(int staffId) {
        return organizationPort.findDepartmentIdsOfStaff(staffId);
    }

    /** 社員が所属するすべての部署（主所属が先頭）。申請画面で兼任先を選ばせるのに使う。 */
    public List<Department> getDepartmentsOfStaff(int staffId) {
        Map<Integer, Department> all = organizationPort.findAllDepartments();
        List<Department> result = new ArrayList<>();
        for (Integer id : organizationPort.findDepartmentIdsOfStaff(staffId)) {
            Department d = all.get(id);
            if (d != null) {
                result.add(d);
            }
        }
        return result;
    }

    private ResolvedRoute resolve(RequestType requestType, int applicant, Integer departmentId, RequestFacts facts) {
        Integer dept = departmentId;
        if (dept == null) {
            dept = organizationPort.findDepartmentIdOfStaff(applicant);
        } else if (!organizationPort.findDepartmentIdsOfStaff(applicant).contains(dept)) {
            throw new IllegalArgumentException("選んだ部署に所属していません。所属部署を選び直してください。");
        }
        OrganizationSnapshot org = new OrganizationSnapshot(
                organizationPort.findAllDepartments(),
                organizationPort.findSubApprovers(),
                organizationPort.findApprovalRoutes(),
                organizationPort.findMaxLevels(requestType));
        return resolver.resolve(applicant, dept, requestType, org, facts);
    }

    /**
     * 現在の段の候補者として判定を記録し、申請全体が次にどうなるかを返す。
     * @throws IllegalStateException 操作した人が現在の段の候補者でない、またはその段がすでに処理済みの場合
     */
    public ApprovalOutcome decide(RequestType requestType, int requestId, int currentLevel,
                                  String approverStaffId, boolean approve) {
        List<ApprovalStep> steps = approvalStepPort.findSteps(requestType, requestId);
        List<ApprovalStep> current = steps.stream()
                .filter(s -> s.getLevel() == currentLevel)
                .collect(Collectors.toList());
        if (current.isEmpty()) {
            throw new IllegalStateException("この申請には承認ルートがありません。申請者に再申請を依頼してください。");
        }
        int approver = Integer.parseInt(approverStaffId);
        if (current.stream().noneMatch(s -> s.getApproverStaffId() == approver)) {
            throw new IllegalStateException("この申請の現在の承認者ではありません。");
        }
        if (current.stream().anyMatch(ApprovalStep::isDecided)) {
            throw new IllegalStateException("この段はすでにほかの承認者が処理しています。");
        }

        approvalStepPort.recordDecision(requestType, requestId, currentLevel, approver,
                approve ? ApprovalStep.APPROVED : ApprovalStep.REJECTED);

        if (!approve) {
            return ApprovalOutcome.REJECTED;
        }
        boolean hasNext = steps.stream().anyMatch(s -> s.getLevel() == currentLevel + 1);
        return hasNext ? ApprovalOutcome.NEXT_LEVEL : ApprovalOutcome.APPROVED;
    }

    /** 申請ごとの承認ルート（画面表示用。段ごとにまとめる）。 */
    public Map<Integer, List<ApprovalLevel>> getRoutes(RequestType requestType, Collection<Integer> requestIds) {
        Map<Integer, List<ApprovalLevel>> result = new LinkedHashMap<>();
        approvalStepPort.findSteps(requestType, requestIds).forEach((requestId, steps) -> {
            Map<Integer, List<ApprovalStep>> byLevel = new TreeMap<>();
            for (ApprovalStep step : steps) {
                byLevel.computeIfAbsent(step.getLevel(), k -> new ArrayList<>()).add(step);
            }
            List<ApprovalLevel> levels = new ArrayList<>();
            byLevel.forEach((level, candidates) -> levels.add(new ApprovalLevel(level, candidates)));
            result.put(requestId, levels);
        });
        return result;
    }
}
