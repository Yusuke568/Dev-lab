package com.example.kintai.domain.port.out;

import com.example.kintai.domain.model.attendance.OvertimeRequest;
import com.example.kintai.domain.model.employee.EmployeeId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 残業事前申請の永続化操作を定義するポート。
 */
public interface OvertimeRequestPort {

    Optional<OvertimeRequest> findByEmployeeAndDate(EmployeeId employeeId, LocalDate date);

    List<OvertimeRequest> findByEmployee(EmployeeId employeeId);

    List<OvertimeRequest> findPending();

    void insert(EmployeeId employeeId, LocalDate date, int plannedOvertimeMinutes, String plannedStartTime, String plannedEndTime, String reason);

    void resubmit(int requestId, int plannedOvertimeMinutes, String plannedStartTime, String plannedEndTime, String reason);

    void decide(int requestId, boolean approve, String decidedBy);
}
