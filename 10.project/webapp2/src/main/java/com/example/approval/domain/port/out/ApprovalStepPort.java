package com.example.approval.domain.port.out;

import com.example.approval.domain.model.ApprovalStep;
import com.example.approval.domain.model.RequestType;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 申請ごとの承認ステップ（approval_step）を扱う出力ポート。
 * 1つの段に候補者が複数いるときは、候補者ごとに1行持つ。誰か1人が判定すればその段は終わる。
 */
public interface ApprovalStepPort {

    /** 申請の承認ルートを作り直す（levels は段ごとの候補者。1段目から）。 */
    void replaceSteps(RequestType requestType, int requestId, List<List<Integer>> levels);

    /** 申請の承認ステップ（段・候補者の順）。 */
    List<ApprovalStep> findSteps(RequestType requestType, int requestId);

    /** 複数の申請の承認ステップ（申請ID → ステップ）。 */
    Map<Integer, List<ApprovalStep>> findSteps(RequestType requestType, Collection<Integer> requestIds);

    /** 段の判定を、判定した候補者の行に記録する。 */
    void recordDecision(RequestType requestType, int requestId, int level, int approverStaffId, String decision);
}
