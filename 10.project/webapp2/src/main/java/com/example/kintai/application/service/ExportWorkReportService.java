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

    public ExportWorkReportService(GetMonthlyAttendanceUseCase getMonthlyAttendanceUseCase,
                                    GetWorkTypesUseCase getWorkTypesUseCase,
                                    GetWeeklyWorkReportsUseCase getWeeklyWorkReportsUseCase,
                                    WorkReportExportPort workReportExportPort) {
        this.getMonthlyAttendanceUseCase = getMonthlyAttendanceUseCase;
        this.getWorkTypesUseCase = getWorkTypesUseCase;
        this.getWeeklyWorkReportsUseCase = getWeeklyWorkReportsUseCase;
        this.workReportExportPort = workReportExportPort;
    }

    @Override
    public byte[] exportMonth(EmployeeId employeeId, YearMonth yearMonth) {
        MonthlyAttendanceDto monthlyData = getMonthlyAttendanceUseCase.getMonthlyAttendance(
                new GetMonthlyAttendanceUseCase.GetMonthlyAttendanceCommand(employeeId, yearMonth));

        List<WorkType> workTypes = getWorkTypesUseCase.getWorkTypes();

        List<WeeklyWorkReport> weeklyWorkReports = getWeeklyWorkReportsUseCase.getWeeklyWorkReports(employeeId, yearMonth);

        return workReportExportPort.generate(monthlyData, monthlyData.getEmployeeName(), workTypes, weeklyWorkReports);
    }
}
