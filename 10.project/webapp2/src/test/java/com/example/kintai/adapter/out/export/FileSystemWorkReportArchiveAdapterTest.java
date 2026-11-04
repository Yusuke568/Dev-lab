package com.example.kintai.adapter.out.export;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileSystemWorkReportArchiveAdapterTest {

    @TempDir
    Path base;

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-04T01:02:03Z"), ZoneId.of("Asia/Tokyo"));

    @Test
    void 年と月のフォルダに社員ごとのファイルを置く() throws Exception {
        FileSystemWorkReportArchiveAdapter adapter = new FileSystemWorkReportArchiveAdapter(base, clock);

        String path = adapter.archive("126", "山田 太郎", YearMonth.of(2026, 11), new byte[] {1, 2, 3});

        Path expected = base.resolve("2026").resolve("11").resolve("作業報告書_202611_126_山田_太郎.xlsm");
        assertEquals(expected.toString(), path);
        assertArrayEquals(new byte[] {1, 2, 3}, Files.readAllBytes(expected));
    }

    @Test
    void 同じ月をもう一度出力したら前のファイルをバックアップへ移してから置く() throws Exception {
        FileSystemWorkReportArchiveAdapter adapter = new FileSystemWorkReportArchiveAdapter(base, clock);
        adapter.archive("126", "山田 太郎", YearMonth.of(2026, 11), new byte[] {1});

        adapter.archive("126", "山田 太郎", YearMonth.of(2026, 11), new byte[] {2});

        Path month = base.resolve("2026").resolve("11");
        assertArrayEquals(new byte[] {2}, Files.readAllBytes(month.resolve("作業報告書_202611_126_山田_太郎.xlsm")));
        // 共有フォルダで書き足した内容（リーダー所見など）を失わないよう、前のファイルは日時付きで残す
        Path backup = month.resolve("_backup").resolve("作業報告書_202611_126_山田_太郎_20261104_100203.xlsm");
        assertArrayEquals(new byte[] {1}, Files.readAllBytes(backup));
    }

    @Test
    void 書き込み用の一時ファイルは残さない() throws Exception {
        new FileSystemWorkReportArchiveAdapter(base, clock).archive("126", "山田", YearMonth.of(2026, 11), new byte[] {1});

        try (var files = Files.list(base.resolve("2026").resolve("11"))) {
            assertTrue(files.noneMatch(p -> p.getFileName().toString().startsWith(".tmp_")));
        }
    }

    @Test
    void ファイル名に使えない文字は置き換える() {
        assertEquals("a_b_c_d", FileSystemWorkReportArchiveAdapter.safe("a/b:c*d"));
        assertEquals("山田_太郎", FileSystemWorkReportArchiveAdapter.safe("山田　太郎"));
        assertEquals("_", FileSystemWorkReportArchiveAdapter.safe(null));
    }

    @Test
    void 置き場所が設定されていなければ置かない() {
        assertFalse(new FileSystemWorkReportArchiveAdapter(null, clock).isEnabled());
        assertTrue(new FileSystemWorkReportArchiveAdapter(base, clock).isEnabled());
    }

    @Test
    void 既定のフォルダはあるときだけ使い_後から作っても使える() throws Exception {
        Path defaultDir = base.resolve("srv-kintai-work-reports");
        FileSystemWorkReportArchiveAdapter adapter = new FileSystemWorkReportArchiveAdapter(defaultDir, clock, true);

        assertFalse(adapter.isEnabled(), "フォルダがなければ置かない");
        assertTrue(adapter.describe().contains("現在なし"));

        Files.createDirectories(defaultDir);

        assertTrue(adapter.isEnabled(), "Tomcat を起動した後にフォルダを作っても置く");
        adapter.archive("126", "山田", YearMonth.of(2026, 11), new byte[] {1});
        assertTrue(Files.exists(defaultDir.resolve("2026").resolve("11").resolve("作業報告書_202611_126_山田.xlsm")));
    }

    @Test
    void 設定したフォルダはなければ作って置く() throws Exception {
        Path configured = base.resolve("configured");
        FileSystemWorkReportArchiveAdapter adapter = new FileSystemWorkReportArchiveAdapter(configured, clock);

        assertTrue(adapter.isEnabled());
        adapter.archive("126", "山田", YearMonth.of(2026, 11), new byte[] {1});
        assertTrue(Files.exists(configured.resolve("2026").resolve("11").resolve("作業報告書_202611_126_山田.xlsm")));
    }
}
