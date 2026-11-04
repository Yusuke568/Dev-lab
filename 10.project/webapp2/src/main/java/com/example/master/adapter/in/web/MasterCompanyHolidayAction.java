package com.example.master.adapter.in.web;

import com.example.kintai.domain.service.JapaneseHolidayCalendar;
import com.example.master.application.port.in.MasterMaintenanceUseCase;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;

/**
 * 会社休日（年末年始・創立記念日など）の一覧・追加・削除。年ごとに表示する。
 */
public class MasterCompanyHolidayAction extends MasterActionSupport {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy/M/d");

    public MasterCompanyHolidayAction(MasterMaintenanceUseCase masterUseCase) {
        super(masterUseCase);
    }

    @Override
    protected String selfPath() {
        return "/masterCompanyHoliday.do";
    }

    /** 保存した後も、表示していた年の一覧に戻る。 */
    @Override
    protected String redirectPath(HttpServletRequest request) {
        return selfPath() + "?year=" + year(request);
    }

    @Override
    protected String show(HttpServletRequest request) {
        int year = year(request);
        request.setAttribute("year", year);
        request.setAttribute("holidays", masterUseCase.getCompanyHolidays(year));
        // 参考に、その年の国民の祝日も表示する（祝日は自動で計算されるので登録しなくてよい）
        request.setAttribute("nationalHolidays", JapaneseHolidayCalendar.holidaysOf(year));
        return "/WEB-INF/view/master/master_company_holiday.jsp";
    }

    @Override
    protected String save(HttpServletRequest request) {
        if ("delete".equals(request.getParameter("mode"))) {
            LocalDate date = LocalDate.parse(request.getParameter("date"));
            masterUseCase.deleteCompanyHoliday(date);
            return date.format(DATE) + " を会社休日から外しました。";
        }
        LocalDate from = parseDate(request.getParameter("from"));
        LocalDate to = parseDate(request.getParameter("to"));
        int count = masterUseCase.addCompanyHolidays(from, to, request.getParameter("name"));
        return count == 1 ? from.format(DATE) + " を会社休日に登録しました。"
                : from.format(DATE) + "〜" + to.format(DATE) + " の" + count + "日を会社休日に登録しました。";
    }

    /** 表示する年（なければ今年。登録・削除した日の年を優先）。 */
    private static int year(HttpServletRequest request) {
        for (String name : new String[] {"from", "date"}) {
            LocalDate date = parseDate(request.getParameter(name));
            if (date != null) {
                return date.getYear();
            }
        }
        String value = request.getParameter("year");
        if (value != null && value.matches("\\d{4}")) {
            return Integer.parseInt(value);
        }
        return Year.now().getValue();
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("日付の形式が正しくありません。");
        }
    }
}
