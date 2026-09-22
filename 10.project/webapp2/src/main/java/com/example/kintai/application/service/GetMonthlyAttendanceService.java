package com.example.kintai.application.service;

import com.example.kintai.adapter.in.web.dto.DailyAttendanceDto;
import com.example.kintai.adapter.in.web.dto.MonthlyAttendanceDto;
import com.example.kintai.application.port.in.GetMonthlyAttendanceUseCase;
import com.example.kintai.domain.model.attendance.AttendanceRecord;
import com.example.kintai.domain.model.employee.Employee;
import com.example.kintai.domain.port.out.LoadAttendanceRecordPort;
import com.example.kintai.domain.port.out.LoadEmployeePort;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 月次勤怠惁E��取得ユースケースの実裁E��ラス、E
 */
public class GetMonthlyAttendanceService implements GetMonthlyAttendanceUseCase {

    private final LoadAttendanceRecordPort loadAttendanceRecordPort;
    private final LoadEmployeePort loadEmployeePort;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 依存性をコンストラクタ経由で注入します！EI�E�、E
     *
     * @param loadAttendanceRecordPort 勤怠記録をロードするため�Eポ�EチE
     * @param loadEmployeePort 社員惁E��をロードするため�Eポ�EチE
     */
    public GetMonthlyAttendanceService(LoadAttendanceRecordPort loadAttendanceRecordPort, LoadEmployeePort loadEmployeePort) {
        this.loadAttendanceRecordPort = loadAttendanceRecordPort;
        this.loadEmployeePort = loadEmployeePort;
    }

    @Override
    public MonthlyAttendanceDto getMonthlyAttendance(GetMonthlyAttendanceCommand command) {
        // 1. ポ�Eトを通じて永続化層からドメインオブジェクトを取征E
        List<AttendanceRecord> records = loadAttendanceRecordPort.loadByEmployeeAndMonth(
                command.getEmployeeId(), command.getYearMonth());
        
        Employee employee = loadEmployeePort.load(command.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));

        // 2. ドメインオブジェクトをDTOに変換
        List<DailyAttendanceDto> dailyDtos = records.stream()
                .map(this::toDailyDto)
                .collect(Collectors.toList());

        // 3. サマリー惁Eを計箁E
        Duration totalDuration = records.stream()
                .map(AttendanceRecord::calculateWorkDuration)
                .reduce(Duration.ZERO, Duration::plus);
        String totalWorkHours = formatDuration(totalDuration);
        long totalWorkMinutes = totalDuration.toMinutes();

        // 時間外（残業）の合計は、日次一覧に表示する値（record.getOvertimeMinutes()）と必ず一致させる。
        // 画面上の「時間外」列の合計と、上部サマリー・労働基準法チェックの数値がずれないようにするため。
        long totalOvertimeMinutes = records.stream()
                .mapToLong(AttendanceRecord::getOvertimeMinutes)
                .sum();
        long annualOvertimeMinutes = loadAttendanceRecordPort.sumOvertimeMinutesByEmployeeAndYear(
                command.getEmployeeId(), command.getYearMonth().getYear());

        // 4. 最終的なDTOを絁E立てて返す
        return new MonthlyAttendanceDto(command.getYearMonth(), dailyDtos, totalWorkHours, totalWorkMinutes,
                totalOvertimeMinutes, annualOvertimeMinutes, employee.getName());
        }


    /**
     * AttendanceRecord (ドメインモチE��) めEDailyAttendanceDto に変換します、E
     */
    private DailyAttendanceDto toDailyDto(AttendanceRecord record) {
        String startTime = record.getWorkTime() != null ? record.getWorkTime().getStartTime().format(TIME_FORMATTER) : "-";
        String endTime = record.getWorkTime() != null ? record.getWorkTime().getEndTime().format(TIME_FORMATTER) : "-";
        String workHours = formatDuration(record.calculateWorkDuration());
        
        return new DailyAttendanceDto(
                record.getWorkDate(),
                startTime,
                endTime,
                workHours,
                record.getWorkDescription(),
                record.getAbstractId(),
                record.getCorrectionId(),
                record.getCorrectionUsTime(),
                record.getCorrectionMidTime(),
                record.getApprovalStatus(),
                record.getOvertimeMinutes()
        );
    }

    /**
     * DurationめE"HH:mm" 形式�E斁E���Eにフォーマットします、E
     */
    private String formatDuration(Duration duration) {
        if (duration == null || duration.isZero()) {
            return "0:00";
        }
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%d:%02d", hours, minutes);
    }
}
