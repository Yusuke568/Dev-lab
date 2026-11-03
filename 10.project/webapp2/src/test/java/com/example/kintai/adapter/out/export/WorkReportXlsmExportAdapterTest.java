package com.example.kintai.adapter.out.export;

import com.example.entity.WorkType;
import com.example.kintai.adapter.in.web.dto.DailyAttendanceDto;
import com.example.kintai.adapter.in.web.dto.MonthlyAttendanceDto;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkReportXlsmExportAdapterTest {

    private static WorkType workType(int id, String name, boolean paid) {
        WorkType wt = new WorkType();
        wt.setId(id);
        wt.setName(name);
        wt.setPaid(paid);
        return wt;
    }

    private static DailyAttendanceDto day(int dayOfMonth, String start, String end, Integer abstractId) {
        return new DailyAttendanceDto(LocalDate.of(2026, 11, dayOfMonth), start, end, "", "", abstractId,
                null, null, null, null, 0, 0);
    }

    /** 様式（実際のテンプレート）に出力し、摘要（AN列）に勤務区分が入ることを確かめる */
    @Test
    void 摘要欄に勤務区分を様式の表記で出力する() throws Exception {
        List<WorkType> workTypes = List.of(workType(1, "出勤", false), workType(10, "休日", false),
                workType(3, "有給", true), workType(4, "代休", false), workType(9, "その他", false),
                workType(12, "欠勤", false));
        List<DailyAttendanceDto> days = new ArrayList<>();
        days.add(day(1, "-", "-", 10));        // 休日 → 様式にないので空欄
        days.add(day(2, "09:00", "18:00", 1)); // 出勤
        days.add(day(3, "-", "-", 3));         // 有給 → 有休
        days.add(day(4, "-", "-", 4));         // 代休
        days.add(day(5, "09:00", "18:00", 9)); // その他 → 様式の「そ他」
        days.add(day(6, "-", "-", null));      // 未設定 → 空欄
        days.add(day(9, "-", "-", 12));        // 様式の選択肢にない区分 → 名前を印字
        MonthlyAttendanceDto month = new MonthlyAttendanceDto(YearMonth.of(2026, 11), days, "16:00", 960, 0, 0, "山田 太郎");

        byte[] xlsm = new WorkReportXlsmExportAdapter().generate(month, "山田 太郎", workTypes, List.of());

        try (XSSFWorkbook book = new XSSFWorkbook(new ByteArrayInputStream(xlsm))) {
            Sheet sheet = book.getSheet("11月");
            assertEquals("", an(sheet, 1));
            assertEquals("出勤", an(sheet, 2));
            assertEquals("有休", an(sheet, 3));
            assertEquals("代休", an(sheet, 4));
            assertEquals("そ他", an(sheet, 5));
            assertEquals("", an(sheet, 6));
            assertEquals("欠勤", an(sheet, 9));
        }
    }

    /** 週次作業報告の「問題点・対処」は、週ブロックの「問題点・対処：」の下の2行（AS列）に出る */
    @Test
    void 問題点と対処を週ブロックの2行に出力する() throws Exception {
        MonthlyAttendanceDto month = new MonthlyAttendanceDto(YearMonth.of(2026, 11), List.of(), "0:00", 0, 0, 0, "山田 太郎");
        com.example.kintai.domain.model.attendance.WeeklyWorkReport week1 = new com.example.kintai.domain.model.attendance.WeeklyWorkReport(
                "126", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 4),
                "客先A", "PJ-X", "設計", "オンスケ", "画面設計\nレビュー対応", "順調", "問題なし\n");

        byte[] xlsm = new WorkReportXlsmExportAdapter().generate(month, "山田 太郎", List.of(), List.of(week1));

        try (XSSFWorkbook book = new XSSFWorkbook(new ByteArrayInputStream(xlsm))) {
            Sheet sheet = book.getSheet("11月");
            // 第1週は Excel の5〜14行目。「問題なし」は短い1行なので、見出し（12行目）に続けて書き、下の説明文は消す
            assertEquals("問題点・対処：問題なし", text(sheet, "AS12"));
            assertEquals("", text(sheet, "AS13"));
            assertEquals("", text(sheet, "AS14"));
            assertEquals("作業進捗：オンスケ", text(sheet, "AS5"));
            assertEquals("画面設計", text(sheet, "AS6"));
        }
    }

    @Test
    void 長い問題点と対処は見出しの下の2行に書く() throws Exception {
        MonthlyAttendanceDto month = new MonthlyAttendanceDto(YearMonth.of(2026, 11), List.of(), "0:00", 0, 0, 0, "山田 太郎");
        com.example.kintai.domain.model.attendance.WeeklyWorkReport week1 = new com.example.kintai.domain.model.attendance.WeeklyWorkReport(
                "126", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 4),
                null, null, null, null, null, null, "レビュー指摘が多く遅延気味\n有識者レビューを追加");

        byte[] xlsm = new WorkReportXlsmExportAdapter().generate(month, "山田 太郎", List.of(), List.of(week1));

        try (XSSFWorkbook book = new XSSFWorkbook(new ByteArrayInputStream(xlsm))) {
            Sheet sheet = book.getSheet("11月");
            assertEquals("問題点・対処：", text(sheet, "AS12"));
            assertEquals("レビュー指摘が多く遅延気味", text(sheet, "AS13"));
            assertEquals("有識者レビューを追加", text(sheet, "AS14"));
        }
    }

    /** 作業報告書の例のように、工程名の行の右に作業内容を書く。空行の位置も取込で戻る */
    @Test
    void 工程名と作業内容の行の位置をそろえて出力し_取り込んでも位置が変わらない() throws Exception {
        MonthlyAttendanceDto month = new MonthlyAttendanceDto(YearMonth.of(2026, 10), List.of(), "0:00", 0, 0, 0, "山田 太郎");
        com.example.kintai.domain.model.attendance.WeeklyWorkReport week1 = new com.example.kintai.domain.model.attendance.WeeklyWorkReport(
                "126", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 7),
                "BCC", "住民税業務保守",
                "問い合わせ対応サポート\n問い合わせ対応\n住民税スケジュール資料整理\n確定申告振り返り会",
                "オンスケ",
                "\n→4/4 件完了\n\n→10/11 件完了\n→2/11 件完了",
                "原課様への説明資料は完了。", "問題なし");

        byte[] xlsm = new WorkReportXlsmExportAdapter().generate(month, "山田 太郎", List.of(), List.of(week1));

        try (XSSFWorkbook book = new XSSFWorkbook(new ByteArrayInputStream(xlsm))) {
            Sheet sheet = book.getSheet("10月");
            assertEquals("　問い合わせ対応サポート", text(sheet, "AR7"));
            assertEquals("→4/4 件完了", text(sheet, "AS7"));
            assertEquals("　問い合わせ対応", text(sheet, "AR8"));
            assertEquals("", text(sheet, "AS8"));
            assertEquals("　住民税スケジュール資料整理", text(sheet, "AR9"));
            assertEquals("→10/11 件完了", text(sheet, "AS9"));
            assertEquals("→2/11 件完了", text(sheet, "AS10"));
            assertEquals("問題点・対処：問題なし", text(sheet, "AS12"));
        }

        com.example.kintai.domain.model.attendance.WeeklyWorkReport imported = new WorkReportXlsmImportAdapter()
                .parse(new ByteArrayInputStream(xlsm)).stream()
                .filter(m -> m.getYearMonth().equals(YearMonth.of(2026, 10)))
                .findFirst().orElseThrow()
                .getWeeklyWorkReports().get(0);
        assertEquals("\n→4/4 件完了\n\n→10/11 件完了\n→2/11 件完了", imported.getProgressDetail());
        assertEquals("問い合わせ対応サポート\n問い合わせ対応\n住民税スケジュール資料整理\n確定申告振り返り会", imported.getProcessName());
        assertEquals("問題なし", imported.getProblemCountermeasure());
        assertEquals(List.of("", "→4/4 件完了", "", "→10/11 件完了", "→2/11 件完了", ""), imported.getProgressDetailLines());
    }

    /** 工程名・作業名等は改行ごとに週ブロックの3行目から下へ書き、取り込むと元の改行に戻る */
    @Test
    void 工程名は複数行で出力し_取り込むと元に戻る() throws Exception {
        MonthlyAttendanceDto month = new MonthlyAttendanceDto(YearMonth.of(2026, 11), List.of(), "0:00", 0, 0, 0, "山田 太郎");
        com.example.kintai.domain.model.attendance.WeeklyWorkReport week1 = new com.example.kintai.domain.model.attendance.WeeklyWorkReport(
                "126", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 4),
                "BCC", "住民情報システム", "確定申告システム\n帳票改修", null, null, null, null);

        byte[] xlsm = new WorkReportXlsmExportAdapter().generate(month, "山田 太郎", List.of(), List.of(week1));

        try (XSSFWorkbook book = new XSSFWorkbook(new ByteArrayInputStream(xlsm))) {
            Sheet sheet = book.getSheet("11月");
            assertEquals("BCC", text(sheet, "AR5"));
            assertEquals("・住民情報システム", text(sheet, "AR6"));
            assertEquals("　確定申告システム", text(sheet, "AR7"));
            assertEquals("　帳票改修", text(sheet, "AR8"));
        }

        com.example.kintai.domain.model.attendance.WeeklyWorkReport imported = new WorkReportXlsmImportAdapter()
                .parse(new ByteArrayInputStream(xlsm)).stream()
                .filter(m -> m.getYearMonth().equals(YearMonth.of(2026, 11)))
                .findFirst().orElseThrow()
                .getWeeklyWorkReports().get(0);
        assertEquals("確定申告システム\n帳票改修", imported.getProcessName());
        assertEquals("住民情報システム", imported.getProjectName());
    }

    /** 日をまたいだ退勤は「25:00」のように24時間を足して出力し、取り込むと翌日の退勤（1:00）に戻る */
    @Test
    void 日またぎの退勤は25時として出力し_取り込むと元の時刻に戻る() throws Exception {
        List<DailyAttendanceDto> days = List.of(day(2, "09:00", "01:00", 1), day(3, "09:00", "18:00", 1));
        MonthlyAttendanceDto month = new MonthlyAttendanceDto(YearMonth.of(2026, 11), days, "24:00", 1440, 0, 0, "山田 太郎");

        byte[] xlsm = new WorkReportXlsmExportAdapter().generate(month, "山田 太郎", List.of(workType(1, "出勤", false)), List.of());

        try (XSSFWorkbook book = new XSSFWorkbook(new ByteArrayInputStream(xlsm))) {
            Sheet sheet = book.getSheet("11月");
            // 2日はExcelの9行目。F列（退勤）は1日=1.0の時刻値
            assertEquals(25 * 60, Math.round(sheet.getRow(8).getCell(5).getNumericCellValue() * 1440));
            assertEquals(18 * 60, Math.round(sheet.getRow(9).getCell(5).getNumericCellValue() * 1440));
        }

        var importedDays = new WorkReportXlsmImportAdapter().parse(new ByteArrayInputStream(xlsm)).stream()
                .filter(m -> m.getYearMonth().equals(YearMonth.of(2026, 11)))
                .findFirst().orElseThrow()
                .getDays();
        var day2 = importedDays.stream().filter(d -> d.getDate().getDayOfMonth() == 2).findFirst().orElseThrow();
        assertEquals("09:00", day2.getStartTime());
        assertEquals("01:00", day2.getEndTime());
    }

    private static String text(Sheet sheet, String ref) {
        org.apache.poi.ss.util.CellReference cr = new org.apache.poi.ss.util.CellReference(ref);
        Row row = sheet.getRow(cr.getRow());
        Cell cell = row == null ? null : row.getCell(cr.getCol());
        return cell == null ? "" : cell.getStringCellValue();
    }

    /** 日の摘要欄（AN列。1日目はExcelの8行目） */
    private static String an(Sheet sheet, int dayOfMonth) {
        Row row = sheet.getRow(7 + dayOfMonth - 1);
        Cell cell = row == null ? null : row.getCell(39); // AN = 40列目
        return cell == null ? "" : cell.getStringCellValue();
    }
}
