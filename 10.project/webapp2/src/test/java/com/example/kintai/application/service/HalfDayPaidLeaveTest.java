package com.example.kintai.application.service;

import com.example.application.port.out.WorkTypePort;
import com.example.entity.WorkType;
import com.example.kintai.application.port.in.UpdateAttendanceUseCase.AttendanceUpdateCommand;
import com.example.kintai.domain.model.attendance.AttendanceRecord;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.kintai.domain.port.out.LoadAttendanceRecordPort;
import com.example.kintai.domain.port.out.OvertimeRequestPort;
import com.example.kintai.domain.port.out.SaveAttendanceRecordPort;
import com.example.leave.domain.model.PaidLeaveDays;
import com.example.leave.domain.port.out.PaidLeavePort;
import com.example.shared.transaction.TransactionManager;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 勤怠を保存したときの有給残日数の増減（有給 1 日・半休 0.5 日） */
class HalfDayPaidLeaveTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 10);
    private static final int WORK = 1;
    private static final int PAID = 3;
    private static final int HALF = 5;

    /** 保存済みの勤務区分（日付 → 区分ID） */
    private final Map<LocalDate, Integer> stored = new HashMap<>();
    /** 有給残日数の増減の記録 */
    private final List<Double> adjustments = new ArrayList<>();
    private final UpdateAttendanceService service = new UpdateAttendanceService(
            proxy(SaveAttendanceRecordPort.class, (name, args) -> {
                AttendanceRecord r = (AttendanceRecord) args[0];
                stored.put(r.getWorkDate(), r.getAbstractId());
                return null;
            }),
            proxy(LoadAttendanceRecordPort.class, (name, args) -> {
                LocalDate date = (LocalDate) args[1];
                if (!stored.containsKey(date)) return Optional.empty();
                AttendanceRecord r = new AttendanceRecord((EmployeeId) args[0], date, null);
                r.setAbstractId(stored.get(date));
                return Optional.of(r);
            }),
            proxy(PaidLeavePort.class, (name, args) -> {
                if ("adjustDays".equals(name)) adjustments.add((Double) args[1]);
                return null;
            }),
            proxy(TransactionManager.class, (name, args) -> {
                if (args[0] instanceof TransactionManager.TransactionOperation) {
                    return ((TransactionManager.TransactionOperation<?>) args[0]).execute();
                }
                ((TransactionManager.TransactionRunnable) args[0]).run();
                return null;
            }),
            proxy(WorkTypePort.class, (name, args) -> List.of(type(WORK, "出勤", false, 0), type(PAID, "有給", true, 1),
                    type(HALF, "半休", false, 0.5))),
            proxy(OvertimeRequestPort.class, (name, args) -> Optional.empty()));

    private interface Handler {
        Object handle(String name, Object[] args) throws Exception;
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Handler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type},
                (p, method, args) -> handler.handle(method.getName(), args));
    }

    private static WorkType type(int id, String name, boolean paid, double days) {
        WorkType t = new WorkType();
        t.setId(id);
        t.setName(name);
        t.setPaid(paid);
        t.setPaidLeaveDays(days);
        return t;
    }

    private void save(int abstractId, String from, String to) {
        service.updateAttendance(List.of(new AttendanceUpdateCommand("126", DAY, "TUE", from, to, 0, abstractId,
                "", null, null, null, null, null, null, false)));
    }

    @Test
    void 出勤から半休にすると0_5日減る() {
        stored.put(DAY, WORK);
        save(HALF, "13:00", "18:00");
        assertEquals(List.of(-0.5), adjustments);
    }

    @Test
    void 有給から半休にすると0_5日戻る() {
        stored.put(DAY, PAID);
        save(HALF, "13:00", "18:00");
        assertEquals(List.of(0.5), adjustments);
    }

    @Test
    void 半休から出勤に戻すと0_5日戻る() {
        stored.put(DAY, HALF);
        save(WORK, "09:00", "18:00");
        assertEquals(List.of(0.5), adjustments);
    }

    @Test
    void 有給は1日減り_同じ区分で保存し直しても増減しない() {
        stored.put(DAY, WORK);
        save(PAID, "", "");
        save(PAID, "", "");
        assertEquals(List.of(-1.0), adjustments);
    }

    @Test
    void 半休は出退勤があってよいが_有給の日に出退勤があれば止める() {
        stored.put(DAY, WORK);
        save(HALF, "13:00", "18:00");

        RuntimeException e = assertThrows(RuntimeException.class, () -> save(PAID, "09:00", "18:00"));
        assertTrue(String.valueOf(e.getCause() != null ? e.getCause().getMessage() : e.getMessage()).contains("有給"));
    }

    @Test
    void 日数の表示は整数なら小数点を付けない() {
        assertEquals("12", PaidLeaveDays.format(12.0));
        assertEquals("11.5", PaidLeaveDays.format(11.5));
        assertEquals("0", PaidLeaveDays.format(0));
    }
}
