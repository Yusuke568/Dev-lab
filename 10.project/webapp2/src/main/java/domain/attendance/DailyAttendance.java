package domain.attendance;

import domain.contract.ProjectContract;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 日次勤怠エンティティ（集約ルート）
 * （リアルタイム打刻を廃止し、事後申告・テンプレート展開・締めロックをカプセル化）
 */
public class DailyAttendance {
    private final String id;
    private final String employeeId;
    private final LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int breakMinutes;
    private int abstractId;
    private String abstractName;
    private LeaveType leaveType;
    private String remarks;
    private boolean isFilled;
    private boolean isLocked;
    private final List<WorkTimeSegment> segments;

    public DailyAttendance(String id, String employeeId, LocalDate workDate,
                           LocalTime startTime, LocalTime endTime, int breakMinutes,
                           int abstractId, LeaveType leaveType, String remarks, boolean isFilled,
                           boolean isLocked, List<WorkTimeSegment> segments) {
        this.id = Objects.requireNonNull(id, "IDは必須です。");
        this.employeeId = Objects.requireNonNull(employeeId, "社員IDは必須です。");
        this.workDate = Objects.requireNonNull(workDate, "勤務日は必須です。");
        this.startTime = startTime;
        this.endTime = endTime;
        this.breakMinutes = breakMinutes;
        this.abstractId = abstractId > 0 ? abstractId : 1;
        this.leaveType = leaveType != null ? leaveType : LeaveType.NORMAL;
        this.remarks = remarks;
        this.isFilled = isFilled;
        this.isLocked = isLocked;
        this.segments = new ArrayList<>(segments != null ? segments : Collections.emptyList());
    }

    public DailyAttendance(String id, String employeeId, LocalDate workDate,
                           LocalTime startTime, LocalTime endTime, int breakMinutes,
                           LeaveType leaveType, String remarks, boolean isFilled,
                           boolean isLocked, List<WorkTimeSegment> segments) {
        this(id, employeeId, workDate, startTime, endTime, breakMinutes, 1, leaveType, remarks, isFilled, isLocked, segments);
    }

    /**
     * テンプレートからの初期生成（未入力状態）
     */
    public static DailyAttendance createFromTemplate(String id, String employeeId, LocalDate workDate,
                                                      LocalTime defaultStart, LocalTime defaultEnd,
                                                      int defaultBreakMinutes) {
        DailyAttendance att = new DailyAttendance(
                id, employeeId, workDate, defaultStart, defaultEnd, defaultBreakMinutes,
                1, LeaveType.NORMAL, null, false, false, new ArrayList<>()
        );
        // デフォルトでは全実労働時間を「直接労働」セグメントとして仮割り当て
        int totalMinutes = att.calculateTotalWorkMinutes();
        att.segments.add(new WorkTimeSegment(
                id + "_seg_default", WorkCategory.DIRECT, totalMinutes, "通常客先作業"
        ));
        return att;
    }

    /**
     * 休日テンプレートの初期生成（未入力状態）
     */
    public static DailyAttendance createHoliday(String id, String employeeId, LocalDate workDate) {
        DailyAttendance att = new DailyAttendance(
                id, employeeId, workDate, null, null, 0,
                10, LeaveType.NORMAL, null, false, false, new ArrayList<>()
        );
        att.setAbstractName("休日");
        return att;
    }

    /**
     * 勤怠入力のクリア・リセット（未入力または休日に戻す）
     */
    public void reset(ProjectContract contract) {
        if (this.isLocked) {
            throw new IllegalStateException("月次確定によりロックされているため変更できません。");
        }
        boolean isWeekend = (workDate.getDayOfWeek() == java.time.DayOfWeek.SATURDAY
                || workDate.getDayOfWeek() == java.time.DayOfWeek.SUNDAY);
        this.remarks = null;
        this.isFilled = false;
        this.segments.clear();
        if (isWeekend) {
            this.startTime = null;
            this.endTime = null;
            this.breakMinutes = 0;
            this.abstractId = 10;
            this.abstractName = "休日";
            this.leaveType = LeaveType.NORMAL;
        } else {
            LocalTime st = contract != null ? contract.getDefaultStartTime() : LocalTime.of(9, 0);
            LocalTime et = contract != null ? contract.getDefaultEndTime() : LocalTime.of(18, 0);
            int brk = contract != null ? contract.getDefaultBreakMinutes() : 60;
            this.startTime = st;
            this.endTime = et;
            this.breakMinutes = brk;
            this.abstractId = 1;
            this.abstractName = "出勤";
            this.leaveType = LeaveType.NORMAL;
            this.segments.add(new WorkTimeSegment(
                    this.id + "_seg_default", WorkCategory.DIRECT, calculateTotalWorkMinutes(), "通常客先作業"
            ));
        }
    }

