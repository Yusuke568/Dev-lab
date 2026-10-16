package com.example.kintai.adapter.in.web;

import com.example.kintai.application.port.in.ImportWorkReportUseCase;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.time.YearMonth;

/**
 * 社内所定の「作業報告書」Excel（.xlsm）をアップロードし、勤怠データ・週次作業報告を一括登録するアクション。
 */
public class WorkReportImportAction implements Action {

    private final ImportWorkReportUseCase importWorkReportUseCase;

    public WorkReportImportAction(ImportWorkReportUseCase importWorkReportUseCase) {
        this.importWorkReportUseCase = importWorkReportUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String employeeId = session != null ? (String) session.getAttribute("loginUser") : null;
        if (employeeId == null) {
            return new View("/login.do", true);
        }

        YearMonth currentMonth = YearMonth.now();
        String redirectPath = "/kintaiDisplay.do?id=" + employeeId
                + "&year=" + currentMonth.getYear() + "&month=" + currentMonth.getMonthValue();

        try {
            Part filePart = request.getPart("workReportFile");
            if (filePart == null || filePart.getSize() == 0) {
                session.setAttribute("kintaiErrorMessage", "アップロードするファイルを選択してください。");
                return new View(redirectPath, true);
            }

            try (InputStream in = filePart.getInputStream()) {
                int importedMonths = importWorkReportUseCase.importFile(new EmployeeId(employeeId), in);
                session.setAttribute("kintaiSuccessMessage", importedMonths + "ヶ月分のデータを取り込みました。");
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("kintaiErrorMessage", "作業報告書の取り込みに失敗しました: " + e.getMessage());
        }

        return new View(redirectPath, true);
    }
}
