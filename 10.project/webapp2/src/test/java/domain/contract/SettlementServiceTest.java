package domain.contract;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("稼働時間・精算幅計算ドメインサービステスト")
public class SettlementServiceTest {

    private SettlementService settlementService;
    private ProjectContract standardContract;

    @BeforeEach
    void setUp() {
        settlementService = new SettlementService();
        // 基準: 140h〜180h, 9:00-18:00 (休憩60分)
        standardContract = new ProjectContract(
                "contract_001", "テスト株式会社", 140.0, 180.0,
                LocalTime.of(9, 0), LocalTime.of(18, 0), 60
        );
    }

    @Test
    @DisplayName("基準時間内（160時間稼働）のときWITHIN_RANGEと判定されること")
    void testWithinRange() {
        // 160時間 = 9600分
        SettlementResult result = settlementService.calculate(standardContract, 20, 20, 9600, 1.0);
        assertEquals(SettlementStatus.WITHIN_RANGE, result.getStatus());
        assertEquals(0.0, result.getDiffHours());
        assertEquals(140.0, result.getAdjustedLowerHours());
        assertEquals(180.0, result.getAdjustedUpperHours());
    }

    @Test
    @DisplayName("下限割れ（130時間稼働）のときSHORTAGEと判定され不足時間10時間が算出されること")
    void testShortage() {
        // 130時間 = 7800分
        SettlementResult result = settlementService.calculate(standardContract, 20, 20, 7800, 1.0);
        assertEquals(SettlementStatus.SHORTAGE, result.getStatus());
        assertEquals(10.0, result.getDiffHours());
    }

    @Test
    @DisplayName("上限超過（195時間稼働）のときOVERTIMEと判定され超過時間15時間が算出されること")
    void testOvertime() {
        // 195時間 = 11700分
        SettlementResult result = settlementService.calculate(standardContract, 20, 20, 11700, 1.0);
        assertEquals(SettlementStatus.OVERTIME, result.getStatus());
        assertEquals(15.0, result.getDiffHours());
    }

    @Test
    @DisplayName("月中入場（20営業日中10日参画）のとき精算幅が70h〜90hに按分されること")
    void testMidMonthAdjustment() {
        // 10日参画、80時間(4800分)稼働 -> 按分後 70h〜90h のため基準内
        SettlementResult result = settlementService.calculate(standardContract, 20, 10, 4800, 1.0);
        assertEquals(70.0, result.getAdjustedLowerHours());
        assertEquals(90.0, result.getAdjustedUpperHours());
        assertEquals(SettlementStatus.WITHIN_RANGE, result.getStatus());
    }

    @Test
    @DisplayName("月末着地予測で前半ハイペース（10営業日消化で110時間）のときOVERTIME警告となること")
    void testMonthEndPredictionOvertime() {
        // 経過10日/残10日、現在110h(6600分)稼働 -> ペース11h/日 -> 着地予測220h
        SettlementResult result = settlementService.predictMonthEnd(standardContract, 10, 10, 6600, 8.0);
        assertEquals(SettlementStatus.OVERTIME, result.getStatus());
        assertTrue(result.getActualDirectHours() > 180.0);
    }
}
