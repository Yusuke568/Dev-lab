package com.example.kintai.domain.model.attendance;

import com.example.kintai.domain.model.employee.EmployeeId;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;

/**
 * 特定の日付の勤怠記録（エンティティ）
 *
 * このエンティティの識別子は、社員IDと日付の組み合わせです。
 */
public class AttendanceRecord {
    private final EmployeeId employeeId;
    private final LocalDate workDate;
    private WorkTime workTime;
    private String workDescription; // 作業内容など

    public AttendanceRecord(EmployeeId employeeId, LocalDate workDate, WorkTime workTime) {
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId must not be null");
        this.workDate = Objects.requireNonNull(workDate, "workDate must not be null");
        this.workTime = workTime; // Can be null if not worked
    }

    /**
     * 勤務時間を計算します。
     * @return 勤務時間。未勤務の場合は Duration.ZERO。
     */
    public Duration calculateWorkDuration() {
        return workTime != null ? workTime.getDuration() : Duration.ZERO;
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
