package com.example.kintai.domain.service;

import com.example.kintai.domain.model.attendance.AttendanceRecord;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 保存する勤怠の承認状態（0:一時保存 / 1:承認待ち / 2:承認済み）を決める。
 *
 *   ・一時保存は一時保存
 *   ・有給・事前申請のない時間外がなければ承認済み
 *   ・承認の運用開始日より前の日は承認済み（作業報告書を取り込み直しても承認待ちに戻さない）
 *   ・すでに承認済みの日は、出勤・退勤・勤務区分・時間外が同じなら承認済みのまま
 *   ・それ以外は承認待ち
 */
public final class AttendanceApprovalPolicy {

    public static final int DRAFT = 0;
    public static final int PENDING = 1;
    public static final int APPROVED = 2;

    private AttendanceApprovalPolicy() {
    }

    /**
     * @param temporary      一時保存か
     * @param needsApproval  内容から見て承認が要るか（有給・事前申請のない時間外）
     * @param date           勤怠の日
     * @param approvalStart  承認の運用開始日。null なら全期間が対象
     * @param before         保存する前の勤怠。なければ null
     * @param after          保存する勤怠（時間外は計算済み）
     */
    public static int decide(boolean temporary, boolean needsApproval, LocalDate date, LocalDate approvalStart,
                             AttendanceRecord before, AttendanceRecord after) {
        if (temporary) return DRAFT;
        if (!needsApproval) return APPROVED;
        if (approvalStart != null && date.isBefore(approvalStart)) return APPROVED;
        if (before != null && before.getApprovalStatus() == APPROVED && sameForApproval(before, after)) return APPROVED;
        return PENDING;
    }

    /** 承認の判断に関わる内容（出勤・退勤・勤務区分・時間外）が同じか */
    static boolean sameForApproval(AttendanceRecord a, AttendanceRecord b) {
        return Objects.equals(a.getStartTime(), b.getStartTime())
                && Objects.equals(a.getEndTime(), b.getEndTime())
                && Objects.equals(a.getAbstractId(), b.getAbstractId())
                && a.getOvertimeMinutes() == b.getOvertimeMinutes();
    }
}
