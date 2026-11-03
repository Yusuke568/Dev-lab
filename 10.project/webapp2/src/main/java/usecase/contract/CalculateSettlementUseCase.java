package usecase.contract;

import domain.attendance.AttendanceRepository;
import domain.attendance.DailyAttendance;
import domain.attendance.MonthlySummary;
import domain.contract.*;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

/**
 * 月次稼働時間精算・集計ユースケース
 */
public class CalculateSettlementUseCase {
    private final AttendanceRepository attendanceRepository;
    private final ContractRepository contractRepository;
    private final SettlementService settlementService;

    public CalculateSettlementUseCase(AttendanceRepository attendanceRepository,
                                     ContractRepository contractRepository,
                                     SettlementService settlementService) {
        this.attendanceRepository = Objects.requireNonNull(attendanceRepository);
        this.contractRepository = Objects.requireNonNull(contractRepository);
        this.settlementService = Objects.requireNonNull(settlementService);
    }

    public static class Output {
        public final MonthlySummary monthlySummary;
        public final SettlementResult settlementResult;

        public Output(MonthlySummary monthlySummary, SettlementResult settlementResult) {
            this.monthlySummary = monthlySummary;
            this.settlementResult = settlementResult;
        }
    }

    public Output execute(String employeeId, YearMonth yearMonth, int totalBusinessDays, int attendedBusinessDays) {
        Assignment assignment = contractRepository.findActiveAssignment(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("アサイン情報が存在しません: " + employeeId));

        ProjectContract contract = contractRepository.findContractById(assignment.getProjectContractId())
                .orElseThrow(() -> new IllegalArgumentException("案件契約が存在しません: " + assignment.getProjectContractId()));

        List<DailyAttendance> attendances = attendanceRepository.findByEmployeeAndMonth(employeeId, yearMonth);

        // 入力済み（isFilled = true）の勤怠のみを実績集計対象とする
        int totalWorkMinutes = attendances.stream()
                .filter(DailyAttendance::isFilled)
                .mapToInt(DailyAttendance::calculateTotalWorkMinutes)
                .sum();
        int directMinutes = attendances.stream()
                .filter(DailyAttendance::isFilled)
                .mapToInt(DailyAttendance::getDirectMinutes)
                .sum();
        int indirectMinutes = attendances.stream()
                .filter(DailyAttendance::isFilled)
                .mapToInt(DailyAttendance::getIndirectMinutes)
                .sum();

        // 法定外残業（簡易計算: 1日480分超過の合計）
        int legalOvertimeMinutes = attendances.stream()
                .filter(DailyAttendance::isFilled)
                .mapToInt(a -> Math.max(0, a.calculateTotalWorkMinutes() - 480))
                .sum();

        // 精算計算
        SettlementResult result = settlementService.calculate(
                contract, totalBusinessDays, attendedBusinessDays, directMinutes, assignment.getAllocationRatio()
        );

        MonthlySummary summary = attendanceRepository.findMonthlySummary(employeeId, yearMonth)
                .orElseGet(() -> new MonthlySummary(
                        String.format("%s_%s", employeeId, yearMonth.toString()),
                        employeeId, yearMonth.toString(), assignment.getId()
                ));

        summary.updateSummaries(totalWorkMinutes, directMinutes, indirectMinutes, legalOvertimeMinutes, result.getStatus());
        attendanceRepository.saveMonthlySummary(summary);

        return new Output(summary, result);
    }
}
