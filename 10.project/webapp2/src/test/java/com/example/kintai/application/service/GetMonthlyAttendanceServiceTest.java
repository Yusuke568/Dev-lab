package com.example.kintai.application.service;

import com.example.kintai.application.port.in.GetMonthlyAttendanceUseCase;
import com.example.kintai.domain.model.attendance.AttendanceRecord;
import com.example.kintai.domain.model.employee.Employee;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.LoadAttendanceRecordPort;
import com.example.kintai.domain.port.out.LoadEmployeePort;
import com.example.kintai.adapter.in.web.dto.MonthlyAttendanceDto;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GetMonthlyAttendanceServiceTest {

    public static void main(String[] args) {
        testSuccessfulGetMonthlyAttendance();
        testEmployeeNotFound();
        System.out.println("All GetMonthlyAttendanceService tests passed.");
    }

    private static void testSuccessfulGetMonthlyAttendance() {
        // Arrange
        EmployeeId employeeId = new EmployeeId("1");
        YearMonth yearMonth = YearMonth.of(2026, 4);

        LoadEmployeePort loadEmployeePort = new LoadEmployeePort() {
            @Override
            public Optional<Employee> load(EmployeeId id) {
                if (id.equals(employeeId)) {
                    return Optional.of(new Employee(employeeId, "Test User"));
                }
                return Optional.empty();
            }
            @Override
            public List<Employee> loadAll() {
                return new ArrayList<>();
            }
        };

        LoadAttendanceRecordPort loadAttendanceRecordPort = new LoadAttendanceRecordPort() {
            @Override
            public List<AttendanceRecord> loadByEmployeeAndMonth(EmployeeId id, YearMonth ym) {
                List<AttendanceRecord> records = new ArrayList<>();
                // Mock adding one record
                records.add(new AttendanceRecord(id, LocalDate.of(2026, 4, 1), null));
                return records;
            }

            @Override
            public Optional<AttendanceRecord> loadByEmployeeAndDate(EmployeeId id, LocalDate date) {
                return Optional.empty();
            }
        };

        GetMonthlyAttendanceService service = new GetMonthlyAttendanceService(
                loadAttendanceRecordPort, loadEmployeePort);

        GetMonthlyAttendanceUseCase.GetMonthlyAttendanceCommand command =
                new GetMonthlyAttendanceUseCase.GetMonthlyAttendanceCommand(employeeId, yearMonth);

        // Act
        MonthlyAttendanceDto dto = service.getMonthlyAttendance(command);

        // Assert
        if (!dto.getEmployeeName().equals("Test User")) {
            throw new AssertionError("Employee name mismatch. Expected: Test User, Got: " + dto.getEmployeeName());
        }
        if (dto.getDailyRecords().size() != 1) {
            throw new AssertionError("Daily records size mismatch. Expected: 1, Got: " + dto.getDailyRecords().size());
        }
        
        System.out.println("testSuccessfulGetMonthlyAttendance passed.");
    }

    private static void testEmployeeNotFound() {
        // Arrange
        EmployeeId employeeId = new EmployeeId("999");
        YearMonth yearMonth = YearMonth.of(2026, 4);

        LoadEmployeePort loadEmployeePort = new LoadEmployeePort() {
            @Override
            public Optional<Employee> load(EmployeeId id) {
                return Optional.empty();
            }
            @Override
            public List<Employee> loadAll() {
                return new ArrayList<>();
            }
        };
        LoadAttendanceRecordPort loadAttendanceRecordPort = new LoadAttendanceRecordPort() {
            @Override
            public List<AttendanceRecord> loadByEmployeeAndMonth(EmployeeId id, YearMonth ym) {
                return new ArrayList<>();
            }
            @Override
            public Optional<AttendanceRecord> loadByEmployeeAndDate(EmployeeId id, LocalDate date) {
                return Optional.empty();
            }
        };

        GetMonthlyAttendanceService service = new GetMonthlyAttendanceService(
                loadAttendanceRecordPort, loadEmployeePort);

        GetMonthlyAttendanceUseCase.GetMonthlyAttendanceCommand command =
                new GetMonthlyAttendanceUseCase.GetMonthlyAttendanceCommand(employeeId, yearMonth);

        // Act & Assert
        try {
            service.getMonthlyAttendance(command);
            throw new AssertionError("Expected IllegalArgumentException was not thrown.");
        } catch (IllegalArgumentException e) {
            if (!e.getMessage().contains("Employee not found")) {
                throw new AssertionError("Unexpected exception message: " + e.getMessage());
            }
        }
        
        System.out.println("testEmployeeNotFound passed.");
    }
}
