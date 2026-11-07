package com.example.approval.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 承認ルートの「条件つきで足す段」（残業事前申請だけに使う）。
 * 条件に当たった申請では、ルートのふだんの段のあとに、この段の承認者を足す。
 *
 *   MONTHLY_OVERTIME … 当月の時間外（実績＋今回の申請）が thresholdMinutes 以上
 *   NIGHT            … 申請の予定が深夜（22:00〜翌5:00）にかかる
 */
public class ExtraLevel {

    public static final String MONTHLY_OVERTIME = "MONTHLY_OVERTIME";
    public static final String NIGHT = "NIGHT";

    private final String condition;
    private final Integer thresholdMinutes;
    private final List<Integer> approvers;

    public ExtraLevel(String condition, Integer thresholdMinutes, List<Integer> approvers) {
        if (!MONTHLY_OVERTIME.equals(condition) && !NIGHT.equals(condition)) {
            throw new IllegalArgumentException("追加の段の条件が分かりません: " + condition);
        }
        if (MONTHLY_OVERTIME.equals(condition) && (thresholdMinutes == null || thresholdMinutes <= 0)) {
            throw new IllegalArgumentException("追加の段の時間（何時間以上か）を入れてください。");
        }
        this.condition = condition;
        this.thresholdMinutes = MONTHLY_OVERTIME.equals(condition) ? thresholdMinutes : null;
        this.approvers = approvers != null ? new ArrayList<>(approvers) : new ArrayList<>();
    }

    public String getCondition() { return condition; }
    public Integer getThresholdMinutes() { return thresholdMinutes; }
    public List<Integer> getApprovers() { return Collections.unmodifiableList(approvers); }

    /** この申請で段を足すか */
    public boolean matches(RequestFacts facts) {
        if (facts == null) {
            return false;
        }
        if (NIGHT.equals(condition)) {
            return facts.night();
        }
        return facts.monthlyOvertimeMinutes() >= thresholdMinutes;
    }

    /** 画面・承認ルートの説明に出す条件（例：「当月の時間外 18.5時間以上」） */
    public String getConditionLabel() {
        if (NIGHT.equals(condition)) {
            return "深夜（22時〜翌5時）にかかる";
        }
        return "当月の時間外 " + formatHours(thresholdMinutes) + "時間以上";
    }

    /** 1110 → "18.5"、2700 → "45" */
    public static String formatHours(int minutes) {
        if (minutes % 60 == 0) {
            return String.valueOf(minutes / 60);
        }
        return java.math.BigDecimal.valueOf(minutes).divide(java.math.BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString();
    }
}
