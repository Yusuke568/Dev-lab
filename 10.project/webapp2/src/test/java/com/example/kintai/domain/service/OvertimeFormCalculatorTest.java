package com.example.kintai.domain.service;

import com.example.kintai.domain.service.OvertimeFormCalculator.ApprovalMark;
import com.example.kintai.domain.service.OvertimeFormCalculator.Span;
import com.example.kintai.domain.service.OvertimeFormCalculator.Step;
import com.example.kintai.domain.service.OvertimeFormCalculator.TimeCells;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 時間外申請書（Excel 様式）の1行の時刻・調整・承認欄 */
class OvertimeFormCalculatorTest {

    private static int t(int h, int m) {
        return h * 60 + m;
    }

    @Test
    void 様式の式と同じく休憩を除いて数える() {
        assertEquals(t(3, 30), OvertimeFormCalculator.plannedFormMinutes(new Span(t(18, 0), t(22, 0))));  // 18:00〜18:30 は休憩
        assertEquals(t(2, 0), OvertimeFormCalculator.plannedFormMinutes(new Span(t(20, 0), t(22, 0))));
        assertEquals(t(4, 30), OvertimeFormCalculator.plannedFormMinutes(new Span(t(20, 0), t(25, 0))));  // 22:00〜22:30 は休憩
        assertEquals(t(1, 30), OvertimeFormCalculator.plannedFormMinutes(new Span(t(7, 0), t(9, 0))));    // 8:30〜9:00 は休憩
        assertEquals(t(8, 0), OvertimeFormCalculator.actualFormMinutes(new Span(t(9, 0), t(18, 0))));
    }

    @Test
    void 申請時は残業の区間を入れ_早出もある日は早出の分を調整で足す() {
        TimeCells late = OvertimeFormCalculator.planned(null, t(22, 0), t(4, 0));
        assertEquals(new Span(t(18, 0), t(22, 0)), late.span());
        assertEquals(0, late.adjustMinutes());

        TimeCells early = OvertimeFormCalculator.planned(t(7, 0), null, t(2, 0));
        assertEquals(new Span(t(7, 0), t(9, 0)), early.span());

        TimeCells both = OvertimeFormCalculator.planned(t(7, 0), t(20, 0), t(4, 0));
        assertEquals(new Span(t(18, 0), t(20, 0)), both.span());
        assertEquals(t(1, 30), both.adjustMinutes());

        TimeCells nextDay = OvertimeFormCalculator.planned(t(13, 0), t(25, 30), t(7, 30));
        assertEquals(new Span(t(18, 0), t(25, 30)), nextDay.span());
    }

    @Test
    void 休日の申請は予定出勤から予定退勤までを区間にする() {
        TimeCells cells = OvertimeFormCalculator.planned(t(9, 0), t(12, 0), t(3, 0), 0, 0, true);
        assertEquals(new Span(t(9, 0), t(12, 0)), cells.span());
        assertEquals(0, cells.adjustMinutes());
        assertEquals(t(3, 0), OvertimeFormCalculator.plannedFormMinutes(cells.span()));
    }

    @Test
    void 申請の補正は通常と深夜の調整に分けて入れる() {
        // 18:00〜22:00（様式 3:30）＋ 補正 0:30 ＋ 補正（深夜）0:15
        TimeCells cells = OvertimeFormCalculator.planned(null, t(22, 0), t(4, 45), 30, 15);
        assertEquals(new Span(t(18, 0), t(22, 0)), cells.span());
        assertEquals(30, cells.adjustMinutes());
        assertEquals(15, cells.nightAdjustMinutes());

        // 補正だけの申請
        TimeCells only = OvertimeFormCalculator.planned(null, null, 45, 30, 15);
        assertEquals(null, only.span());
        assertEquals(30, only.adjustMinutes());
        assertEquals(15, only.nightAdjustMinutes());
    }

