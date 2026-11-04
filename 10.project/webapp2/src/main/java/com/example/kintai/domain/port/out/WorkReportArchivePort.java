package com.example.kintai.domain.port.out;

import java.io.IOException;
import java.time.YearMonth;

/**
 * 出力した作業報告書を、サーバーの所定フォルダ（共有して参照・編集する場所）にも置くポート。
 */
public interface WorkReportArchivePort {

    /** 置き場所が設定されているか。設定されていなければ置かない。 */
    boolean isEnabled();

    /**
     * 作業報告書を置く。同じ社員・同じ月のファイルがすでにあれば、上書きする前にバックアップへ移す
     * （出力後に共有フォルダ上で書き足した内容（リーダー所見など）を失わないため）。
     *
     * @return 置いたファイルの場所（画面やログに出す用）
     */
    String archive(String staffId, String staffName, YearMonth yearMonth, byte[] content) throws IOException;
}
