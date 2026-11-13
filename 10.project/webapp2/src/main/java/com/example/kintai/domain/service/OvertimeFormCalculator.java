package com.example.kintai.domain.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 個人別時間外勤務申請兼実績報告書（Excel 様式 9版）の1行分を組み立てる。
 *
 * 様式は「残業開始時刻〜残業終了時刻」の1区間から、休憩を除いた時間を Excel の式で出す（隠し列）。
 *   通常の区間 … 〜8:30 / 9:00〜12:00 / 13:00〜18:00 / 18:30〜22:00
 *   深夜の区間 … 22:30〜26:00 / 27:00〜29:00（実績報告はさらに 37:00〜42:00）
 * 1区間に入らない分（早出と残業の両方がある日の早出の分など）は「調整」欄で足す。
 *
 * 申請時：予定の残業の区間（18:00〜予定退勤。早出だけなら予定出勤〜9:00）を入れる。時間は様式の式どおり（休憩を除く）。
 *         補正（通常）は通常の調整、補正（深夜）は深夜の調整で足す。
 *         区間がない申請（補正・間接で時間外になる日）は、申請した時間外を調整で入れる。
 * 実績：勤怠表の出退勤から同じように区間を入れ、勤怠の補正（深夜）を深夜の調整に、
 *       残りの差を通常の調整に入れて、勤怠表の時間外と合わせる
 *       （勤怠表の時間外は補正・間接・週40時間を含むので、様式の式だけでは合わないことがある）。
 */
public final class OvertimeFormCalculator {

    public static final int STANDARD_START = 9 * 60;
    public static final int STANDARD_END = 18 * 60;

    /** 様式の休憩を除いた区間（分。24時以降は 24*60 を足した値） */
    private static final int[][] NORMAL_BLOCKS = {{9 * 60, 12 * 60}, {13 * 60, 18 * 60}, {18 * 60 + 30, 22 * 60}};
    private static final int EARLY_BLOCK_END = 8 * 60 + 30;
    private static final int[][] PLANNED_NIGHT_BLOCKS = {{22 * 60 + 30, 26 * 60}, {27 * 60, 29 * 60}};
    private static final int[][] ACTUAL_NIGHT_BLOCKS = {{22 * 60 + 30, 26 * 60}, {27 * 60, 29 * 60}, {37 * 60, 42 * 60}};

    private OvertimeFormCalculator() {
    }

    /** 区間（分。終わりが翌日なら 24*60 以上） */
    public record Span(int start, int end) {
    }

    /** 様式1行の時刻と調整（調整は分。正なら「+」、負なら「-」。通常と深夜で別の欄） */
    public record TimeCells(Span span, int adjustMinutes, int nightAdjustMinutes) {
        public static final TimeCells EMPTY = new TimeCells(null, 0, 0);

        public TimeCells(Span span, int adjustMinutes) {
            this(span, adjustMinutes, 0);
        }

        public boolean isEmpty() {
            return span == null && adjustMinutes == 0 && nightAdjustMinutes == 0;
        }
    }

    /** 承認欄1つ。skipped は承認ルートにない段（様式では「－」） */
    public record ApprovalMark(LocalDate date, boolean skipped) {
        public static final ApprovalMark SKIPPED = new ApprovalMark(null, true);
        public static final ApprovalMark BLANK = new ApprovalMark(null, false);
    }

    /** 承認ルートの1段（承認者の名前と判定） */
    public record Step(int level, String approverName, boolean approved, LocalDate decidedOn) {
    }

    /**
     * 申請時の時刻。
     * @param plannedStart   予定出勤（分。空なら 9:00）
     * @param plannedEnd     予定退勤（分。翌日なら 24*60 以上。空なら 18:00）
     * @param plannedMinutes アプリで申請した時間外（分）
     */
    public static TimeCells planned(Integer plannedStart, Integer plannedEnd, int plannedMinutes) {
        return planned(plannedStart, plannedEnd, plannedMinutes, 0, 0);
    }

    /**
     * @param correction    補正（通常）の分（plannedMinutes に含まれている）
     * @param correctionMid 補正（深夜）の分（plannedMinutes に含まれている）
     */
    public static TimeCells planned(Integer plannedStart, Integer plannedEnd, int plannedMinutes,
                                    int correction, int correctionMid) {
        return planned(plannedStart, plannedEnd, plannedMinutes, correction, correctionMid, false);
    }

