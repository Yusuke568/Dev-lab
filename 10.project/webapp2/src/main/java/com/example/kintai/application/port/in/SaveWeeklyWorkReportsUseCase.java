package com.example.kintai.application.port.in;

import com.example.kintai.domain.model.attendance.WeeklyWorkReport;
import com.example.kintai.domain.model.employee.EmployeeId;

import java.util.List;

/**
 * 週次作業報告をまとめて保存（新規作成・更新）するユースケース。
 */
public interface SaveWeeklyWorkReportsUseCase {

    void saveWeeklyWorkReports(EmployeeId employeeId, List<WeeklyWorkReport> reports);
}
