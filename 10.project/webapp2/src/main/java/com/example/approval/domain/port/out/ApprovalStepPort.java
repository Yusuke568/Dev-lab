package com.example.approval.domain.port.out;

import com.example.approval.domain.model.ApprovalStep;
import com.example.approval.domain.model.RequestType;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 申請ごとの承認ステップ（approval_step）の永続化操作を定義するポート。
 */
public interface ApprovalStepPort {

    /** 申請の承認ステップを、渡した承認者の並びで作り直す（再申請時は古いステップを消す）。 */
    void replaceSteps(RequestType requestType, int requestId, List<Integer> approverStaffIds);

    /** 申請の承認ステップを1段目から順に返す。 */
    List<ApprovalStep> findSteps(RequestType requestType, int requestId);

    /** 複数の申請の承認ステップをまとめて返す（申請ID → ステップ）。 */
    Map<Integer, List<ApprovalStep>> findSteps(RequestType requestType, Collection<Integer> requestIds);

    /** 指定した段の判定を記録する。 */
    void recordDecision(RequestType requestType, int requestId, int level, String decision);
}
