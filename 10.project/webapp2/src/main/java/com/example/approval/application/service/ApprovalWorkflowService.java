package com.example.approval.application.service;

import com.example.approval.domain.model.ApprovalOutcome;
import com.example.approval.domain.model.ApprovalStep;
import com.example.approval.domain.model.RequestType;
import com.example.approval.domain.port.out.ApprovalStepPort;
import com.example.approval.domain.port.out.OrganizationPort;
import com.example.approval.domain.service.ApprovalRouteBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 組織図に基づく多段階承認の進め方。残業事前申請と休暇申請で共通に使う。
 *
 * 申請の状態（申請中／承認／却下、いま何段目か）は各申請のテーブルが持ち、
 * ここでは承認ルートの作成と、1段分の判定の記録・次の状態の判定だけを行う。
 */
public class ApprovalWorkflowService {

    private final ApprovalStepPort approvalStepPort;
    private final OrganizationPort organizationPort;
    private final ApprovalRouteBuilder routeBuilder = new ApprovalRouteBuilder();

    public ApprovalWorkflowService(ApprovalStepPort approvalStepPort, OrganizationPort organizationPort) {
        this.approvalStepPort = approvalStepPort;
        this.organizationPort = organizationPort;
    }

    /**
     * 申請者の所属部署から承認ルートを作り、保存する。
     * @throws IllegalStateException 所属部署や承認者が設定されていない場合
     */
    public void startRoute(RequestType requestType, int requestId, String applicantStaffId) {
        int applicant = Integer.parseInt(applicantStaffId);
        List<Integer> approvers = buildRoute(requestType, applicant);
        approvalStepPort.replaceSteps(requestType, requestId, approvers);
    }

    /**
     * 申請を保存する前に、承認ルートが組めるか確かめる（組めなければ例外）。
     */
    public void checkRouteAvailable(RequestType requestType, String applicantStaffId) {
        buildRoute(requestType, Integer.parseInt(applicantStaffId));
    }

    /**
     * 現在の段の承認者として判定を記録し、申請全体が次にどうなるかを返す。
     * @throws IllegalStateException 操作した人が現在の段の承認者でない、またはすでに処理済みの場合
     */
    public ApprovalOutcome decide(RequestType requestType, int requestId, int currentLevel,
                                  String approverStaffId, boolean approve) {
        List<ApprovalStep> steps = approvalStepPort.findSteps(requestType, requestId);
        ApprovalStep current = steps.stream()
                .filter(s -> s.getLevel() == currentLevel)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("この申請には承認ルートがありません。申請者に再申請を依頼してください。"));

        if (current.getApproverStaffId() != Integer.parseInt(approverStaffId)) {
            throw new IllegalStateException("この申請の現在の承認者ではありません。");
        }
        if (current.isDecided()) {
            throw new IllegalStateException("この段はすでに処理済みです。");
        }

        approvalStepPort.recordDecision(requestType, requestId, currentLevel,
                approve ? ApprovalStep.APPROVED : ApprovalStep.REJECTED);

        if (!approve) {
            return ApprovalOutcome.REJECTED;
        }
        boolean hasNext = steps.stream().anyMatch(s -> s.getLevel() == currentLevel + 1);
        return hasNext ? ApprovalOutcome.NEXT_LEVEL : ApprovalOutcome.APPROVED;
    }

    /** 申請ごとの承認ルート（画面表示用）。 */
    public Map<Integer, List<ApprovalStep>> getRoutes(RequestType requestType, Collection<Integer> requestIds) {
        return approvalStepPort.findSteps(requestType, requestIds);
    }

    private List<Integer> buildRoute(RequestType requestType, int applicant) {
        return routeBuilder.build(
                applicant,
                organizationPort.findDepartmentIdOfStaff(applicant),
                organizationPort.findAllDepartments(),
                organizationPort.findMaxLevels(requestType));
    }
}
