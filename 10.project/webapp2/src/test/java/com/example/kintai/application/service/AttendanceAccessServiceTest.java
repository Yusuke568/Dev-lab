package com.example.kintai.application.service;

import com.example.kintai.domain.model.attendance.Subordinate;
import com.example.kintai.domain.port.out.SubordinatePort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttendanceAccessServiceTest {

    private static final Set<String> APPROVER = Set.of("ATTENDANCE", "APPROVAL");
    private static final Set<String> GENERAL = Set.of("ATTENDANCE");

    /** 課長(300)の配下は 301・302、部長(200)の配下は 300・301・302 */
    private static final Map<String, Set<String>> MANAGED = Map.of(
            "300", Set.of("301", "302"),
            "200", Set.of("300", "301", "302"));

    private final AttendanceAccessService service = new AttendanceAccessService(new SubordinatePort() {
        @Override
        public boolean isManagedBy(String employeeId, String approverId) {
            return MANAGED.getOrDefault(approverId, Set.of()).contains(employeeId);
        }

        @Override
        public List<Subordinate> findManagedStaff(String approverId) {
            return MANAGED.getOrDefault(approverId, Set.of()).stream().sorted()
                    .map(id -> new Subordinate(id, "社員" + id, "開発1課")).toList();
        }

        @Override
        public java.util.Optional<Subordinate> findStaff(String staffId) {
            return java.util.Optional.of(new Subordinate(staffId, "社員" + staffId, "開発1課"));
        }

        @Override
        public List<Subordinate> findStaffInMyDepartments(String staffId) {
            return List.of(new Subordinate(staffId, "本人", "開発1課"), new Subordinate("301", "社員301", "開発1課"));
        }
    });

    @Test
    void 週報担当だけが所属部署の社員の作業報告書を取り込める() {
        assertEquals(2, service.getWeeklyReportTargets("300", Set.of("ATTENDANCE", "WEEKLY_REPORT_ADMIN")).size());
        assertTrue(service.getWeeklyReportTargets("300", APPROVER).isEmpty(), "承認権限だけでは取り込めない");
        assertTrue(service.getWeeklyReportTargets(null, Set.of("WEEKLY_REPORT_ADMIN")).isEmpty());
    }

    @Test
    void 本人の勤怠表は誰でも扱える() {
        assertTrue(service.canAccess("301", GENERAL, "301"));
    }

    @Test
    void 承認権限があれば配下の社員の勤怠表を扱える() {
        assertTrue(service.canAccess("300", APPROVER, "301"));
        assertTrue(service.canAccess("200", APPROVER, "302"), "下位部署の社員も配下");
    }

    @Test
    void 配下でない社員の勤怠表は扱えない() {
        assertFalse(service.canAccess("300", APPROVER, "200"), "上司は配下ではない");
        assertFalse(service.canAccess("300", APPROVER, "999"));
    }

    @Test
    void 承認権限がなければ組織図上の配下でも扱えない() {
        assertFalse(service.canAccess("300", GENERAL, "301"));
        assertFalse(service.canAccess("300", null, "301"));
    }

    @Test
    void 社員IDの形でなければ扱えない() {
        assertFalse(service.canAccess("300", APPROVER, "301 OR 1=1"));
        assertFalse(service.canAccess(null, APPROVER, "301"));
    }

    @Test
    void 配下の一覧は承認権限があるときだけ返し_自分を先頭に入れる() {
        assertEquals(List.of("300", "301", "302"),
                service.getSubordinates("300", APPROVER).stream().map(Subordinate::getStaffId).toList());
        assertTrue(service.getSubordinates("300", GENERAL).isEmpty());
    }
}
