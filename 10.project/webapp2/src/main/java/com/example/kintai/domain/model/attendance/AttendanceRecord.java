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

    private final EmployeeId employeeId;
    private final LocalDate workDate;
    private WorkTime workTime;
    /** 出勤だけ・退勤だけ打刻した日の時刻（workTime が揃っていないときだけ使う） */
    private LocalTime partialStartTime;
    private LocalTime partialEndTime;
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

    /** 所定労働時間（これを超えた総勤務時間が時間外） */
    public static final int STANDARD_WORK_MINUTES = 8 * 60;

    /**
     * 総勤務時間（作業報告書の (e)）を計算します。
     * 出退勤時刻から休憩時間（BreakSchedule の時間帯と重なる分）を引いた実働時間に、
     * 稼働補正（補正(通)＋補正(深)）と、出退勤時刻の外で行った間接作業の時間（勤務時間(間接) (d)）を足す。
     * 例：9:00〜23:00、補正(通)+0:30、補正(深)+0:30 → 実働12:00 ＋ 1:00 ＝ 13:00
     *     9:00〜17:45、補正(通)+0:15、間接2:00     → 実働7:45 ＋ 0:15 ＋ 2:00 ＝ 10:00
     * @return 総勤務時間。未勤務の場合は補正と間接の分だけ（なければ Duration.ZERO）。
     */
    public Duration calculateWorkDuration() {
        return calculateRawWorkDuration().plusMinutes(getCorrectionMinutes()).plusMinutes(getIndirectMinutes());
    }

    /** 勤務時間(直接)（作業報告書の (f) ＝ (e) − (d)） */
    public Duration calculateDirectWorkDuration() {
        return calculateWorkDuration().minusMinutes(getIndirectMinutes());
    }

    /** 時間外（残業時間）：総勤務時間のうち所定の8時間を超えた分。補正と間接作業の時間も含めて数える。 */
    public int calculateOvertimeMinutes() {
        return (int) Math.max(0, calculateWorkDuration().toMinutes() - STANDARD_WORK_MINUTES);
    }

    /**
     * 出退勤時刻から休憩控除のみを行った実働時間（補正・間接作業を含まない）。
     * @return 休憩控除後の実働時間。未勤務の場合は Duration.ZERO。
     */
    public Duration calculateRawWorkDuration() {
        if (workTime == null) {
            return Duration.ZERO;
        }
        Duration raw = workTime.getDuration();
        Duration afterBreak = raw.minus(calculateBreakDeduction(workTime));
        return afterBreak.isNegative() ? Duration.ZERO : afterBreak;
    }

    private long getCorrectionMinutes() {
        return (correctionUsTime != null ? correctionUsTime : 0)
                + (correctionMidTime != null ? correctionMidTime : 0);
    }

    private long getIndirectMinutes() {
        return indirectTime != null ? indirectTime : 0;
    }

    /**
     * 勤務時間と休憩時間帯（BreakSchedule）の重なり。日をまたぐ勤務は翌日の休憩時間帯との重なりも見る。
     */
    private static Duration calculateBreakDeduction(WorkTime workTime) {
        return Duration.ofMinutes(BreakSchedule.overlapMinutes(
                workTime.getStartMinuteOfDay(), workTime.getEndMinuteFromStartDay()));
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

    /**
     * 出勤・退勤の片方だけが入力されている日の時刻を設定します。
     * 両方揃っている日は WorkTime を使い、こちらは使いません。
     */
    public void setPartialTimes(LocalTime startTime, LocalTime endTime) {
        this.partialStartTime = startTime;
        this.partialEndTime = endTime;
    }

    /** 出勤時刻（未入力なら null）。片方だけ打刻した日も返す。 */
    public LocalTime getStartTime() {
        return workTime != null ? workTime.getStartTime() : partialStartTime;
    }

    /** 退勤時刻（未入力なら null）。片方だけ打刻した日も返す。 */
    public LocalTime getEndTime() {
        return workTime != null ? workTime.getEndTime() : partialEndTime;
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