    /**
     * 社員による事後差分入力（更新）
     */
    public void fillAttendance(LocalTime startTime, LocalTime endTime, int breakMinutes,
                               int abstractId, LeaveType leaveType, String remarks, List<WorkTimeSegment> newSegments) {
        if (this.isLocked) {
            throw new IllegalStateException("月次確定によりロックされているため変更できません。");
        }
        this.startTime = startTime;
        this.endTime = endTime;
        this.breakMinutes = breakMinutes;
        this.abstractId = abstractId > 0 ? abstractId : 1;
        this.leaveType = leaveType != null ? leaveType : LeaveType.NORMAL;
        this.remarks = remarks;
        this.isFilled = true;

        this.segments.clear();
        if (newSegments != null && !newSegments.isEmpty()) {
            this.segments.addAll(newSegments);
        } else {
            // セグメント指定なしの場合は全実働時間を直接労働として記録
            int total = calculateTotalWorkMinutes();
            this.segments.add(new WorkTimeSegment(
                    this.id + "_seg_1", WorkCategory.DIRECT, total, "客先作業"
            ));
        }

        validateSegmentsIntegrity();
    }

    public void fillAttendance(LocalTime startTime, LocalTime endTime, int breakMinutes,
                               LeaveType leaveType, String remarks, List<WorkTimeSegment> newSegments) {
        fillAttendance(startTime, endTime, breakMinutes, 1, leaveType, remarks, newSegments);
    }

    /**
     * 実労働時間の計算（拘束時間 - 休憩時間）
     */
    public int calculateTotalWorkMinutes() {
        if (leaveType == LeaveType.PAID_LEAVE || leaveType == LeaveType.ABSENCE) {
            return 0; // 全休時は実働0分
        }
        if (startTime == null || endTime == null) {
            return 0;
        }
        long diff = Duration.between(startTime, endTime).toMinutes();
        if (diff < 0) {
            diff += 24 * 60; // 翌日またぎ
        }
        long actual = diff - breakMinutes;
        return (int) Math.max(0, actual);
    }

    /**
     * 直接労働時間（客先精算対象）の合計（分）
     */
    public int getDirectMinutes() {
        return segments.stream()
                .filter(s -> s.getCategory() == WorkCategory.DIRECT)
                .mapToInt(WorkTimeSegment::getDurationMinutes)
                .sum();
    }

    /**
     * 間接労働時間（自社内研修・MTG等）の合計（分）
     */
    public int getIndirectMinutes() {
        return segments.stream()
                .filter(s -> s.getCategory() == WorkCategory.INDIRECT)
                .mapToInt(WorkTimeSegment::getDurationMinutes)
                .sum();
    }

    /**
     * 実労働時間とセグメント合計の整合性チェック
     */
    private void validateSegmentsIntegrity() {
        int total = calculateTotalWorkMinutes();
        int segmentsTotal = getDirectMinutes() + getIndirectMinutes();
        if (total != segmentsTotal) {
            throw new IllegalArgumentException(
                    String.format("実労働時間(%d分)と内訳セグメント合計(%d分)が一致しません。", total, segmentsTotal)
            );
        }
    }

    public void lock() { this.isLocked = true; }
    public void unlock() { this.isLocked = false; }

    public String getId() { return id; }
    public String getEmployeeId() { return employeeId; }
    public LocalDate getWorkDate() { return workDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public int getBreakMinutes() { return breakMinutes; }
    public int getAbstractId() { return abstractId; }
    public String getAbstractName() { return abstractName; }
    public void setAbstractName(String abstractName) { this.abstractName = abstractName; }
    public LeaveType getLeaveType() { return leaveType; }
    public String getRemarks() { return remarks; }
    public boolean isFilled() { return isFilled; }
    public boolean isLocked() { return isLocked; }
    public List<WorkTimeSegment> getSegments() { return Collections.unmodifiableList(segments); }
}
