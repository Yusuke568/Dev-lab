package com.example.kintai.application.port.in;

import java.time.LocalDate;
import java.util.List;

/**
 * 勤怠記録を更新するユースケースインターフェース（入りポート）。
 */
public interface UpdateAttendanceUseCase {

    void updateAttendance(List<AttendanceUpdateCommand> commands);

    /**
     * 1日分の勤怠更新コマンド。
     * Webアダプタ側のDTO（KintaiRecordDto）から変換して渡す。
     */
    class AttendanceUpdateCommand {
        private final String employeeId;
        private final LocalDate date;
        private final String week;
        private final String fromTime; // "HH:mm" or null
        private final String toTime;   // "HH:mm" or null
        private final int overtimeMinutes;
        private final Integer abstractId;
        private final String memo;
        private final Integer correctionId;
        private final Integer correctionUsTime;
        private final Integer correctionMidTime;
        private final Integer indirectTime;
        private final Integer totalWorkTime;
        private final Integer totalDirectWorkTime;
        private final boolean temporary;

        public AttendanceUpdateCommand(String employeeId, LocalDate date, String week, String fromTime, String toTime,
                                        int overtimeMinutes, Integer abstractId, String memo, Integer correctionId,
                                        Integer correctionUsTime, Integer correctionMidTime, Integer indirectTime,
                                        Integer totalWorkTime, Integer totalDirectWorkTime, boolean temporary) {
            this.employeeId = employeeId;
            this.date = date;
            this.week = week;
            this.fromTime = fromTime;
            this.toTime = toTime;
            this.overtimeMinutes = overtimeMinutes;
            this.abstractId = abstractId;
            this.memo = memo;
            this.correctionId = correctionId;
            this.correctionUsTime = correctionUsTime;
            this.correctionMidTime = correctionMidTime;
            this.indirectTime = indirectTime;
            this.totalWorkTime = totalWorkTime;
            this.totalDirectWorkTime = totalDirectWorkTime;
            this.temporary = temporary;
        }

        public String getEmployeeId() { return employeeId; }
        public LocalDate getDate() { return date; }
        public String getWeek() { return week; }
        public String getFromTime() { return fromTime; }
        public String getToTime() { return toTime; }
        public int getOvertimeMinutes() { return overtimeMinutes; }
        public Integer getAbstractId() { return abstractId; }
        public String getMemo() { return memo; }
        public Integer getCorrectionId() { return correctionId; }
        public Integer getCorrectionUsTime() { return correctionUsTime; }
        public Integer getCorrectionMidTime() { return correctionMidTime; }
        public Integer getIndirectTime() { return indirectTime; }
        public Integer getTotalWorkTime() { return totalWorkTime; }
        public Integer getTotalDirectWorkTime() { return totalDirectWorkTime; }
        public boolean isTemporary() { return temporary; }
    }
}
