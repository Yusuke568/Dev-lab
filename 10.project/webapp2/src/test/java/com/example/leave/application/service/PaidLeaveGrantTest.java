package com.example.leave.application.service;

import com.example.leave.domain.model.LeaveGrantRule;
import com.example.leave.domain.port.out.PaidLeavePort;
import com.example.shain.domain.model.Shain;
import com.example.shain.domain.model.ShainId;
import com.example.shain.domain.port.out.ShainPort;
import com.example.shared.transaction.TransactionManager;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** 勤続年数で有給を一括付与する */
class PaidLeaveGrantTest {

    /** 0年10日・1年11日・…・6年20日（よくある付与表） */
    private final List<LeaveGrantRule> rules = List.of(
            new LeaveGrantRule(0, 10), new LeaveGrantRule(1, 11), new LeaveGrantRule(2, 12), new LeaveGrantRule(3, 14),
            new LeaveGrantRule(4, 16), new LeaveGrantRule(5, 18), new LeaveGrantRule(6, 20));

    /** 社員ID → 更新後の残日数 */
    private final Map<Integer, Double> updated = new LinkedHashMap<>();

    private final List<Shain> staff = List.of(
            new Shain(ShainId.of(1), "新人", "", 2026, "", "", 0, 0),
            new Shain(ShainId.of(2), "3年目", "", 2023, "", "", 5, 3),
            new Shain(ShainId.of(3), "ベテラン", "", 2010, "", "", 30, 16),
            new Shain(ShainId.of(4), "入社年なし", "", 0, "", "", 3, 0));

    private PaidLeaveService service() {
        PaidLeavePort paidLeave = (PaidLeavePort) Proxy.newProxyInstance(PaidLeavePort.class.getClassLoader(),
                new Class<?>[] {PaidLeavePort.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "findAllRules": return rules;
                        case "updateDays": updated.put((Integer) args[0], (Double) args[1]); return null;
                        default: throw new UnsupportedOperationException(method.getName());
                    }
                });
        ShainPort shainPort = (ShainPort) Proxy.newProxyInstance(ShainPort.class.getClassLoader(),
                new Class<?>[] {ShainPort.class}, (proxy, method, args) -> {
                    if (method.getName().equals("findAll")) return staff;
                    throw new UnsupportedOperationException(method.getName());
                });
        TransactionManager tx = new TransactionManager() {
            @Override public <T> T executeInTransaction(TransactionOperation<T> operation) {
                try {
                    return operation.execute();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            @Override public void executeInTransaction(TransactionRunnable operation) {
                executeInTransaction(() -> { operation.run(); return null; });
            }
        };
        return new PaidLeaveService(paidLeave, shainPort, tx);
    }

    @Test
    void 勤続年数は入社年から数える() {
        assertEquals(0, Shain.yearsOfServiceFrom(2026, 2026));
        assertEquals(3, Shain.yearsOfServiceFrom(2023, 2026));
        assertEquals(0, Shain.yearsOfServiceFrom(0, 2026));
        assertEquals(0, Shain.yearsOfServiceFrom(2027, 2026));
    }

    @Test
    void ルールより長い人には一番長いルールの日数() {
        assertEquals(10, PaidLeaveService.grantDaysFor(0, rules));
        assertEquals(14, PaidLeaveService.grantDaysFor(3, rules));
        assertEquals(20, PaidLeaveService.grantDaysFor(16, rules));
        assertNull(PaidLeaveService.grantDaysFor(0, List.of(new LeaveGrantRule(1, 11))));
    }

    @Test
    void 全社員の残日数に勤続年数に合う日数を足す() {
        int count = service().grantLeaveByYearsOfService();

        assertEquals(3, count);
        assertEquals(Map.of(1, 10.0, 2, 19.0, 3, 50.0), updated);
    }

    @Test
    void 画面の確認用に足す日数を返す() {
        assertEquals(Map.of(1, 10, 2, 14, 3, 20), service().previewGrantDays());
    }
}
