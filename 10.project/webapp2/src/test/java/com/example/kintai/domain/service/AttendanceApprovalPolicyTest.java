package com.example.kintai.domain.service;

import com.example.kintai.domain.model.attendance.AttendanceRecord;
import com.example.kintai.domain.model.attendance.WorkTime;
import com.example.kintai.domain.model.employee.EmployeeId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static com.example.kintai.domain.service.AttendanceApprovalPolicy.APPROVED;
import static com.example.kintai.domain.service.AttendanceApprovalPolicy.DRAFT;
import static com.example.kintai.domain.service.AttendanceApprovalPolicy.PENDING;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** 保存する勤怠の承認状態の決め方（作業報告書の取り込み直しで承認待ちに戻さない） */
class AttendanceApprovalPolicyTest {

    private static final LocalDate START = LocalDate.of(2026, 11, 1);
    private static final LocalDate OCT = LocalDate.of(2026, 10, 15);
    private static final LocalDate NOV = LocalDate.of(2026, 11, 16);

    private static AttendanceRecord record(LocalDate date, String from, String to, Integer abstractId, int overtime, int status) {
        AttendanceRecord r = new AttendanceRecord(new EmployeeId("1"), date,
                new WorkTime(LocalTime.parse(from), LocalTime.parse(to)));
        r.setAbstractId(abstractId);
        r.setOvertimeMinutes(overtime);
        r.setApprovalStatus(status);
        return r;
    }

    @Test
    void 一時保存は一時保存_承認が要らなければ承認済み() {
        AttendanceRecord now = record(NOV, "09:00", "20:00", 1, 120, 0);
        assertEquals(DRAFT, AttendanceApprovalPolicy.decide(true, true, NOV, START, null, now));
        assertEquals(APPROVED, AttendanceApprovalPolicy.decide(false, false, NOV, START, null, now));
    }

    @Test
    void 運用開始日より前の日は_時間外があっても承認済み() {
        AttendanceRecord now = record(OCT, "09:00", "20:00", 1, 120, 0);
        assertEquals(APPROVED, AttendanceApprovalPolicy.decide(false, true, OCT, START, null, now));
        assertEquals(PENDING, AttendanceApprovalPolicy.decide(false, true, OCT, null, null, now));
        assertEquals(PENDING, AttendanceApprovalPolicy.decide(false, true, START, START, null, now));
    }

    @Test
    void 承認済みの日は内容が同じなら承認済みのまま() {
        AttendanceRecord before = record(NOV, "09:00", "20:00", 1, 120, APPROVED);
        AttendanceRecord now = record(NOV, "09:00", "20:00", 1, 120, 0);
        assertEquals(APPROVED, AttendanceApprovalPolicy.decide(false, true, NOV, START, before, now));
    }

    @Test
    void 承認済みでも内容が変われば承認待ちに戻る() {
        AttendanceRecord before = record(NOV, "09:00", "20:00", 1, 120, APPROVED);
        assertEquals(PENDING, AttendanceApprovalPolicy.decide(false, true, NOV, START, before, record(NOV, "09:00", "21:00", 1, 180, 0)));
        assertEquals(PENDING, AttendanceApprovalPolicy.decide(false, true, NOV, START, before, record(NOV, "09:00", "20:00", 2, 120, 0)));
    }

    @Test
    void 承認待ちや却下の日は内容が同じでも承認待ち() {
        AttendanceRecord now = record(NOV, "09:00", "20:00", 1, 120, 0);
        assertEquals(PENDING, AttendanceApprovalPolicy.decide(false, true, NOV, START, record(NOV, "09:00", "20:00", 1, 120, PENDING), now));
        assertEquals(PENDING, AttendanceApprovalPolicy.decide(false, true, NOV, START, record(NOV, "09:00", "20:00", 1, 120, 3), now));
    }
}
