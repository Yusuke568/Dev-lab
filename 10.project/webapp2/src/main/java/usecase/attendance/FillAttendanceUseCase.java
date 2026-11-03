package usecase.attendance;

import domain.attendance.AttendanceRepository;
import domain.attendance.DailyAttendance;
import domain.attendance.LeaveType;
import domain.attendance.WorkTimeSegment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/**
 * 日次勤怠 事後入力ユースケース
 */
public class FillAttendanceUseCase {
    private final AttendanceRepository attendanceRepository;

    public FillAttendanceUseCase(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = Objects.requireNonNull(attendanceRepository);
    }

    public static class Input {
        public final String employeeId;
        public final LocalDate workDate;
        public final LocalTime startTime;
        public final LocalTime endTime;
        public final int breakMinutes;
        public final int abstractId;
        public final LeaveType leaveType;
        public final String remarks;
        public final List<WorkTimeSegment> segments;

        public Input(String employeeId, LocalDate workDate, LocalTime startTime,
                     LocalTime endTime, int breakMinutes, int abstractId, LeaveType leaveType,
                     String remarks, List<WorkTimeSegment> segments) {
            this.employeeId = employeeId;
            this.workDate = workDate;
            this.startTime = startTime;
            this.endTime = endTime;
            this.breakMinutes = breakMinutes;
            this.abstractId = abstractId;
            this.leaveType = leaveType;
            this.remarks = remarks;
            this.segments = segments;
        }

        public Input(String employeeId, LocalDate workDate, LocalTime startTime,
                     LocalTime endTime, int breakMinutes, LeaveType leaveType,
                     String remarks, List<WorkTimeSegment> segments) {
            this(employeeId, workDate, startTime, endTime, breakMinutes, 1, leaveType, remarks, segments);
        }
    }

    public DailyAttendance execute(Input input) {
        DailyAttendance attendance = attendanceRepository.findByEmployeeAndDate(input.employeeId, input.workDate)
                .orElseGet(() -> {
                    String recordId = String.format("%s_%s", input.employeeId, input.workDate.toString());
                    return DailyAttendance.createHoliday(recordId, input.employeeId, input.workDate);
                });

        attendance.fillAttendance(
                input.startTime,
                input.endTime,
                input.breakMinutes,
                input.abstractId,
                input.leaveType,
                input.remarks,
                input.segments
        );

        attendanceRepository.save(attendance);
        return attendance;
    }
}
