package domain.attendance;

import domain.contract.ProjectContract;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 勤怠テンプレート展開 & 未入力検知ドメインサービス
 */
public class AttendanceTemplateService {

    /**
     * 指定月の全営業日に対してテンプレート勤怠リストを生成する
     *
     * @param employeeId 社員ID
     * @param yearMonth 対象年月
     * @param contract 案件契約（所定時間テンプレート）
     * @return 初期生成された日次勤怠リスト
     */
    public List<DailyAttendance> generateMonthlyTemplate(String employeeId, YearMonth yearMonth, ProjectContract contract) {
        List<DailyAttendance> list = new ArrayList<>();
        int daysInMonth = yearMonth.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = yearMonth.atDay(day);
            String recordId = String.format("%s_%s", employeeId, date.toString());
            boolean isWeekend = (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY);

            DailyAttendance attendance;
            if (isWeekend) {
                attendance = DailyAttendance.createHoliday(recordId, employeeId, date);
            } else {
                attendance = DailyAttendance.createFromTemplate(
                        recordId, employeeId, date,
                        contract.getDefaultStartTime(),
                        contract.getDefaultEndTime(),
                        contract.getDefaultBreakMinutes()
                );
            }
            list.add(attendance);
        }
        return list;
    }

    /**
     * 未入力（ため込み）日を検出する
     *
     * @param attendances 当月の日次勤怠リスト
     * @param referenceDate 基準日（本日）
     * @return 基準日以前で未入力（isFilled = false）の勤怠リスト（休日は除く）
     */
    public List<DailyAttendance> findUnfilledAttendances(List<DailyAttendance> attendances, LocalDate referenceDate) {
        return attendances.stream()
                .filter(a -> !a.getWorkDate().isAfter(referenceDate))
                .filter(a -> {
                    // 土日等の休日（未入力）はため込み未入力の警告対象外とする
                    boolean isWeekend = (a.getWorkDate().getDayOfWeek() == DayOfWeek.SATURDAY || a.getWorkDate().getDayOfWeek() == DayOfWeek.SUNDAY);
                    if (isWeekend && a.getAbstractId() == 10 && !a.isFilled()) {
                        return false;
                    }
                    return !a.isFilled();
                })
                .collect(Collectors.toList());
    }
}
