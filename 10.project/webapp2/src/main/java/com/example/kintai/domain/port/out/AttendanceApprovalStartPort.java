package com.example.kintai.domain.port.out;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 勤怠の承認の運用開始日（マスタ管理の承認設定）を読む。
 * この日より前の勤怠は、有給や時間外があっても承認待ちにしない。
 */
public interface AttendanceApprovalStartPort {

    /** 運用開始日。未設定なら空（すべての日を承認の対象にする） */
    Optional<LocalDate> attendanceApprovalStartDate();
}
