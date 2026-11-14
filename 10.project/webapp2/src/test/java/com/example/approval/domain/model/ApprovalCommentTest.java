package com.example.approval.domain.model;

import com.example.approval.application.service.ApprovalWorkflowService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** 承認・却下の理由（任意）の整え方と、申請者の画面に出す値 */
class ApprovalCommentTest {

    @Test
    void 理由は前後の空白を除き_空なら記録しない_255文字まで() {
        assertNull(ApprovalWorkflowService.normalizeComment(null));
        assertNull(ApprovalWorkflowService.normalizeComment("  　 "));
        assertEquals("時間を見直してください", ApprovalWorkflowService.normalizeComment("　時間を見直してください \n"));
        assertEquals(255, ApprovalWorkflowService.normalizeComment("あ".repeat(300)).length());
    }

    @Test
    void 判定した段は判定した人の日時と理由を出す() {
        ApprovalLevel level = new ApprovalLevel(1, List.of(
                new ApprovalStep(1, 10, "山田", null, null),
                new ApprovalStep(1, 11, "佐藤", ApprovalStep.REJECTED, "2026-11-09 08:05", "予定の時間が長すぎます")));
        assertEquals("1. 佐藤 却下", level.getLabel());
        assertEquals("11/9 08:05", level.getDecidedAtShort());
        assertEquals("予定の時間が長すぎます", level.getComment());
        assertEquals(ApprovalStep.REJECTED, level.getDecision());
    }

    @Test
    void 未処理の段は日時も理由も空() {
        ApprovalLevel level = new ApprovalLevel(2, List.of(new ApprovalStep(2, 10, "山田", null, null)));
        assertEquals("", level.getDecidedAtShort());
        assertEquals("", level.getComment());
        assertEquals("", level.getDecision());
        assertEquals("2. 山田 未処理", level.getLabel());
    }
}
