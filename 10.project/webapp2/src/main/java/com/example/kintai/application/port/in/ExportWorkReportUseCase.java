package com.example.kintai.application.port.in;

import com.example.kintai.domain.model.employee.EmployeeId;

import java.time.YearMonth;

/**
 * 月次勤怠データを社内所定の「作業報告書」Excel（.xlsm）様式として出力するユースケース。
 */
public interface ExportWorkReportUseCase {

    /**
     * @param employeeId 対象の社員ID
     * @param yearMonth  対象の年月
     * @return 生成された .xlsm ファイルのバイト列
     */
    byte[] exportMonth(EmployeeId employeeId, YearMonth yearMonth);
}
