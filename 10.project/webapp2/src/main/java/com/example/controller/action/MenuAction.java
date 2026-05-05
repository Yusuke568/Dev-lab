package com.example.controller.action;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.example.adapter.out.persistence.ConnectionBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.example.adapter.in.web.Action;
import com.example.adapter.in.web.View;
import com.example.kintai.adapter.in.web.dto.DailyAttendanceDto;
import com.example.kintai.adapter.in.web.dto.MonthlyAttendanceDto;
import com.example.kintai.application.port.in.GetMonthlyAttendanceUseCase;
import com.example.kintai.domain.model.employee.EmployeeId;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * メニュー画面表示のアクションクラス、E
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
                // ログインユーザーの過去の勤怠月リストを取征E
                List<YearMonth> availableMonths = new ArrayList<>();
                try (Connection con = ConnectionBase.getConnection();
                     PreparedStatement ps = con.prepareStatement(
                             "SELECT DISTINCT YEAR(WORK_DATE) AS y, MONTH(WORK_DATE) AS m " +
                             "FROM work_month_table " +
                             "WHERE STAFF_ID = ? AND WORK_DATE <= CURRENT_DATE() " +
                             "ORDER BY y DESC, m DESC")) {
                    ps.setInt(1, Integer.parseInt(loginUser));
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            availableMonths.add(YearMonth.of(rs.getInt("y"), rs.getInt("m")));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // 今月が含まれてぁE��ければ追加
                if (!availableMonths.contains(currentMonth)) {
                    availableMonths.add(currentMonth);
                }
                Collections.sort(availableMonths, Collections.reverseOrder());
                request.setAttribute("availableMonths", availableMonths);
                request.setAttribute("currentMonth", currentMonth);

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
                    request.setAttribute("alertMessage", "未入力�E勤怠ぁE" + missingCount + " 件あります、E);
                }
            } catch (Exception e) {
                // Ignore errors for menu alert
            }
        }
        
        // menu.jspへフォワードするため�EViewオブジェクトを返す
        return new View("/WEB-INF/view/menu.jsp");
    }
}
