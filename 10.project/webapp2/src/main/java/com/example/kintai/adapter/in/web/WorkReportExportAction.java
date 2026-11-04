package com.example.kintai.adapter.in.web;

import com.example.kintai.application.port.in.ExportWorkReportUseCase;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;

/**
 * 月次勤怠データを社内所定の「作業報告書」Excel（.xlsm）として出力するアクション。
 */
public class WorkReportExportAction implements Action {

    private static final String CONTENT_TYPE = "application/vnd.ms-excel.sheet.macroEnabled.12";

    private final ExportWorkReportUseCase exportWorkReportUseCase;

    public WorkReportExportAction(ExportWorkReportUseCase exportWorkReportUseCase) {
        this.exportWorkReportUseCase = exportWorkReportUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String employeeId = session != null ? (String) session.getAttribute("loginUser") : null;
        if (employeeId == null) {
            return new View("/login.do", true);
        }

        int year = -1;
        int month = -1;
        try {
            year = Integer.parseInt(request.getParameter("year"));
            month = Integer.parseInt(request.getParameter("month"));
            YearMonth yearMonth = YearMonth.of(year, month);

            ExportWorkReportUseCase.ExportedWorkReport exported = exportWorkReportUseCase.exportMonth(new EmployeeId(employeeId), yearMonth);
            byte[] fileBytes = exported.getContent();
            if (exported.getArchiveError() != null) {
                // ダウンロードは続ける。次に勤怠画面を開いたときに知らせる
                session.setAttribute("kintaiErrorMessage",
                        "作業報告書はダウンロードしましたが、サーバーのフォルダに置けませんでした: " + exported.getArchiveError());
            }

            String fileName = String.format("作業報告書%d%02d.xlsm", year, month);
            String asciiFallbackFileName = String.format("work_report_%d%02d.xlsm", year, month);
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

            response.setContentType(CONTENT_TYPE);
            response.setContentLength(fileBytes.length);
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + asciiFallbackFileName + "\"; filename*=UTF-8''" + encodedFileName);
            response.getOutputStream().write(fileBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
            if (session != null) {
                session.setAttribute("kintaiErrorMessage", "作業報告書の出力に失敗しました: " + e.getMessage());
            }
            YearMonth fallback = (year > 0 && month > 0) ? YearMonth.of(year, month) : YearMonth.now();
            return new View("/kintaiDisplay.do?id=" + employeeId
                    + "&year=" + fallback.getYear() + "&month=" + fallback.getMonthValue(), true);
        }

        return null;
    }
}
