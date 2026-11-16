package com.example.kintai.adapter.in.web;

import com.example.kintai.application.port.in.AttendanceAccessUseCase;
import com.example.kintai.application.port.in.ExportWorkReportUseCase;
import com.example.kintai.domain.model.attendance.Subordinate;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.shared.web.Action;
import com.example.shared.web.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 部下の勤怠表で選んだ社員の作業報告書（.xlsm）を、1つの ZIP にまとめて出す（APPROVAL 権限・POST のみ）。
 * 出せるのは、部下の勤怠表に出る社員（組織図で自分の配下と自分）だけ。
 * 1人分は本人が勤怠画面から出す作業報告書と同じ（サーバーのフォルダが設定されていれば、そこにも置く）。
 */
public class SubordinateWorkReportExportAction implements Action {

    private static final Logger LOG = Logger.getLogger(SubordinateWorkReportExportAction.class.getName());
    /** 一度に出せる人数の上限（サーバーの負荷を抑える） */
    static final int MAX_STAFF = 100;

    private final AttendanceAccessUseCase attendanceAccessUseCase;
    private final ExportWorkReportUseCase exportWorkReportUseCase;

    public SubordinateWorkReportExportAction(AttendanceAccessUseCase attendanceAccessUseCase,
                                             ExportWorkReportUseCase exportWorkReportUseCase) {
        this.attendanceAccessUseCase = attendanceAccessUseCase;
        this.exportWorkReportUseCase = exportWorkReportUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String loginUser = session != null ? (String) session.getAttribute("loginUser") : null;
        if (loginUser == null) {
            return new View("/login.do", true);
        }
        @SuppressWarnings("unchecked")
        Set<String> features = (Set<String>) session.getAttribute("features");
        YearMonth month;
        try {
            month = YearMonth.of(Integer.parseInt(request.getParameter("year")), Integer.parseInt(request.getParameter("month")));
        } catch (RuntimeException e) {
            month = YearMonth.now();
        }
        View back = new View("/subordinateAttendance.do?ym=" + month, true);

        String[] ids = request.getParameterValues("id");
        Set<String> selected = new LinkedHashSet<>();
        if (ids != null) {
            for (String id : ids) {
                if (id != null && !id.isBlank()) selected.add(id.trim());
            }
        }
        if (selected.isEmpty()) {
            session.setAttribute("subordinateMessage", "作業報告書を出す社員を選んでください。");
            return back;
        }
        if (selected.size() > MAX_STAFF) {
            session.setAttribute("subordinateMessage", "一度に出せるのは " + MAX_STAFF + " 人までです。");
            return back;
        }

        try {
            Map<String, Subordinate> mine = new LinkedHashMap<>();
            for (Subordinate s : attendanceAccessUseCase.getSubordinates(loginUser, features)) {
                mine.put(s.getStaffId(), s);
            }
            List<String> problems = new ArrayList<>();
            Map<String, byte[]> files = new LinkedHashMap<>();
            for (String id : selected) {
                Subordinate staff = mine.get(id);
                if (staff == null) {
                    problems.add("社員ID " + id + "：部下ではないため出せません。");
                    continue;
                }
                try {
                    ExportWorkReportUseCase.ExportedWorkReport exported =
                            exportWorkReportUseCase.exportMonth(new EmployeeId(id), month);
                    files.put(fileName(month, staff, files.keySet()), exported.getContent());
                    if (exported.getArchiveError() != null) {
                        problems.add(staff.getName() + "（社員ID " + id + "）：サーバーのフォルダに置けませんでした（" + exported.getArchiveError() + "）。");
                    }
                } catch (RuntimeException e) {
                    LOG.log(Level.WARNING, "作業報告書を作れませんでした: staff=" + id + " month=" + month, e);
                    problems.add(staff.getName() + "（社員ID " + id + "）：作業報告書を作れませんでした。");
                }
            }
            if (files.isEmpty()) {
                session.setAttribute("subordinateMessage", "作業報告書を出せませんでした。" + String.join(" ", problems));
                return back;
            }

            byte[] zip = zip(files, problems);
            String zipName = String.format("作業報告書_%d%02d_%d人.zip", month.getYear(), month.getMonthValue(), files.size());
            String ascii = String.format("work_reports_%d%02d.zip", month.getYear(), month.getMonthValue());
            String encoded = URLEncoder.encode(zipName, StandardCharsets.UTF_8).replace("+", "%20");
            response.setContentType("application/zip");
            response.setContentLength(zip.length);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + ascii + "\"; filename*=UTF-8''" + encoded);
            response.getOutputStream().write(zip);
            response.getOutputStream().flush();
            return null;
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "作業報告書をまとめて出せませんでした: month=" + month, e);
            session.setAttribute("subordinateMessage", com.example.shared.error.UserMessages.of(e,
                    "作業報告書をまとめて出せませんでした。時間をおいてもう一度お試しください。"));
            return back;
        }
    }

    /** ZIP の中のファイル名。「作業報告書202611_123_山田太郎.xlsm」。同じ名前が続けば番号を付ける */
    static String fileName(YearMonth month, Subordinate staff, Set<String> used) {
        String name = staff.getName() == null ? "" : staff.getName().replaceAll("[\\s　\\\\/:*?\"<>|]", "");
        String base = String.format("作業報告書%d%02d_%s%s", month.getYear(), month.getMonthValue(), staff.getStaffId(),
                name.isEmpty() ? "" : "_" + name);
        String candidate = base + ".xlsm";
        for (int i = 2; used.contains(candidate); i++) {
            candidate = base + "(" + i + ").xlsm";
        }
        return candidate;
    }

    /** 作業報告書を ZIP にまとめる。出せなかった人がいれば「出せなかった社員.txt」も入れる */
    static byte[] zip(Map<String, byte[]> files, List<String> problems) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
            for (Map.Entry<String, byte[]> f : files.entrySet()) {
                zip.putNextEntry(new ZipEntry(f.getKey()));
                zip.write(f.getValue());
                zip.closeEntry();
            }
            if (!problems.isEmpty()) {
                zip.putNextEntry(new ZipEntry("出せなかった社員.txt"));
                // メモ帳で文字化けしないよう BOM 付き UTF-8・CRLF にする
                zip.write(new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
                zip.write((String.join("\r\n", problems) + "\r\n").getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return out.toByteArray();
    }
}
