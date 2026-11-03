package usecase;

import domain.attendance.*;
import usecase.attendance.ApplyMonthlyTemplateUseCase;
import usecase.attendance.FillAttendanceUseCase;
import domain.contract.*;
import usecase.contract.CalculateSettlementUseCase;
import domain.weeklyreport.WeeklyReport;
import domain.weeklyreport.WeeklyReportRepository;
import usecase.weeklyreport.SubmitWeeklyReportUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SES勤怠管理ユースケース統合シナリオテスト")
public class SesUseCaseIntegrationTest {

    // インメモリーリポジトリ
    private static class InMemoryAttendanceRepository implements AttendanceRepository {
        private final Map<String, DailyAttendance> storage = new HashMap<>();
        private final Map<String, MonthlySummary> monthlyStorage = new HashMap<>();

        @Override
        public void save(DailyAttendance attendance) {
            storage.put(attendance.getEmployeeId() + "_" + attendance.getWorkDate(), attendance);
        }

        @Override
        public void saveAll(List<DailyAttendance> attendances) {
            for (DailyAttendance a : attendances) save(a);
        }

        @Override
        public Optional<DailyAttendance> findByEmployeeAndDate(String employeeId, LocalDate date) {
            return Optional.ofNullable(storage.get(employeeId + "_" + date));
        }

        @Override
        public List<DailyAttendance> findByEmployeeAndMonth(String employeeId, YearMonth yearMonth) {
            List<DailyAttendance> list = new ArrayList<>();
            for (DailyAttendance a : storage.values()) {
                if (a.getEmployeeId().equals(employeeId) && YearMonth.from(a.getWorkDate()).equals(yearMonth)) {
                    list.add(a);
                }
            }
            return list;
        }

        @Override
        public void saveMonthlySummary(MonthlySummary summary) {
            monthlyStorage.put(summary.getEmployeeId() + "_" + summary.getYearMonth(), summary);
        }

        @Override
        public Optional<MonthlySummary> findMonthlySummary(String employeeId, YearMonth yearMonth) {
            return Optional.ofNullable(monthlyStorage.get(employeeId + "_" + yearMonth));
        }

        @Override
        public List<AbstractItem> findAllAbstractItems() {
            return Collections.emptyList();
        }
    }

    private static class InMemoryContractRepository implements ContractRepository {
        private final ProjectContract contract;
        private final Assignment assignment;

        public InMemoryContractRepository(ProjectContract contract, Assignment assignment) {
            this.contract = contract;
            this.assignment = assignment;
        }

        @Override
        public Optional<ProjectContract> findContractById(String contractId) {
            return Optional.of(contract);
        }

        @Override
        public Optional<Assignment> findActiveAssignment(String employeeId) {
            return Optional.of(assignment);
        }
    }

    private static class InMemoryWeeklyReportRepository implements WeeklyReportRepository {
        private final Map<String, WeeklyReport> storage = new HashMap<>();

        @Override
        public void save(WeeklyReport report) {
            storage.put(report.getEmployeeId() + "_" + report.getYearWeek(), report);
        }

        @Override
        public Optional<WeeklyReport> findByEmployeeAndWeek(String employeeId, String yearWeek) {
            return Optional.ofNullable(storage.get(employeeId + "_" + yearWeek));
        }

        @Override
        public Optional<WeeklyReport> findById(String id) {
            return storage.values().stream().filter(r -> r.getId().equals(id)).findFirst();
        }

        @Override
        public Optional<WeeklyReport> findLatestBefore(String employeeId, String yearWeek) {
            return storage.values().stream()
                    .filter(r -> r.getEmployeeId().equals(employeeId) && r.getYearWeek().compareTo(yearWeek) < 0)
                    .max(Comparator.comparing(WeeklyReport::getYearWeek));
        }
    }

