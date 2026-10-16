package com.example.kintai.application.service;

import com.example.application.port.in.GetWorkTypesUseCase;
import com.example.entity.WorkType;
import com.example.kintai.application.port.in.ImportWorkReportUseCase;
import com.example.kintai.application.port.in.SaveWeeklyWorkReportsUseCase;
import com.example.kintai.application.port.in.UpdateAttendanceUseCase;
import com.example.kintai.application.port.in.UpdateAttendanceUseCase.AttendanceUpdateCommand;
import com.example.kintai.domain.model.attendance.ImportedDayData;
import com.example.kintai.domain.model.attendance.ImportedMonthData;
import com.example.kintai.domain.model.attendance.WeeklyWorkReport;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.WorkReportImportPort;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 作業報告書Excelからの一括取り込みユースケースの実装。
 *
 * パース結果は既存のUpdateAttendanceUseCase・SaveWeeklyWorkReportsUseCaseに変換して渡すことで、
 * 手入力・API経由の保存と同じ検証・計算ロジック（時間外再計算、承認要否判定等）を適用する。
 */
public class ImportWorkReportService implements ImportWorkReportUseCase {

    private final WorkReportImportPort workReportImportPort;
    private final UpdateAttendanceUseCase updateAttendanceUseCase;
    private final SaveWeeklyWorkReportsUseCase saveWeeklyWorkReportsUseCase;
    private final GetWorkTypesUseCase getWorkTypesUseCase;

    public ImportWorkReportService(WorkReportImportPort workReportImportPort,
                                    UpdateAttendanceUseCase updateAttendanceUseCase,
                                    SaveWeeklyWorkReportsUseCase saveWeeklyWorkReportsUseCase,
                                    GetWorkTypesUseCase getWorkTypesUseCase) {
        this.workReportImportPort = workReportImportPort;
        this.updateAttendanceUseCase = updateAttendanceUseCase;
        this.saveWeeklyWorkReportsUseCase = saveWeeklyWorkReportsUseCase;
        this.getWorkTypesUseCase = getWorkTypesUseCase;
    }

    @Override
    public int importFile(EmployeeId employeeId, InputStream xlsmInputStream) {
        List<ImportedMonthData> months = workReportImportPort.parse(xlsmInputStream);
        if (months.isEmpty()) {
            throw new IllegalArgumentException("取り込み可能な年月のデータが見つかりませんでした。作業報告書の様式をご確認ください。");
        }

        List<WorkType> workTypes = getWorkTypesUseCase.getWorkTypes();
        Integer paidLeaveId = workTypes.stream()
                .filter(wt -> "有給".equals(wt.getName()) || wt.isPaid())
                .map(WorkType::getId)
                .findFirst()
                .orElse(null);
        Integer defaultWorkTypeId = workTypes.stream()
                .filter(wt -> "出勤".equals(wt.getName()))
                .map(WorkType::getId)
                .findFirst()
                .orElse(null);

        for (ImportedMonthData month : months) {
            List<AttendanceUpdateCommand> commands = new ArrayList<>();
            for (ImportedDayData day : month.getDays()) {
                Integer abstractId = "有休".equals(day.getCategory()) ? paidLeaveId : defaultWorkTypeId;
                commands.add(new AttendanceUpdateCommand(
                        employeeId.getValue(),
                        day.getDate(),
                        day.getDate().getDayOfWeek().toString().substring(0, 3),
                        day.getStartTime(),
                        day.getEndTime(),
                        0, // 時間外分は保存時に再計算されるため未使用
                        abstractId,
                        day.getMemo(),
                        null, // 補正CDマスタとの紐付けは行わず時間のみ取り込む
                        day.getCorrectionUsTime(),
                        day.getCorrectionMidTime(),
                        day.getIndirectTime(),
                        null,
                        null,
                        false
                ));
            }
            if (!commands.isEmpty()) {
                updateAttendanceUseCase.updateAttendance(commands);
            }

            List<WeeklyWorkReport> weeklyReports = new ArrayList<>();
            for (WeeklyWorkReport parsed : month.getWeeklyWorkReports()) {
                weeklyReports.add(new WeeklyWorkReport(
                        employeeId.getValue(), parsed.getWeekStartDate(), parsed.getWeekEndDate(),
                        parsed.getClientName(), parsed.getProjectName(), parsed.getProcessName(),
                        parsed.getProgressStatus(), parsed.getProgressDetail(), parsed.getPersonalNote()
                ));
            }
            if (!weeklyReports.isEmpty()) {
                saveWeeklyWorkReportsUseCase.saveWeeklyWorkReports(employeeId, weeklyReports);
            }
        }

        return months.size();
    }
}
