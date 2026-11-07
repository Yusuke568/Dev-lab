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
import java.util.Collections;
import java.util.List;

/**
 * 作業報告書Excelからの一括取り込みユースケースの実装。
 *
 * パース結果は既存のUpdateAttendanceUseCase・SaveWeeklyWorkReportsUseCaseに変換して渡すことで、
 * 手入力・API経由の保存と同じ検証・計算ロジック（時間外再計算、承認要否判定等）を適用する。
 *
 * 1日ごとに個別のトランザクションとして保存することで、過去の実績データにありがちな
 * 1件の不整合（日またぎの退勤時刻等）が月全体・ファイル全体の取り込みを止めてしまわないようにする。
 * 失敗した日・週はスキップして理由を記録し、残りの取り込みを継続する。
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
    public ImportResult importFile(EmployeeId employeeId, InputStream xlsmInputStream) {
        List<ImportedMonthData> months = workReportImportPort.parse(xlsmInputStream);
        if (months.isEmpty()) {
            throw new IllegalArgumentException("取り込み可能な年月のデータが見つかりませんでした。作業報告書の様式をご確認ください。");
        }

        List<WorkType> workTypes = getWorkTypesUseCase.getWorkTypes();

        List<String> skipped = new ArrayList<>();
        int importedMonthCount = 0;
        int savedDays = 0;
        int savedWeeks = 0;
        List<String> monthLabels = new ArrayList<>();

        for (ImportedMonthData month : months) {
            boolean monthHadData = !month.getDays().isEmpty() || !month.getWeeklyWorkReports().isEmpty();
            boolean monthHadFailure = false;
            if (monthHadData) {
                monthLabels.add(month.getYearMonth().toString());
            }

            for (ImportedDayData day : month.getDays()) {
                // 摘要欄の値を勤務区分に読み替える（有休 → 有給、代休 → 代休 など）。
                // 摘要が空欄の日は、時刻があれば出勤、時刻もなければ未設定（null）
                boolean blankCategory = day.getCategory() == null || day.getCategory().isBlank();
                boolean hasTime = hasText(day.getStartTime()) || hasText(day.getEndTime());
                Integer abstractId = blankCategory && !hasTime
                        ? null
                        : com.example.kintai.domain.service.WorkReportCategoryMapper.toWorkTypeId(day.getCategory(), workTypes);
                AttendanceUpdateCommand command = new AttendanceUpdateCommand(
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
                );
                try {
                    updateAttendanceUseCase.updateAttendance(Collections.singletonList(command));
                    savedDays++;
                } catch (Exception e) {
                    monthHadFailure = true;
                    skipped.add(day.getDate() + ": " + rootMessage(e));
                }
            }

            for (WeeklyWorkReport parsed : month.getWeeklyWorkReports()) {
                WeeklyWorkReport report = new WeeklyWorkReport(
                        employeeId.getValue(), parsed.getWeekStartDate(), parsed.getWeekEndDate(),
                        parsed.getClientName(), parsed.getProjectName(), parsed.getProcessName(),
                        parsed.getProgressStatus(), parsed.getProgressDetail(), parsed.getPersonalNote(),
                        parsed.getProblemCountermeasure()
                ).replacingLeaderComment(parsed.getLeaderComment());
                try {
                    saveWeeklyWorkReportsUseCase.saveWeeklyWorkReports(employeeId, Collections.singletonList(report));
                    savedWeeks++;
                } catch (Exception e) {
                    monthHadFailure = true;
                    skipped.add(month.getYearMonth() + "週(" + report.getWeekLabel() + "): " + rootMessage(e));
                }
            }

            if (monthHadData && !monthHadFailure) {
                importedMonthCount++;
            } else if (monthHadData) {
                importedMonthCount++; // 一部失敗があっても、成功した日・週があれば取り込み月として数える
            }
        }

        return new ImportResult(importedMonthCount, skipped, savedDays, savedWeeks, monthLabels);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String rootMessage(Throwable e) {
        Throwable current = e;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return (message == null || message.isBlank()) ? current.getClass().getSimpleName() : message;
    }
}