    /** @param holiday 休日出勤の申請か。休日は予定出勤〜予定退勤を区間にする（勤務時間を全部時間外の見込みにしている） */
    public static TimeCells planned(Integer plannedStart, Integer plannedEnd, int plannedMinutes,
                                    int correction, int correctionMid, boolean holiday) {
        if (holiday && plannedStart != null && plannedEnd != null) {
            return new TimeCells(new Span(plannedStart, plannedEnd), correction, correctionMid);
        }
        int start = plannedStart == null ? STANDARD_START : plannedStart;
        int end = plannedEnd == null ? STANDARD_END : plannedEnd;
        Span early = start < STANDARD_START ? new Span(start, STANDARD_START) : null;
        Span late = end > STANDARD_END ? new Span(Math.max(start, STANDARD_END), end) : null;
        if (late != null) {
            int adjust = early != null ? plannedFormMinutes(early) : 0;
            return new TimeCells(late, adjust + correction, correctionMid);
        }
        if (early != null) {
            return new TimeCells(early, correction, correctionMid);
        }
        // 出退勤は標準どおりでも、補正・間接で時間外になる日（深夜の補正のほかは通常の調整に入れる）
        return plannedMinutes > 0 ? new TimeCells(null, plannedMinutes - correctionMid, correctionMid) : TimeCells.EMPTY;
    }

    /**
     * 実績の時刻。勤怠がない・時間外がない日は空。
     * @param workStart       出勤（分）
     * @param workEnd         退勤（分。翌日なら 24*60 以上）
     * @param holiday         休日（土日・祝日・会社休日）か。休日は出勤から退勤までを区間にする
     * @param overtimeMinutes 勤怠表の時間外（分）
     */
    public static TimeCells actual(Integer workStart, Integer workEnd, boolean holiday, int overtimeMinutes) {
        return actual(workStart, workEnd, holiday, overtimeMinutes, 0);
    }

    /** @param correctionMid 勤怠の補正（深夜）の分（深夜の調整に入れる） */
    public static TimeCells actual(Integer workStart, Integer workEnd, boolean holiday, int overtimeMinutes, int correctionMid) {
        if (workStart == null || workEnd == null || overtimeMinutes <= 0) {
            return TimeCells.EMPTY;
        }
        Span span;
        int extra = 0;
        if (holiday) {
            span = new Span(workStart, workEnd);
        } else {
            Span early = workStart < STANDARD_START ? new Span(workStart, STANDARD_START) : null;
            Span late = workEnd > STANDARD_END ? new Span(Math.max(workStart, STANDARD_END), workEnd) : null;
            if (late != null) {
                span = late;
                extra = early != null ? actualFormMinutes(early) : 0;
            } else {
                span = early;
            }
        }
        int formMinutes = (span == null ? 0 : actualFormMinutes(span)) + extra;
        return new TimeCells(span, extra + (overtimeMinutes - formMinutes) - correctionMid, correctionMid);
    }

    /** 様式の申請時の式で出る時間（通常＋深夜） */
    public static int plannedFormMinutes(Span s) {
        return normalMinutes(s) + overlap(s, PLANNED_NIGHT_BLOCKS);
    }

    /** 様式の実績報告の式で出る時間（通常＋深夜） */
    public static int actualFormMinutes(Span s) {
        return normalMinutes(s) + overlap(s, ACTUAL_NIGHT_BLOCKS);
    }

    private static int normalMinutes(Span s) {
        // 様式の最初の区間は「開始が 8:30 より前なら、開始〜8:30（終了がそれより前なら終了）」
        int early = s.start() < EARLY_BLOCK_END ? Math.max(0, Math.min(s.end(), EARLY_BLOCK_END) - s.start()) : 0;
        return early + overlap(s, NORMAL_BLOCKS);
    }

    private static int overlap(Span s, int[][] blocks) {
        int total = 0;
        for (int[] b : blocks) {
            total += Math.max(0, Math.min(s.end(), b[1]) - Math.max(s.start(), b[0]));
        }
        return total;
    }

