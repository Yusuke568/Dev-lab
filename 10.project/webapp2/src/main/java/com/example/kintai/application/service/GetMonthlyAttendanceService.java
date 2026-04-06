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
 * 月次勤怠情報取得ユースケースの実装クラス。
 */
public class GetMonthlyAttendanceService implements GetMonthlyAttendanceUseCase {

    private final LoadAttendanceRecordPort loadAttendanceRecordPort;
    private final LoadEmployeePort loadEmployeePort;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 依存性をコンストラクタ経由で注入します（DI）。
     *
     * @param loadAttendanceRecordPort 勤怠記録をロードするためのポート
     * @param loadEmployeePort 社員情報をロードするためのポート
     */
    public GetMonthlyAttendanceService(LoadAttendanceRecordPort loadAttendanceRecordPort, LoadEmployeePort loadEmployeePort) {
        this.loadAttendanceRecordPort = loadAttendanceRecordPort;
        this.loadEmployeePort = loadEmployeePort;
    }

    @Override
    public MonthlyAttendanceDto getMonthlyAttendance(GetMonthlyAttendanceCommand command) {
        // 1. ポートを通じて永続化層からドメインオブジェクトを取得
        List<AttendanceRecord> records = loadAttendanceRecordPort.loadByEmployeeAndMonth(
                command.getEmployeeId(), command.getYearMonth());
        
        Employee employee = loadEmployeePort.load(command.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));

        // 2. ドメインオブジェクトをDTOに変換
        List<DailyAttendanceDto> dailyDtos = records.stream()
                .map(this::toDailyDto)
                .collect(Collectors.toList());

        // 3. サマリー情報を計算
        Duration totalDuration = records.stream()
                .map(AttendanceRecord::calculateWorkDuration)
                .reduce(Duration.ZERO, Duration::plus);
        String totalWorkHours = formatDuration(totalDuration);

        // 4. 最終的なDTOを組み立てて返す
        return new MonthlyAttendanceDto(command.getYearMonth(), dailyDtos, totalWorkHours, employee.getName());
    }

    /**
     * AttendanceRecord (ドメインモデル) を DailyAttendanceDto に変換します。
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
                record.getWorkDescription()
        );
    }

    /**
     * Durationを "HH:mm" 形式の文字列にフォーマットします。
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
