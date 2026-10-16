package com.example.kintai.application.port.in;

import com.example.kintai.domain.model.attendance.WeeklyWorkReport;
import com.example.kintai.domain.model.employee.EmployeeId;

import java.time.YearMonth;
import java.util.List;

/**
 * 指定月に対応する5週分の週次作業報告を取得するユースケース。
 * 未保存の週も含めて、月の週ブロック構成に従った5件を返す（未保存分は入力値がすべてnullの空データ）。
 */
public interface GetWeeklyWorkReportsUseCase {

    List<WeeklyWorkReport> getWeeklyWorkReports(EmployeeId employeeId, YearMonth yearMonth);
}
