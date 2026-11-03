package domain.compliance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("36協定・有給休暇労務コンプライアンステスト")
public class Article36AndPaidLeaveTest {

    private Article36ComplianceService article36Service;
    private PaidLeaveComplianceService paidLeaveService;

    @BeforeEach
    void setUp() {
        article36Service = new Article36ComplianceService();
        paidLeaveService = new PaidLeaveComplianceService();
    }

    @Test
    @DisplayName("法定外残業36時間でイエローアラート（WARNING_35H）が発出されること")
    void test35HourWarning() {
        // 36時間 = 2160分
        OvertimeLevel level = article36Service.evaluateMonthlyOvertime(2160);
        assertEquals(OvertimeLevel.WARNING_35H, level);
    }

    @Test
    @DisplayName("法定外残業50時間で特別条項発動（SPECIAL_CLAUSE_ACTIVE）となること")
    void testSpecialClauseActive() {
        // 50時間 = 3000分
        OvertimeLevel level = article36Service.evaluateMonthlyOvertime(3000);
        assertEquals(OvertimeLevel.SPECIAL_CLAUSE_ACTIVE, level);
    }

    @Test
    @DisplayName("法定外残業100時間以上で違法警告（VIOLATION_100H）となること")
    void test100HourViolation() {
        // 100時間 = 6000分
        OvertimeLevel level = article36Service.evaluateMonthlyOvertime(6000);
        assertEquals(OvertimeLevel.VIOLATION_100H, level);
    }

    @Test
    @DisplayName("特別条項は年6回まで適用可能であること")
    void testSpecialClauseYearlyLimit() {
        assertTrue(article36Service.canApplySpecialClause(5));
        assertFalse(article36Service.canApplySpecialClause(6));
    }

    @Test
    @DisplayName("有給付与から半年経過で取得1日の場合アラートが出ること")
    void testPaidLeaveHalfYearAlert() {
        // 6ヶ月経過で1日消化 -> アラートあり
        String alert = paidLeaveService.checkPaidLeavePace(6, 1.0);
        assertNotNull(alert);
        assertTrue(alert.contains("半年が経過"));

        // 6ヶ月経過で2日消化 -> 正常（アラートなし）
        assertNull(paidLeaveService.checkPaidLeavePace(6, 2.0));
    }

    @Test
    @DisplayName("有給付与から9ヶ月経過で取得3日の場合アラートが出ること")
    void testPaidLeave9MonthsAlert() {
        // 9ヶ月経過で3日消化 -> アラートあり
        String alert = paidLeaveService.checkPaidLeavePace(9, 3.0);
        assertNotNull(alert);
        assertTrue(alert.contains("3ヶ月を切りました"));
    }
}
