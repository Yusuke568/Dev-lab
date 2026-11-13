package com.example.kintai.application.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 承認者の「承認・確認待ち」バッジの件数 */
class ApprovalBadgeServiceTest {

    @Test
    void 合計と内訳を出し_0件の種類は内訳に出さない() {
        ApprovalBadgeService.Counts c = new ApprovalBadgeService.Counts(2, 1, 0, 3);
        assertEquals(6, c.getTotal());
        assertEquals("残業事前申請 2件・残業の実績確認 1件・休暇申請 3件", c.getSummary());
    }

    @Test
    void 待ちがなければ合計0で内訳は空() {
        ApprovalBadgeService.Counts c = new ApprovalBadgeService.Counts(0, 0, 0, 0);
        assertEquals(0, c.getTotal());
        assertEquals("", c.getSummary());
    }
}
