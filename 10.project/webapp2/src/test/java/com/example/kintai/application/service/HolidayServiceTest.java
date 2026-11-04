package com.example.kintai.application.service;

import com.example.master.application.service.MasterMaintenanceService;
import com.example.master.domain.port.out.MasterPort;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 会社休日（マスタ）と国民の祝日 */
class HolidayServiceTest {

    @Test
    void 国民の祝日と会社休日をまとめて返し_重なる日は名前をつなぐ() {
        HolidayService service = new HolidayService((from, to) -> Map.of(
                LocalDate.of(2026, 11, 3), "創立記念日",
                LocalDate.of(2026, 11, 20), "社内イベント"));

        Map<LocalDate, String> holidays = service.getHolidays(YearMonth.of(2026, 11));

        assertEquals("文化の日・創立記念日", holidays.get(LocalDate.of(2026, 11, 3)));
        assertEquals("社内イベント", holidays.get(LocalDate.of(2026, 11, 20)));
        assertEquals("勤労感謝の日", holidays.get(LocalDate.of(2026, 11, 23)));
        assertEquals(3, holidays.size());
    }

    @Test
    void 会社休日を読むときは月初から月末までを指定する() {
        LocalDate[] asked = new LocalDate[2];
        new HolidayService((from, to) -> {
            asked[0] = from;
            asked[1] = to;
            return Map.of();
        }).getHolidays(YearMonth.of(2026, 2));

        assertEquals(LocalDate.of(2026, 2, 1), asked[0]);
        assertEquals(LocalDate.of(2026, 2, 28), asked[1]);
    }

    // ---- マスタでの登録 ----

    private static MasterPort masterPort(Map<LocalDate, String> saved) {
        return (MasterPort) Proxy.newProxyInstance(MasterPort.class.getClassLoader(), new Class<?>[] {MasterPort.class},
                (proxy, method, args) -> {
                    if ("saveCompanyHoliday".equals(method.getName())) {
                        saved.put((LocalDate) args[0], (String) args[1]);
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    @Test
    void 期間で登録すると毎日を会社休日にする() {
        Map<LocalDate, String> saved = new TreeMap<>();
        MasterMaintenanceService service = new MasterMaintenanceService(masterPort(saved));

        int count = service.addCompanyHolidays(LocalDate.of(2026, 12, 29), LocalDate.of(2027, 1, 3), " 年末年始休暇 ");

        assertEquals(6, count);
        assertEquals(6, saved.size());
        assertEquals("年末年始休暇", saved.get(LocalDate.of(2027, 1, 1)));
    }

    @Test
    void 終了日がなければ1日だけ登録する() {
        Map<LocalDate, String> saved = new TreeMap<>();
        new MasterMaintenanceService(masterPort(saved)).addCompanyHolidays(LocalDate.of(2026, 6, 1), null, "創立記念日");

        assertEquals(Map.of(LocalDate.of(2026, 6, 1), "創立記念日"), saved);
    }

    @Test
    void 名前がない_終了日が開始日より前_31日を超える期間は登録しない() {
        Map<LocalDate, String> saved = new TreeMap<>();
        MasterMaintenanceService service = new MasterMaintenanceService(masterPort(saved));

        assertThrows(IllegalArgumentException.class, () -> service.addCompanyHolidays(LocalDate.of(2026, 6, 1), null, " "));
        assertThrows(IllegalArgumentException.class,
                () -> service.addCompanyHolidays(LocalDate.of(2026, 6, 2), LocalDate.of(2026, 6, 1), "休み"));
        IllegalArgumentException tooLong = assertThrows(IllegalArgumentException.class,
                () -> service.addCompanyHolidays(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 2), "休み"));
        assertTrue(tooLong.getMessage().contains("31日"));
        assertTrue(saved.isEmpty());
    }
}
