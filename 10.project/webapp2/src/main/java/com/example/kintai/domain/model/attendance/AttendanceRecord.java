package com.example.kintai.domain.model.attendance;

import com.example.kintai.domain.model.employee.EmployeeId;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * 特定�E日付�E勤怠記録�E�エンチE��チE���E�E
 *
 * こ�EエンチE��チE��の識別子�E、社員IDと日付�E絁E��合わせです、E
 */
public class AttendanceRecord {
    private static final LocalTime BREAK_START = LocalTime.of(12, 0);
    private static final LocalTime BREAK_END = LocalTime.of(13, 0);

    private final EmployeeId employeeId;
    private final LocalDate workDate;
    private WorkTime workTime;
    private String workDescription; // 作業冁E��など
    private Integer abstractId;
    private Integer correctionId;
    private Integer correctionUsTime;
    private Integer correctionMidTime;
    private int approvalStatus;
    private String workWeek;
    private int overtimeMinutes;
    private Integer indirectTime;
    private Integer totalWorkTime;
    private Integer totalDirectWorkTime;

    public AttendanceRecord(EmployeeId employeeId, LocalDate workDate, WorkTime workTime) {
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId must not be null");
        this.workDate = Objects.requireNonNull(workDate, "workDate must not be null");
        this.workTime = workTime; // Can be null if not worked
    }

    /**
     * 勤務時間を計算します。
     * 出退勤時刻から求めた実働時間から休憩時間（12:00-13:00 と重なる分）を控除し、
     * 派遣先ごとに勤務時間の計上ルールが異なるため、補正CDで登録された補正時間
     * （補正(通)＋補正(深)）を加算した値を最終的な勤務時間とする。
     * @return 休憩控除・補正後の勤務時間。未勤務の場合は Duration.ZERO。
     */
    public Duration calculateWorkDuration() {
        return calculateRawWorkDuration().plusMinutes(getCorrectionMinutes());
    }

    /**
     * 出退勤時刻から休憩控除のみを行った、補正を含まない実働時間。
     * 「時間外（残業）」の判定はこちらを基準にする。補正は給与計算上の勤務時間の
     * 水増し・調整であって、実際に働いた時間の長さとは無関係のため、
     * 補正を含む calculateWorkDuration() で時間外を判定すると、
     * 定時どおりに退勤した日でも補正分だけ「残業扱い」になってしまう。
     * @return 休憩控除後の実働時間。未勤務の場合は Duration.ZERO。
     */
    public Duration calculateRawWorkDuration() {
        if (workTime == null) {
            return Duration.ZERO;
        }
        Duration raw = workTime.getDuration();
        Duration afterBreak = raw.minus(calculateBreakDeduction(workTime.getStartTime(), workTime.getEndTime()));
        return afterBreak.isNegative() ? Duration.ZERO : afterBreak;
    }

    private long getCorrectionMinutes() {
        return (correctionUsTime != null ? correctionUsTime : 0)
                + (correctionMidTime != null ? correctionMidTime : 0);
    }

    private static Duration calculateBreakDeduction(LocalTime start, LocalTime end) {
        LocalTime overlapStart = start.isAfter(BREAK_START) ? start : BREAK_START;
        LocalTime overlapEnd = end.isBefore(BREAK_END) ? end : BREAK_END;
        if (overlapStart.isBefore(overlapEnd)) {
            return Duration.between(overlapStart, overlapEnd);
        }
        return Duration.ZERO;
    }

    // --- Getters and Setters ---

    public EmployeeId getEmployeeId() {
        return employeeId;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public WorkTime getWorkTime() {
        return workTime;
    }

    public void setWorkTime(WorkTime workTime) {
        this.workTime = workTime;
    }

    public String getWorkDescription() {
        return workDescription;
    }

    public void setWorkDescription(String workDescription) {
        this.workDescription = workDescription;
    }

    public Integer getAbstractId() { return abstractId; }
    public void setAbstractId(Integer abstractId) { this.abstractId = abstractId; }
    
    public Integer getCorrectionId() { return correctionId; }
    public void setCorrectionId(Integer correctionId) { this.correctionId = correctionId; }
    
    public Integer getCorrectionUsTime() { return correctionUsTime; }
    public void setCorrectionUsTime(Integer correctionUsTime) { this.correctionUsTime = correctionUsTime; }
    
    public Integer getCorrectionMidTime() { return correctionMidTime; }
    public void setCorrectionMidTime(Integer correctionMidTime) { this.correctionMidTime = correctionMidTime; }
    
    public int getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(int approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getWorkWeek() { return workWeek; }
    public void setWorkWeek(String workWeek) { this.workWeek = workWeek; }

    public int getOvertimeMinutes() { return overtimeMinutes; }
    public void setOvertimeMinutes(int overtimeMinutes) { this.overtimeMinutes = overtimeMinutes; }

    public Integer getIndirectTime() { return indirectTime; }
    public void setIndirectTime(Integer indirectTime) { this.indirectTime = indirectTime; }

    public Integer getTotalWorkTime() { return totalWorkTime; }
    public void setTotalWorkTime(Integer totalWorkTime) { this.totalWorkTime = totalWorkTime; }

    public Integer getTotalDirectWorkTime() { return totalDirectWorkTime; }
    public void setTotalDirectWorkTime(Integer totalDirectWorkTime) { this.totalDirectWorkTime = totalDirectWorkTime; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttendanceRecord that = (AttendanceRecord) o;
        return employeeId.equals(that.employeeId) && workDate.equals(that.workDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, workDate);
    }

    @Override
    public String toString() {
        return "AttendanceRecord{" +
                "employeeId=" + employeeId +
                ", workDate=" + workDate +
                ", workTime=" + workTime +
                '}';
    }
}
