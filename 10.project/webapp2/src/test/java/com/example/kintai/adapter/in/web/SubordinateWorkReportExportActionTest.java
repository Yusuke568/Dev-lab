package com.example.kintai.adapter.in.web;

import com.example.kintai.domain.model.attendance.Subordinate;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 部下の作業報告書をまとめて出すときの、ZIP の中のファイル名と中身 */
class SubordinateWorkReportExportActionTest {

    @Test
    void ファイル名は年月_社員ID_氏名で_空白やファイル名に使えない文字を除く() {
        Subordinate s = new Subordinate("123", "山田　太郎/A", "開発課");
        assertEquals("作業報告書202611_123_山田太郎A.xlsm",
                SubordinateWorkReportExportAction.fileName(YearMonth.of(2026, 11), s, Set.of()));
    }

    @Test
    void 同じ名前が続けば番号を付ける() {
        Subordinate s = new Subordinate("123", "山田太郎", "開発課");
        assertEquals("作業報告書202602_123_山田太郎(2).xlsm",
                SubordinateWorkReportExportAction.fileName(YearMonth.of(2026, 2), s, Set.of("作業報告書202602_123_山田太郎.xlsm")));
    }

    @Test
    void ZIPには作業報告書と_出せなかった社員の一覧が入る() throws Exception {
        Map<String, byte[]> files = new LinkedHashMap<>();
        files.put("作業報告書202611_1_山田.xlsm", new byte[] {1, 2, 3});
        byte[] zip = SubordinateWorkReportExportAction.zip(files, List.of("社員ID 9：部下ではないため出せません。"));

        List<String> names = new ArrayList<>();
        String text = null;
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip), StandardCharsets.UTF_8)) {
            for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
                names.add(e.getName());
                byte[] b = in.readAllBytes();
                if (e.getName().endsWith(".txt")) text = new String(b, 3, b.length - 3, StandardCharsets.UTF_8);
            }
        }
        assertEquals(List.of("作業報告書202611_1_山田.xlsm", "出せなかった社員.txt"), names);
        assertEquals("社員ID 9：部下ではないため出せません。\r\n", text);
    }

    @Test
    void 全員出せればZIPには作業報告書だけが入る() throws Exception {
        byte[] zip = SubordinateWorkReportExportAction.zip(Map.of("a.xlsm", new byte[] {1}), List.of());
        int count = 0;
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip), StandardCharsets.UTF_8)) {
            while (in.getNextEntry() != null) count++;
        }
        assertEquals(1, count);
    }
}
