package com.example.kintai.domain.port.out;

import com.example.kintai.domain.model.employee.EmployeeId;

/**
 * 作業報告書の「所属：」欄に入れる所属名称を調べるポート。
 */
public interface ReportDepartmentNamePort {

    /** 社員の主所属の部署の所属名称（空なら部署名）。部署がなければ null */
    String findReportDepartmentName(EmployeeId employeeId);
}