    /**
     * 承認欄（課長／所属長／社長）。1段目→課長、2段目→所属長、3段目以降（最後の段）→社長。
     * ルートにない段は「－」、まだ判定していない段は空欄。
     * 承認ルートの「条件つきで足す段」（当月の時間外が○時間以上・深夜）は、条件に当たった申請にだけ足されるので、
     * 当たらなかった申請はその欄が「－」になる（所属長・社長の欄を何時間から出すかは、承認ルートの設定で決まる）。
     * @param autoApprovedOn 承認ルートの段がなく自動承認された日（段があるときは使わない）
     */
    public static List<ApprovalMark> approvals(List<Step> steps, LocalDate autoApprovedOn) {
        List<ApprovalMark> marks = new ArrayList<>(List.of(ApprovalMark.SKIPPED, ApprovalMark.SKIPPED, ApprovalMark.SKIPPED));
        if (steps.isEmpty()) {
            if (autoApprovedOn != null) {
                marks.set(0, new ApprovalMark(autoApprovedOn, false));
            }
            return marks;
        }
        int maxLevel = steps.stream().mapToInt(Step::level).max().orElse(1);
        for (int level = 1; level <= maxLevel; level++) {
            int column = Math.min(level, 3) - 1;
            if (level >= 3 && level != maxLevel) {
                continue; // 3段目以降は最後の段の日付を「社長」欄に入れる
            }
            LocalDate approvedOn = null;
            for (Step st : steps) {
                if (st.level() == level && st.approved()) {
                    approvedOn = st.decidedOn();
                }
            }
            marks.set(column, approvedOn != null ? new ApprovalMark(approvedOn, false) : ApprovalMark.BLANK);
        }
        return marks;
    }

    /**
     * 勤怠を保存したときの、実績の確認日。
     *   時間外がない・申請の時間と合う（休日は申請の時間以下）→ 確認日（すでにあればそのまま、なければ today）
     *   合わない → null（申請し直して承認された日を使う）
     */
    public static LocalDate actualConfirmedOn(int plannedMinutes, int actualMinutes, boolean holiday,
                                              LocalDate current, LocalDate today) {
        boolean ok = actualMinutes <= 0 || actualMinutes == plannedMinutes || (holiday && actualMinutes <= plannedMinutes);
        if (!ok) return null;
        return current != null ? current : today;
    }

    /**
     * 実績報告の承認欄（課長／所属長／社長）。
     *   確認日がある … ふだんの段は確認日。条件つきの段（baseLevels より後）は、その段が承認していれば確認日、まだなら空欄
     *   確認日がない … 実績が申請の時間と合っていれば（申請し直して承認された）、段ごとの承認日。合っていなければ空欄
     * ルートにない段は「－」。
     * @param baseLevels 条件つきの段を除いた段数（null なら全部ふだんの段）
     * @param matches    実績が申請の時間と合っているか（確認日がないときに使う）
     */
    public static List<ApprovalMark> actualApprovals(List<Step> steps, Integer baseLevels, LocalDate confirmedOn,
                                                     boolean matches, LocalDate autoApprovedOn) {
        if (confirmedOn == null) {
            if (matches) return approvals(steps, autoApprovedOn);
            List<ApprovalMark> blank = new ArrayList<>(approvals(steps, autoApprovedOn));
            for (int i = 0; i < blank.size(); i++) {
                if (!blank.get(i).skipped()) blank.set(i, ApprovalMark.BLANK);
            }
            return blank;
        }
        List<ApprovalMark> marks = new ArrayList<>(List.of(ApprovalMark.SKIPPED, ApprovalMark.SKIPPED, ApprovalMark.SKIPPED));
        if (steps.isEmpty()) {
            marks.set(0, new ApprovalMark(confirmedOn, false)); // 自動承認
            return marks;
        }
        int maxLevel = steps.stream().mapToInt(Step::level).max().orElse(1);
        int base = baseLevels == null ? maxLevel : baseLevels;
        for (int level = 1; level <= maxLevel; level++) {
            int column = Math.min(level, 3) - 1;
            if (level >= 3 && level != maxLevel) continue; // 3段目以降は最後の段を「社長」欄に
            final int lv = level;
            boolean approved = steps.stream().anyMatch(st -> st.level() == lv && st.approved());
            boolean conditional = level > base;
            marks.set(column, !conditional || approved ? new ApprovalMark(confirmedOn, false) : ApprovalMark.BLANK);
        }
        return marks;
    }

    /** 段の承認者の名前（判定した人。まだなら最初の候補者） */
    public static String approverName(List<Step> steps, int level) {
        String first = null;
        for (Step st : steps) {
            if (st.level() != level) continue;
            if (st.decidedOn() != null && st.approverName() != null) return st.approverName();
            if (first == null) first = st.approverName();
        }
        return first;
    }
}
