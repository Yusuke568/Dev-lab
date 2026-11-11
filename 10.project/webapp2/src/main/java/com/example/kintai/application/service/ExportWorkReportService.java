package com.example.kintai.application.service;

import com.example.application.port.in.GetWorkTypesUseCase;
import com.example.entity.WorkType;
import com.example.kintai.adapter.in.web.dto.MonthlyAttendanceDto;
import com.example.kintai.application.port.in.ExportWorkReportUseCase;
import com.example.kintai.application.port.in.GetMonthlyAttendanceUseCase;
import com.example.kintai.application.port.in.GetWeeklyWorkReportsUseCase;
import com.example.kintai.domain.model.attendance.WeeklyWorkReport;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.WorkReportExportPort;

import java.time.YearMonth;
import java.util.List;

/**
 * 月次勤怠データを社内所定の「作業報告書」Excel（.xlsm）様式として出力するユースケースの実装。
 */
public class ExportWorkReportService implements ExportWorkReportUseCase {

    private final GetMonthlyAttendanceUseCase getMonthlyAttendanceUseCase;
    private final GetWorkTypesUseCase getWorkTypesUseCase;
    private final GetWeeklyWorkReportsUseCase getWeeklyWorkReportsUseCase;
    private final WorkReportExportPort workReportExportPort;
    private final com.example.kintai.domain.port.out.WorkReportArchivePort workReportArchivePort;
    /** データのある月を調べる（読むだけ）。null なら表示中の月だけを出力する */
    private com.example.kintai.domain.port.out.LoadAttendanceRecordPort loadAttendanceRecordPort;

    public ExportWorkReportService withAttendanceCheck(com.example.kintai.domain.port.out.LoadAttendanceRecordPort port) {
        this.loadAttendanceRecordPort = port;
        return this;
    }

    public ExportWorkReportService(GetMonthlyAttendanceUseCase getMonthlyAttendanceUseCase,
                                    GetWorkTypesUseCase getWorkTypesUseCase,
                                    GetWeeklyWorkReportsUseCase getWeeklyWorkReportsUseCase,
                                    WorkReportExportPort workReportExportPort,
                                    com.example.kintai.domain.port.out.WorkReportArchivePort workReportArchivePort) {
        this.workReportArchivePort = workReportArchivePort;
        this.getMonthlyAttendanceUseCase = getMonthlyAttendanceUseCase;
        this.getWorkTypesUseCase = getWorkTypesUseCase;
        this.getWeeklyWorkReportsUseCase = getWeeklyWorkReportsUseCase;
        this.workReportExportPort = workReportExportPort;
    }

    @Override
    public ExportedWorkReport exportMonth(EmployeeId employeeId, YearMonth yearMonth) {
        MonthlyAttendanceDto monthlyData = getMonthlyAttendanceUseCase.getMonthlyAttendance(
                new GetMonthlyAttendanceUseCase.GetMonthlyAttendanceCommand(employeeId, yearMonth));

        List<WorkType> workTypes = getWorkTypesUseCase.getWorkTypes();

        List<WeeklyWorkReport> weeklyWorkReports = getWeeklyWorkReportsUseCase.getWeeklyWorkReports(employeeId, yearMonth);

        byte[] content;
        if (loadAttendanceRecordPort == null) {
            content = workReportExportPort.generate(monthlyData, monthlyData.getEmployeeName(), workTypes, weeklyWorkReports);
        } else {
            // 前年12月〜当年12月のうち、勤怠のある月（と表示中の月）をまとめて1ファイルにする
            int year = yearMonth.getYear();
            List<com.example.kintai.domain.port.out.WorkReportExportPort.MonthData> months = new java.util.ArrayList<>();
            for (int m = 0; m <= 12; m++) {
                YearMonth ym = m == 0 ? YearMonth.of(year - 1, 12) : YearMonth.of(year, m);
                if (ym.equals(yearMonth)) {
                    months.add(new com.example.kintai.domain.port.out.WorkReportExportPort.MonthData(monthlyData, weeklyWorkReports));
                } else if (loadAttendanceRecordPort.hasAnyRecord(employeeId, ym)) {
                    MonthlyAttendanceDto other = getMonthlyAttendanceUseCase.getMonthlyAttendance(
                            new GetMonthlyAttendanceUseCase.GetMonthlyAttendanceCommand(employeeId, ym));
                    months.add(new com.example.kintai.domain.port.out.WorkReportExportPort.MonthData(other,
                            getWeeklyWorkReportsUseCase.getWeeklyWorkReports(employeeId, ym)));
                }
            }
            content = workReportExportPort.generateYear(year, monthlyData.getEmployeeName(), workTypes, months);
        }

        // サーバーの所定フォルダにも置く。置けなくてもダウンロードは止めない
        if (workReportArchivePort == null || !workReportArchivePort.isEnabled()) {
            return new ExportedWorkReport(content, null, null);
        }
        try {
            String path = workReportArchivePort.archive(employeeId.getValue(), monthlyData.getEmployeeName(), yearMonth, content);
            return new ExportedWorkReport(content, path, null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ExportedWorkReport(content, null, e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }
}
