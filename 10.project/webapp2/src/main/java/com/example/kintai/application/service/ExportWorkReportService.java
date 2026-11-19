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

    /** 「所属：」欄の所属名称を調べる。null なら様式のまま */
    private com.example.kintai.domain.port.out.ReportDepartmentNamePort reportDepartmentNamePort;

    /** 作業報告書のファイル名の形。null なら既定の形 */
    private com.example.kintai.domain.port.out.WorkReportFileNamePort fileNamePort;

    public ExportWorkReportService withFileName(com.example.kintai.domain.port.out.WorkReportFileNamePort port) {
        this.fileNamePort = port;
        return this;
    }

    /** マスタで決めた形のファイル名（拡張子を除く）。形が読めなければ既定の形にする */
    private String fileName(EmployeeId employeeId, String employeeName, YearMonth yearMonth) {
        String pattern = null;
        if (fileNamePort != null) {
            try {
                pattern = fileNamePort.workReportFileNamePattern().orElse(null);
            } catch (RuntimeException e) {
                e.printStackTrace(); // マイグレーション前などで読めなくても出力は続ける
            }
        }
        return com.example.kintai.domain.service.WorkReportFileName.render(pattern, yearMonth, employeeId.getValue(), employeeName);
    }

    public ExportWorkReportService withDepartmentName(com.example.kintai.domain.port.out.ReportDepartmentNamePort port) {
        this.reportDepartmentNamePort = port;
        return this;
    }

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

        String departmentName = reportDepartmentNamePort == null ? null
                : reportDepartmentNamePort.findReportDepartmentName(employeeId);
        byte[] content;
        if (loadAttendanceRecordPort == null) {
            content = workReportExportPort.generate(monthlyData, monthlyData.getEmployeeName(), departmentName, workTypes,
                    weeklyWorkReports);
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
            // 開いたときは、出力した月のシートの今日を含む週を表示する
            content = workReportExportPort.generateYear(year, monthlyData.getEmployeeName(), departmentName, workTypes, months,
                    yearMonth);
        }

        String fileName = fileName(employeeId, monthlyData.getEmployeeName(), yearMonth);
        // サーバーの所定フォルダにも置く。置けなくてもダウンロードは止めない
        if (workReportArchivePort == null || !workReportArchivePort.isEnabled()) {
            return new ExportedWorkReport(content, null, null, fileName);
        }
        try {
            String path = workReportArchivePort.archive(employeeId.getValue(), monthlyData.getEmployeeName(), yearMonth, content,
                    fileName);
            return new ExportedWorkReport(content, path, null, fileName);
        } catch (Exception e) {
            // フォルダ名などを画面に出さないよう、詳しい内容はログに残す
            return new ExportedWorkReport(content, null, com.example.shared.error.UserMessages.of(e,
                    "書き込みに失敗しました。管理者に連絡してください"), fileName);
        }
    }
}
