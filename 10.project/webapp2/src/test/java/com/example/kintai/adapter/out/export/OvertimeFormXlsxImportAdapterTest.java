package com.example.kintai.adapter.out.export;

import com.example.kintai.domain.model.attendance.OvertimeFormApprovals;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 時間外申請書（Excel）の承認欄を読む */
class OvertimeFormXlsxImportAdapterTest {

    @Test
    void 様式の承認欄を読む_日付と横線と空欄() throws Exception {
        byte[] xlsx;
        try (InputStream in = getClass().getResourceAsStream("/templates/overtime_request_template.xlsx");
             XSSFWorkbook book = new XSSFWorkbook(in)) {
            Sheet sh = book.getSheetAt(0);
            CellStyle dateStyle = book.createCellStyle();
            dateStyle.setDataFormat(book.createDataFormat().getFormat("m/d"));
            set(sh, "C3", "浦山　純平");
            setDate(sh, "A10", LocalDate.of(2026, 11, 2), dateStyle);
            setDate(sh, "P10", LocalDate.of(2026, 11, 6), dateStyle);
            set(sh, "Q10", "-");
            set(sh, "R10", "－");
            setDate(sh, "AD10", LocalDate.of(2026, 11, 6), dateStyle);
            set(sh, "AE10", "-");
            setDate(sh, "A11", LocalDate.of(2026, 11, 4), dateStyle);
            set(sh, "P11", "2026/11/11");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            book.write(out);
            xlsx = out.toByteArray();
        }

        OvertimeFormApprovals form = new OvertimeFormXlsxImportAdapter().parse(new ByteArrayInputStream(xlsx));

        assertEquals("浦山　純平", form.staffName());
        OvertimeFormApprovals.Row first = form.rows().stream().filter(r -> r.date().equals(LocalDate.of(2026, 11, 2))).findFirst().orElseThrow();
        assertEquals(LocalDate.of(2026, 11, 6), first.planned().get(0).date());
        assertTrue(first.planned().get(1).skipped());
        assertTrue(first.planned().get(2).skipped());
        assertEquals(LocalDate.of(2026, 11, 6), first.actual().get(0).date());
        assertEquals("11/6 / － / 　", first.getActualLabel());
        OvertimeFormApprovals.Row second = form.rows().stream().filter(r -> r.date().equals(LocalDate.of(2026, 11, 4))).findFirst().orElseThrow();
        assertEquals(LocalDate.of(2026, 11, 11), second.planned().get(0).date());
        assertFalse(second.actual().get(0).hasDate());
    }

    private static org.apache.poi.ss.usermodel.Cell cell(Sheet sh, String ref) {
        org.apache.poi.ss.util.CellReference r = new org.apache.poi.ss.util.CellReference(ref);
        org.apache.poi.ss.usermodel.Row row = sh.getRow(r.getRow());
        if (row == null) row = sh.createRow(r.getRow());
        org.apache.poi.ss.usermodel.Cell c = row.getCell(r.getCol());
        return c != null ? c : row.createCell(r.getCol());
    }

    private static void set(Sheet sh, String ref, String value) {
        cell(sh, ref).setCellValue(value);
    }

    private static void setDate(Sheet sh, String ref, LocalDate date, CellStyle style) {
        org.apache.poi.ss.usermodel.Cell c = cell(sh, ref);
        c.setCellValue(date);
        c.setCellStyle(style);
    }
}
