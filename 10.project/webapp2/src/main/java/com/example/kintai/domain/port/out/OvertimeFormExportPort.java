package com.example.kintai.domain.port.out;

import com.example.kintai.domain.service.OvertimeFormCalculator.ApprovalMark;
import com.example.kintai.domain.service.OvertimeFormCalculator.TimeCells;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * 個人別時間外勤務申請兼実績報告書（Excel）を作るポート。
 */
public interface OvertimeFormExportPort {

    /** 1か月分の申請書 */
    record OvertimeForm(YearMonth month, String departmentName, String staffName,
                        String sectionChiefName, String departmentHeadName, List<FormRow> rows) {
    }

    /**
     * 申請1件分の行。
     * @param approvals       申請時の承認欄（課長／所属長／社長の3つ）
     * @param actualApprovals 実績報告の承認欄（同じく3つ）。null なら申請時と同じ
     */
    record FormRow(LocalDate date, String projectName, TimeCells planned, String category,
                   List<ApprovalMark> approvals, TimeCells actual, String remarks, List<ApprovalMark> actualApprovals) {
        public FormRow(LocalDate date, String projectName, TimeCells planned, String category,
                       List<ApprovalMark> approvals, TimeCells actual, String remarks) {
            this(date, projectName, planned, category, approvals, actual, remarks, null);
        }
    }

    /** 様式に入りきる行数（10行目〜40行目） */
    int MAX_ROWS = 31;

    byte[] write(OvertimeForm form);
}
