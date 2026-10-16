package com.example.kintai.application.port.in;

import com.example.kintai.domain.model.employee.EmployeeId;

import java.io.InputStream;

/**
 * 社内所定の「作業報告書」Excel（.xlsm）を読み込み、勤怠データ・週次作業報告を一括登録するユースケース。
 */
public interface ImportWorkReportUseCase {

    /**
     * @return 取り込んだ月数（登録したシート数）
     */
    int importFile(EmployeeId employeeId, InputStream xlsmInputStream);
}
