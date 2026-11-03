package domain.attendance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("日次勤怠エンティティ・集計テスト")
public class DailyAttendanceTest {

    @Test
    @DisplayName("テンプレートから生成された勤怠は未入力状態かつ直接労働が初期セットされること")
    void testTemplateCreation() {
        DailyAttendance att = DailyAttendance.createFromTemplate(
                "att_001", "emp_001", LocalDate.of(2026, 10, 1),
                LocalTime.of(9, 0), LocalTime.of(18, 0), 60
        );

        assertFalse(att.isFilled());
        assertFalse(att.isLocked());
        assertEquals(480, att.calculateTotalWorkMinutes()); // 8時間 = 480分
        assertEquals(480, att.getDirectMinutes());
        assertEquals(0, att.getIndirectMinutes());
    }

    @Test
    @DisplayName("事後入力で直接7時間・社内MTG1時間に分割入力できること")
    void testFillAttendanceWithSegments() {
        DailyAttendance att = DailyAttendance.createFromTemplate(
                "att_001", "emp_001", LocalDate.of(2026, 10, 1),
                LocalTime.of(9, 0), LocalTime.of(18, 0), 60
        );

        // 9:00〜18:00 (実働8h=480分) のうち、直接7h(420分)、間接1h(60分)
        att.fillAttendance(
                LocalTime.of(9, 0), LocalTime.of(18, 0), 60,
                LeaveType.NORMAL, "通常作業と社内定例",
                Arrays.asList(
                        new WorkTimeSegment("seg_1", WorkCategory.DIRECT, 420, "客先機能開発"),
                        new WorkTimeSegment("seg_2", WorkCategory.INDIRECT, 60, "社内帰社日MTG")
                )
        );

        assertTrue(att.isFilled());
        assertEquals(480, att.calculateTotalWorkMinutes());
        assertEquals(420, att.getDirectMinutes());
        assertEquals(60, att.getIndirectMinutes());
    }

    @Test
    @DisplayName("実労働時間とセグメント合計が合わない場合はエラーとなること")
    void testInvalidSegmentSumThrowsException() {
        DailyAttendance att = DailyAttendance.createFromTemplate(
                "att_001", "emp_001", LocalDate.of(2026, 10, 1),
                LocalTime.of(9, 0), LocalTime.of(18, 0), 60
        );

        // 実労働480分に対し、セグメントが400分しかない
        assertThrows(IllegalArgumentException.class, () -> {
            att.fillAttendance(
                    LocalTime.of(9, 0), LocalTime.of(18, 0), 60,
                    LeaveType.NORMAL, "入力不備",
                    Collections.singletonList(
                            new WorkTimeSegment("seg_1", WorkCategory.DIRECT, 400, "開発")
                    )
            );
        });
    }

    @Test
    @DisplayName("月次確定でロックされたレコードは更新できないこと")
    void testLockedRecordCannotBeModified() {
        DailyAttendance att = DailyAttendance.createFromTemplate(
                "att_001", "emp_001", LocalDate.of(2026, 10, 1),
                LocalTime.of(9, 0), LocalTime.of(18, 0), 60
        );
        att.lock();

        assertThrows(IllegalStateException.class, () -> {
            att.fillAttendance(
                    LocalTime.of(9, 0), LocalTime.of(18, 0), 60,
                    LeaveType.NORMAL, "更新試行", null
            );
        });
    }
}