    private InMemoryAttendanceRepository attendanceRepo;
    private InMemoryContractRepository contractRepo;
    private InMemoryWeeklyReportRepository weeklyRepo;
    private ProjectContract standardContract;
    private Assignment standardAssignment;

    @BeforeEach
    void setUp() {
        attendanceRepo = new InMemoryAttendanceRepository();
        standardContract = new ProjectContract(
                "contract_ses_1", "某大手SIer案件", 140.0, 180.0,
                LocalTime.of(9, 0), LocalTime.of(18, 0), 60
        );
        standardAssignment = new Assignment(
                "asg_001", "emp_tanaka", "contract_ses_1",
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), 1.0
        );
        contractRepo = new InMemoryContractRepository(standardContract, standardAssignment);
        weeklyRepo = new InMemoryWeeklyReportRepository();
    }

    @Test
    @DisplayName("月初テンプレート展開 -> 事後入力 -> 未入力防止連動週報提出 -> 月次精算集計のE2Eシナリオ")
    void testEndToEndScenario() {
        AttendanceTemplateService templateService = new AttendanceTemplateService();
        ApplyMonthlyTemplateUseCase applyTemplateUseCase = new ApplyMonthlyTemplateUseCase(attendanceRepo, contractRepo, templateService);
        FillAttendanceUseCase fillAttendanceUseCase = new FillAttendanceUseCase(attendanceRepo);
        SubmitWeeklyReportUseCase submitWeeklyReportUseCase = new SubmitWeeklyReportUseCase(weeklyRepo, attendanceRepo, templateService, null);
        CalculateSettlementUseCase calculateSettlementUseCase = new CalculateSettlementUseCase(attendanceRepo, contractRepo, new SettlementService());

        // 1. 月初テンプレート自動展開（2026年10月の全平日が未入力状態で生成される）
        YearMonth ym = YearMonth.of(2026, 10);
        List<DailyAttendance> templates = applyTemplateUseCase.execute("emp_tanaka", ym);
        assertFalse(templates.isEmpty());

        // 2. 10月2日（金曜）時点で週報を提出しようとするが、10月1日・2日が未入力のため提出がブロックされる
        SubmitWeeklyReportUseCase.Input blockedReportInput = new SubmitWeeklyReportUseCase.Input(
                "rep_w40", "emp_tanaka", "2026-W40", LocalDate.of(2026, 10, 2),
                "未入力のまま週報提出", "次週予定", 3, 4, false
        );
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            submitWeeklyReportUseCase.execute(blockedReportInput);
        });
        assertTrue(ex.getMessage().contains("未入力の日次勤怠"));

        // 3. 10月1日と10月2日の勤怠を入力する
        fillAttendanceUseCase.execute(new FillAttendanceUseCase.Input(
                "emp_tanaka", LocalDate.of(2026, 10, 1),
                LocalTime.of(9, 0), LocalTime.of(18, 0), 60,
                LeaveType.NORMAL, "通常作業", null
        ));
        fillAttendanceUseCase.execute(new FillAttendanceUseCase.Input(
                "emp_tanaka", LocalDate.of(2026, 10, 2),
                LocalTime.of(9, 0), LocalTime.of(19, 0), 60, // 残業1h
                LeaveType.NORMAL, "リリース作業", null
        ));

        // 4. 未入力が解消されたので週報提出が成功する
        WeeklyReport submittedReport = submitWeeklyReportUseCase.execute(blockedReportInput);
        assertNotNull(submittedReport);
        assertEquals("2026-W40", submittedReport.getYearWeek());

        // 5. 月次稼働時間精算集計を実行する
        CalculateSettlementUseCase.Output output = calculateSettlementUseCase.execute("emp_tanaka", ym, 22, 22);
        assertNotNull(output.monthlySummary);
        assertNotNull(output.settlementResult);
        // 1日目(8h=480分) + 2日目(9h=540分) = 1020分 (17時間) 直接労働
        assertEquals(1020, output.monthlySummary.getDirectMinutes());
    }
}
