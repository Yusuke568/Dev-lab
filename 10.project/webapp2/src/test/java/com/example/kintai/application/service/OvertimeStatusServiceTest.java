package com.example.kintai.application.service;

import com.example.kintai.domain.model.attendance.OvertimeRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 残業事前申請を承認するときの、申請者のその月の時間外の集計 */
class OvertimeStatusServiceTest {

    private static OvertimeRequest request(int id, int day, int minutes, String status) {
        return new OvertimeRequest(id, "126", "中島", LocalDate.of(2026, 11, day), minutes, "09:00", "20:00", "",
                status, null, null, "2026-11-01 09:00");
    }

    @Test
    void 実績と予定と申請を分けて数える() {
        Map<LocalDate, Integer> recorded = new HashMap<>();
        recorded.put(LocalDate.of(2026, 11, 2), 120);
        recorded.put(LocalDate.of(2026, 11, 3), 90);
        recorded.put(LocalDate.of(2026, 11, 20), 60); // この申請の日の実績は「この申請」で数える
        OvertimeRequest self = request(1, 20, 150, OvertimeRequest.STATUS_PENDING);
        List<OvertimeRequest> others = List.of(
                request(2, 2, 100, OvertimeRequest.STATUS_APPROVED),  // 実績がある日 → 実績で数える
                request(3, 24, 180, OvertimeRequest.STATUS_APPROVED), // まだ実績なし → 承認済の予定
                request(4, 25, 60, OvertimeRequest.STATUS_PENDING),   // ほかの申請中
                request(5, 26, 999, OvertimeRequest.STATUS_REJECTED)); // 却下は数えない

        OvertimeStatusService.Status s = OvertimeStatusService.calculate(self, recorded, others, Map.of());

        assertEquals(210, s.actualMinutes());
        assertEquals(180, s.approvedPlannedMinutes());
        assertEquals(60, s.otherPendingMinutes());
        assertEquals(150, s.thisRequestMinutes());
        assertEquals(600, s.totalMinutes());
        assertEquals("2026年11月", s.month());
        // 折れ線グラフ用：日ごとの実績（申請日は0）と、日ごとの予定
        assertEquals(30, s.actualByDay().length);
        assertEquals(120, s.actualByDay()[1]);
        assertEquals(0, s.actualByDay()[19]);
        assertEquals(List.of(new OvertimeStatusService.Planned(20, 150, "THIS"),
                new OvertimeStatusService.Planned(24, 180, "APPROVED"),
                new OvertimeStatusService.Planned(25, 60, "PENDING")), s.planned());
    }

    @Test
    void 基準線は承認ルートの段と労基法の45時間() {
        Map<Integer, String> route = new TreeMap<>(Map.of(1110, "18.5h", 2700, "45h"));
        OvertimeStatusService.Status s = OvertimeStatusService.calculate(
                request(1, 20, 60, OvertimeRequest.STATUS_PENDING), Map.of(), List.of(), route);

        assertEquals(2, s.thresholds().size(), "45時間はルートと労基法で重ねない");
        assertEquals(1110, s.thresholds().get(0).minutes());
        assertEquals(2700, s.thresholds().get(1).minutes());
        assertTrue(s.thresholds().get(1).law());
    }
}
