package com.example.kintai.domain.model.attendance;

import java.time.LocalTime;
import java.util.List;

/**
 * 休憩時間帯。出勤から退勤までの間にこの時間帯と重なった分は、勤務時間から引く。
 *
 * 5:00〜5:30、8:30〜9:00、12:00〜13:00、18:00〜18:30、22:00〜22:30、2:00〜3:00
 * （日をまたぐ勤務は、翌日の同じ時間帯とも重なりを見る）
 */
public final class BreakSchedule {

    /** 休憩時間帯（開始・終了）。勤怠画面の JavaScript にも同じ値を渡して使う */
    public static final List<LocalTime[]> PERIODS = List.of(
            period(5, 0, 5, 30),
            period(8, 30, 9, 0),
            period(12, 0, 13, 0),
            period(18, 0, 18, 30),
            period(22, 0, 22, 30),
            period(2, 0, 3, 0));

    private BreakSchedule() {
    }

    private static LocalTime[] period(int startHour, int startMinute, int endHour, int endMinute) {
        return new LocalTime[] {LocalTime.of(startHour, startMinute), LocalTime.of(endHour, endMinute)};
    }

    /**
     * 勤務（開始日の0時からの分。日をまたぐ終了は1440分以上）と休憩時間帯の重なり（分）。
     * 当日と翌日の休憩時間帯の両方と比べる。
     */
    public static int overlapMinutes(int startMinute, int endMinute) {
        int total = 0;
        for (int dayOffset = 0; dayOffset <= 24 * 60; dayOffset += 24 * 60) {
            for (LocalTime[] p : PERIODS) {
                int breakStart = p[0].getHour() * 60 + p[0].getMinute() + dayOffset;
                int breakEnd = p[1].getHour() * 60 + p[1].getMinute() + dayOffset;
                int overlapStart = Math.max(startMinute, breakStart);
                int overlapEnd = Math.min(endMinute, breakEnd);
                if (overlapStart < overlapEnd) {
                    total += overlapEnd - overlapStart;
                }
            }
        }
        return total;
    }

    /** 勤怠画面の JavaScript 用（[["05:00","05:30"], ...] の JSON）。 */
    public static String toJson() {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < PERIODS.size(); i++) {
            if (i > 0) json.append(',');
            json.append("[\"").append(PERIODS.get(i)[0]).append("\",\"").append(PERIODS.get(i)[1]).append("\"]");
        }
        return json.append(']').toString();
    }
}
