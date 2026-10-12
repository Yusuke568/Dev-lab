package usecase.attendance;

import domain.attendance.AttendanceRepository;
import domain.attendance.AttendanceTemplateService;
import domain.attendance.DailyAttendance;
import domain.contract.Assignment;
import domain.contract.ContractRepository;
import domain.contract.ProjectContract;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

/**
 * 月初勤務テンプレート一括展開ユースケース
 */
public class ApplyMonthlyTemplateUseCase {
    private final AttendanceRepository attendanceRepository;
    private final ContractRepository contractRepository;
    private final AttendanceTemplateService templateService;

    public ApplyMonthlyTemplateUseCase(AttendanceRepository attendanceRepository,
                                       ContractRepository contractRepository,
                                       AttendanceTemplateService templateService) {
        this.attendanceRepository = Objects.requireNonNull(attendanceRepository);
        this.contractRepository = Objects.requireNonNull(contractRepository);
        this.templateService = Objects.requireNonNull(templateService);
    }

    public List<DailyAttendance> execute(String employeeId, YearMonth yearMonth) {
        Assignment assignment = contractRepository.findActiveAssignment(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("有効なアサイン情報が見つかりません: " + employeeId));

        ProjectContract contract = contractRepository.findContractById(assignment.getProjectContractId())
                .orElseThrow(() -> new IllegalArgumentException("案件契約が見つかりません: " + assignment.getProjectContractId()));

        List<DailyAttendance> templates = templateService.generateMonthlyTemplate(employeeId, yearMonth, contract);
        attendanceRepository.saveAll(templates);
        return templates;
    }
}
