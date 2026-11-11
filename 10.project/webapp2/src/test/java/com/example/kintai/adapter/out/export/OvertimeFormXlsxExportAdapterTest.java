package com.example.kintai.adapter.out.export;

import com.example.kintai.domain.port.out.OvertimeFormExportPort.FormRow;
import com.example.kintai.domain.port.out.OvertimeFormExportPort.OvertimeForm;
import com.example.kintai.domain.service.OvertimeFormCalculator;
import com.example.kintai.domain.service.OvertimeFormCalculator.ApprovalMark;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 時間外申請書のテンプレートに書き込み、様式の式で出る時間がアプリの時間と合うか */
class OvertimeFormXlsxExportAdapterTest {

    private static int t(int h, int m) {
        return h * 60 + m;
    }

    @Test
    void 様式に書き込み_式で出る実績の合計が勤怠表の時間外と合う() throws Exception {
        LocalDate d1 = LocalDate.of(2026, 10, 7);
        LocalDate d2 = LocalDate.of(2026, 10, 8);
        LocalDate approved = LocalDate.of(2026, 10, 6);
        List<ApprovalMark> marks = OvertimeFormCalculator.approvals(
                List.of(new OvertimeFormCalculator.Step(1, "課長A", true, approved)), null);
        FormRow r1 = new FormRow(d1, "税保守",
                OvertimeFormCalculator.planned(null, t(22, 0), t(4, 0)), "④客先都合", marks,
                OvertimeFormCalculator.actual(t(9, 0), t(22, 0), false, t(4, 0)), "リリース対応");
        FormRow r2 = new FormRow(d2, "週報",
                OvertimeFormCalculator.planned(t(7, 0), t(20, 0), t(4, 0)), "⑤その他", marks,
                OvertimeFormCalculator.actual(t(7, 0), t(20, 30), false, t(4, 0)), "週報纏め");
        OvertimeForm form = new OvertimeForm(YearMonth.of(2026, 10), "第二システム部 第一開発課", "山田　太郎",
                "課長A", "部長B", List.of(r1, r2));

        byte[] bytes = new OvertimeFormXlsxExportAdapter().write(form);

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals(1, wb.getNumberOfSheets());
            Sheet sh = wb.getSheetAt(0);
            FormulaEvaluator ev = wb.getCreationHelper().createFormulaEvaluator();
            assertEquals("山田　太郎", text(sh, "C3"));
            assertEquals("課長A", text(sh, "I3"));
            assertEquals("部長B", text(sh, "I4"));
            assertEquals("税保守", text(sh, "C10"));
            assertEquals("④客先都合", text(sh, "O10"));
            assertEquals("－", text(sh, "Q10"));
            assertEquals(approved, sh.getRow(9).getCell(new CellReference("P10").getCol()).getLocalDateTimeCellValue().toLocalDate());
            assertEquals(approved, sh.getRow(9).getCell(new CellReference("AD10").getCol()).getLocalDateTimeCellValue().toLocalDate());
            assertEquals("リリース対応", text(sh, "AG10"));

            // 申請：18:00〜22:00 → 様式の式では休憩を除いて 3:30
            assertEquals(t(3, 30), minutes(ev, sh, "L10"));
            // 実績：勤怠表の時間外 4:00 と合う
            assertEquals(t(4, 0), minutes(ev, sh, "AA10"));
            // 早出と残業の両方：申請は 18:00〜20:00（1:30）＋ 早出 7:00〜9:00（1:30）
            assertEquals(t(3, 0), minutes(ev, sh, "L11"));
            assertEquals(t(4, 0), minutes(ev, sh, "AA11"));
            // 当月累計
            assertEquals(t(8, 0), minutes(ev, sh, "AC11"));
        }
    }

    private static String text(Sheet sh, String ref) {
        CellReference cr = new CellReference(ref);
        Cell c = sh.getRow(cr.getRow()).getCell(cr.getCol());
        return c == null ? null : c.getStringCellValue();
    }

    private static int minutes(FormulaEvaluator ev, Sheet sh, String ref) {
        CellReference cr = new CellReference(ref);
        Cell c = sh.getRow(cr.getRow()).getCell(cr.getCol());
        double v = c.getCellType() == CellType.FORMULA ? ev.evaluate(c).getNumberValue() : c.getNumericCellValue();
        return (int) Math.round(v * 1440);
    }
}
