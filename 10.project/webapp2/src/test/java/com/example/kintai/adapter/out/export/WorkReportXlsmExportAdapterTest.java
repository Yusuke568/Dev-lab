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

    /** 日の摘要欄（AN列。1日目はExcelの8行目） */
    private static String an(Sheet sheet, int dayOfMonth) {
        Row row = sheet.getRow(7 + dayOfMonth - 1);
        Cell cell = row == null ? null : row.getCell(39); // AN = 40列目
        return cell == null ? "" : cell.getStringCellValue();
    }
}