    @Test
    void 実績の補正深夜は深夜の調整に入れ_残りの差を通常の調整に入れる() {
        // 9:00〜22:00、時間外 4:30（補正（深夜）0:30 を含む）→ 様式 3:30 ＋ 通常 +0:30 ＋ 深夜 +0:30
        TimeCells a = OvertimeFormCalculator.actual(t(9, 0), t(22, 0), false, t(4, 30), 30);
        assertEquals(30, a.adjustMinutes());
        assertEquals(30, a.nightAdjustMinutes());
    }

    @Test
    void 補正や間接だけで時間外になる申請は調整だけで入れる() {
        TimeCells cells = OvertimeFormCalculator.planned(t(9, 0), t(17, 45), t(2, 0));
        assertEquals(null, cells.span());
        assertEquals(t(2, 0), cells.adjustMinutes());
        assertTrue(OvertimeFormCalculator.planned(null, null, 0).isEmpty());
    }

    @Test
    void 実績は勤怠表の時間外と合うように調整を入れる() {
        // 9:00〜22:00、時間外 4:00（様式の式だと 18:00〜22:00 は 3:30）→ 調整 +0:30
        TimeCells a = OvertimeFormCalculator.actual(t(9, 0), t(22, 0), false, t(4, 0));
        assertEquals(new Span(t(18, 0), t(22, 0)), a.span());
        assertEquals(30, a.adjustMinutes());

        // 補正で時間外が増えた日（18:00〜19:00 は様式の式だと 0:30、時間外 1:30）→ 調整 +1:00
        assertEquals(60, OvertimeFormCalculator.actual(t(9, 0), t(19, 0), false, t(1, 30)).adjustMinutes());

        // 休日出勤（9:00〜18:00 の 8時間のうち、週40時間を超えた 2時間だけが時間外）→ 調整 -6:00
        TimeCells holiday = OvertimeFormCalculator.actual(t(9, 0), t(18, 0), true, t(2, 0));
        assertEquals(new Span(t(9, 0), t(18, 0)), holiday.span());
        assertEquals(-t(6, 0), holiday.adjustMinutes());

        // 時間外がない日・勤怠がない日は空
        assertTrue(OvertimeFormCalculator.actual(t(9, 0), t(18, 0), false, 0).isEmpty());
        assertTrue(OvertimeFormCalculator.actual(null, null, false, t(1, 0)).isEmpty());
    }

    @Test
    void 承認欄は1段目が課長_2段目が所属長_最後の段が社長で_ルートにない段はハイフン() {
        LocalDate d1 = LocalDate.of(2026, 10, 15);
        LocalDate d2 = LocalDate.of(2026, 10, 16);
        List<ApprovalMark> oneLevel = OvertimeFormCalculator.approvals(List.of(new Step(1, "山田", true, d1)), null);
        assertEquals(List.of(new ApprovalMark(d1, false), ApprovalMark.SKIPPED, ApprovalMark.SKIPPED), oneLevel);

        List<ApprovalMark> pending = OvertimeFormCalculator.approvals(List.of(
                new Step(1, "山田", true, d1), new Step(2, "椎名", false, null)), null);
        assertEquals(List.of(new ApprovalMark(d1, false), ApprovalMark.BLANK, ApprovalMark.SKIPPED), pending);

        List<ApprovalMark> four = OvertimeFormCalculator.approvals(List.of(
                new Step(1, "a", true, d1), new Step(2, "b", true, d1), new Step(3, "c", true, d1), new Step(4, "d", true, d2)), null);
        assertEquals(new ApprovalMark(d2, false), four.get(2));

        List<ApprovalMark> auto = OvertimeFormCalculator.approvals(List.of(), d1);
        assertEquals(List.of(new ApprovalMark(d1, false), ApprovalMark.SKIPPED, ApprovalMark.SKIPPED), auto);
    }

    @Test
    void 課長名は判定した人_まだなら最初の候補者() {
        List<Step> steps = List.of(new Step(1, "主", false, null), new Step(1, "副", true, LocalDate.of(2026, 10, 1)),
                new Step(2, "所属長", false, null));
        assertEquals("副", OvertimeFormCalculator.approverName(steps, 1));
        assertEquals("所属長", OvertimeFormCalculator.approverName(steps, 2));
        assertEquals(null, OvertimeFormCalculator.approverName(steps, 3));
    }
}
