package com.example.kintai.domain.port.out;

import com.example.kintai.domain.model.attendance.ImportedMonthData;

import java.io.InputStream;
import java.util.List;

/**
 * 社内所定の「作業報告書」Excel（.xlsm）を読み取り、勤怠データ・週次作業報告を抽出するポート。
 */
public interface WorkReportImportPort {

    /**
     * アップロードされた作業報告書Excelの全シート（1月～12月・前年12月のうち、
     * 年月が設定されているもの）を読み取り、月ごとのデータに変換する。
     */
    List<ImportedMonthData> parse(InputStream xlsmInputStream);

    /**
     * 作業報告書に書かれた社員の名前（月のシートの「名前：」欄）。見つからなければ null。
     * 週報担当がまとめて取り込むときに、どの社員のファイルかを決めるのに使う。
     */
    default String readStaffName(InputStream xlsmInputStream) {
        return null;
    }
}
