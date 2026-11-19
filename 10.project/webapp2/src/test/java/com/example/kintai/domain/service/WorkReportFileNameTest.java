package com.example.kintai.domain.service;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** 作業報告書のファイル名の形（マスタ管理の「作業報告書の設定」） */
class WorkReportFileNameTest {

    private static final YearMonth NOV = YearMonth.of(2026, 11);

    @Test
    void 年と氏名を差し込み_様式のバージョンなどの文字はそのまま() {
        assertEquals("作業報告書2026（中島祐介）_41",
                WorkReportFileName.render("作業報告書{年}（{氏名}）_41", NOV, "1", "中島　祐介"));
        assertEquals("作業報告書_202611_123_山田太郎",
                WorkReportFileName.render("作業報告書_{年}{月}_{社員ID}_{氏名}", NOV, "123", "山田 太郎"));
    }

    @Test
    void 形がなければ今までの名前() {
        assertEquals("作業報告書202611", WorkReportFileName.render(null, NOV, "1", "中島祐介"));
        assertEquals("作業報告書202611", WorkReportFileName.render("  ", NOV, "1", "中島祐介"));
    }

    @Test
    void 氏名にファイル名に使えない文字があれば取り除く() {
        assertEquals("作業報告書2026（山田A）", WorkReportFileName.render("作業報告書{年}（{氏名}）", NOV, "1", "山田/A"));
    }

    @Test
    void 保存する前に形を確かめる() {
        assertEquals("作業報告書{年}（{氏名}）_41", WorkReportFileName.validate(" 作業報告書{年}（{氏名}）_41 "));
        assertThrows(IllegalArgumentException.class, () -> WorkReportFileName.validate(""));
        assertThrows(IllegalArgumentException.class, () -> WorkReportFileName.validate("作業報告書{名前}"));
        assertThrows(IllegalArgumentException.class, () -> WorkReportFileName.validate("作業報告書/{年}"));
        assertThrows(IllegalArgumentException.class, () -> WorkReportFileName.validate("作業報告書{年}.xlsm"));
        assertThrows(IllegalArgumentException.class, () -> WorkReportFileName.validate("あ".repeat(101)));
    }
}
