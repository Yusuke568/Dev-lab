package com.example.approval.domain.model;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 承認ルートの1段（画面表示用）。候補者が複数いれば、誰か1人の判定でこの段が終わる。
 */
public class ApprovalLevel {

    private final int level;
    private final List<ApprovalStep> candidates;

    public ApprovalLevel(int level, List<ApprovalStep> candidates) {
        this.level = level;
        this.candidates = candidates;
    }

    public int getLevel() { return level; }
    public List<ApprovalStep> getCandidates() { return candidates; }

    /** この段で判定した人の行（未処理なら null） */
    public ApprovalStep getDecided() {
        return candidates.stream().filter(ApprovalStep::isDecided).findFirst().orElse(null);
    }

    public boolean isDecided() {
        return getDecided() != null;
    }

    /**
     * 画面表示用。例:
     *   未処理 … 「1. 山田／佐藤 未処理」
     *   判定済 … 「1. 山田 承認」（判定した人だけを出す）
     */
    public String getLabel() {
        ApprovalStep decided = getDecided();
        if (decided != null) {
            String state = ApprovalStep.APPROVED.equals(decided.getDecision()) ? "承認" : "却下";
            return level + ". " + nameOf(decided) + " " + state;
        }
        String names = candidates.stream().map(ApprovalLevel::nameOf).collect(Collectors.joining("／"));
        return level + ". " + names + " 未処理";
    }

    private static String nameOf(ApprovalStep step) {
        return step.getApproverName() != null ? step.getApproverName() : String.valueOf(step.getApproverStaffId());
    }
}
