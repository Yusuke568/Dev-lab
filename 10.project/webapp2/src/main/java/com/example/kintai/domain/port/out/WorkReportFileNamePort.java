package com.example.kintai.domain.port.out;

import java.util.Optional;

/**
 * 作業報告書のファイル名の形（マスタ管理の「作業報告書の設定」）を読む。
 * 形の書き方は WorkReportFileName。
 */
public interface WorkReportFileNamePort {

    /** ファイル名の形。決めていなければ空（既定の形を使う） */
    Optional<String> workReportFileNamePattern();
}
