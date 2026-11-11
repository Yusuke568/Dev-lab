package com.example.kintai.domain.port.out;

import com.example.entity.WorkType;
import com.example.kintai.adapter.in.web.dto.MonthlyAttendanceDto;
import com.example.kintai.domain.model.attendance.WeeklyWorkReport;

import java.util.List;

/**
 * 月次勤怠データを、社内所定の「作業報告書」Excel（.xlsm）様式に変換するポート。
 */
public interface WorkReportExportPort {

    /**
     * @param monthlyData         出力対象の月次勤怠データ
     * @param employeeName        氏名（様式のAP2セルに設定）
     * @param workTypes           勤務区分の一覧（各日の勤務区分を「摘要」欄の値に読み替えるために使う）
     * @param weeklyWorkReports   月の週ブロック構成に従った5週分の週次作業報告（表示順どおり）
     * @return 生成された .xlsm ファイルのバイト列
     */
    byte[] generate(MonthlyAttendanceDto monthlyData, String employeeName, List<WorkType> workTypes,
                     List<WeeklyWorkReport> weeklyWorkReports);

    /** 1か月分（勤怠と週次作業報告） */
    record MonthData(MonthlyAttendanceDto monthlyData, List<WeeklyWorkReport> weeklyWorkReports) {
    }

    /**
     * 前年12月〜当年12月の作業報告書を1ファイルで作る。
     * すべての月のシートに年と氏名を入れ、months にある月のシートに勤怠と週次作業報告を書く。
     * @param year 当年（前年12月のシートは year − 1 年）
     */
    default byte[] generateYear(int year, String employeeName, List<WorkType> workTypes, List<MonthData> months) {
        throw new UnsupportedOperationException("generateYear");
    }
}
