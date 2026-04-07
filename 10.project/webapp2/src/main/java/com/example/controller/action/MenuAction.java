package com.example.controller.action;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;

import com.example.controller.Action;
import com.example.controller.View;
import com.example.kintai.adapter.in.web.dto.DailyAttendanceDto;
import com.example.kintai.adapter.in.web.dto.MonthlyAttendanceDto;
import com.example.kintai.application.port.in.GetMonthlyAttendanceUseCase;
import com.example.kintai.domain.model.employee.EmployeeId;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * メニュー画面表示のアクションクラス。
 */
public class MenuAction implements Action {

    private final GetMonthlyAttendanceUseCase getMonthlyAttendanceUseCase;

    public MenuAction(GetMonthlyAttendanceUseCase getMonthlyAttendanceUseCase) {
        this.getMonthlyAttendanceUseCase = getMonthlyAttendanceUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loginUser") != null) {
            String loginUser = (String) session.getAttribute("loginUser");
            EmployeeId employeeId = new EmployeeId(loginUser);
            YearMonth currentMonth = YearMonth.now();
            
            try {
                GetMonthlyAttendanceUseCase.GetMonthlyAttendanceCommand command = 
                    new GetMonthlyAttendanceUseCase.GetMonthlyAttendanceCommand(employeeId, currentMonth);
                MonthlyAttendanceDto dto = getMonthlyAttendanceUseCase.getMonthlyAttendance(command);
                
                long missingCount = 0;
                LocalDate today = LocalDate.now();
                
                if (dto != null && dto.getDailyRecords() != null) {
                    for (DailyAttendanceDto daily : dto.getDailyRecords()) {
                        if (daily.getDate().isBefore(today)) {
                            boolean isWeekend = daily.getDate().getDayOfWeek().getValue() >= 6;
                            boolean hasData = !"-".equals(daily.getStartTime()) && !"-".equals(daily.getEndTime());
                            boolean isHoliday = daily.getAbstractId() != null && daily.getAbstractId() > 0; // Simplified check
                            
                            if (!isWeekend && !hasData && !isHoliday) {
                                missingCount++;
                            }
                        }
                    }
                }
                
                if (missingCount > 0) {
                    request.setAttribute("alertMessage", "未入力の勤怠が " + missingCount + " 件あります。");
                }
            } catch (Exception e) {
                // Ignore errors for menu alert
            }
        }
        
        // menu.jspへフォワードするためのViewオブジェクトを返す
        return new View("/WEB-INF/view/menu.jsp");
    }
}
