package com.example.kintai.domain.model.attendance;

import com.example.kintai.domain.model.employee.EmployeeId;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;

/**
 * 迚ｹ螳壹・譌･莉倥・蜍､諤險倬鹸・医お繝ｳ繝・ぅ繝・ぅ・・
 *
 * 縺薙・繧ｨ繝ｳ繝・ぅ繝・ぅ縺ｮ隴伜挨蟄舌・縲∫､ｾ蜩｡ID縺ｨ譌･莉倥・邨・∩蜷医ｏ縺帙〒縺吶・
 */
public class AttendanceRecord {
    private final EmployeeId employeeId;
    private final LocalDate workDate;
    private WorkTime workTime;
    private String workDescription; // 菴懈･ｭ蜀・ｮｹ縺ｪ縺ｩ
    private Integer abstractId;
    private Integer correctionId;
    private Integer correctionUsTime;
    private Integer correctionMidTime;
    private int approvalStatus;

    public AttendanceRecord(EmployeeId employeeId, LocalDate workDate, WorkTime workTime) {
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId must not be null");
        this.workDate = Objects.requireNonNull(workDate, "workDate must not be null");
        this.workTime = workTime; // Can be null if not worked
    }

    /**
     * 蜍､蜍呎凾髢薙ｒ險育ｮ励＠縺ｾ縺吶・
     * @return 蜍､蜍呎凾髢薙よ悴蜍､蜍吶・蝣ｴ蜷医・ Duration.ZERO縲・
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
