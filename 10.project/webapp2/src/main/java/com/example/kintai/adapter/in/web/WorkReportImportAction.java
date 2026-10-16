package com.example.kintai.adapter.in.web;

import com.example.kintai.application.port.in.ImportWorkReportUseCase;
import com.example.kintai.application.port.in.ImportWorkReportUseCase.ImportResult;
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
import java.util.List;
import java.util.stream.Collectors;

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
                ImportResult result = importWorkReportUseCase.importFile(new EmployeeId(employeeId), in);
                session.setAttribute("kintaiSuccessMessage", buildSuccessMessage(result));
                if (!result.getSkipped().isEmpty()) {
                    session.setAttribute("kintaiErrorMessage", buildSkippedMessage(result.getSkipped()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("kintaiErrorMessage", "作業報告書の取り込みに失敗しました: " + rootMessage(e));
        }

        return new View(redirectPath, true);
    }

    private String buildSuccessMessage(ImportResult result) {
        return result.getImportedMonthCount() + "ヶ月分のデータを取り込みました。";
    }

    private String buildSkippedMessage(List<String> skipped) {
        String detail = skipped.stream().limit(10).collect(Collectors.joining(" / "));
        String suffix = skipped.size() > 10 ? " 他" + (skipped.size() - 10) + "件" : "";
        return skipped.size() + "件をスキップしました（" + detail + suffix + "）。日またぎの退勤時刻など、"
                + "様式側の入力をご確認のうえ、必要であれば画面から手動で補ってください。";
    }

    /** TransactionManagerImpl等による汎用ラップメッセージではなく、根本原因のメッセージを拾う。 */
    private String rootMessage(Throwable e) {
        Throwable current = e;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return (message == null || message.isBlank())
                ? current.getClass().getSimpleName()
                : message;
    }
}
