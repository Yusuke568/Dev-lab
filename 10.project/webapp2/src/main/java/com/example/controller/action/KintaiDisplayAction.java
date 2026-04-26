package com.example.controller.action;

import com.example.kintai.adapter.in.web.dto.MonthlyAttendanceDto;
import com.example.kintai.application.port.in.GetMonthlyAttendanceUseCase;
import com.example.application.port.in.GetWorkTypesUseCase;
import com.example.controller.Action;
import com.example.controller.View;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.entity.WorkType;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.YearMonth;
import java.util.List;

/**
 * 勤怠入力画面を表示するアクションクラス。（リファクタリング後）
 * 責務：リクエストを解釈し、適切なユースケースを呼び出し、結果をビューに渡す。
 */
public class KintaiDisplayAction implements Action {

    private final GetMonthlyAttendanceUseCase getMonthlyAttendanceUseCase;
    private final GetWorkTypesUseCase getWorkTypesUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * コンストラクタ。
     * 依存性は外部のファクトリ（DIコンテナ）から注入されます。
     */
    public KintaiDisplayAction(GetMonthlyAttendanceUseCase getMonthlyAttendanceUseCase, GetWorkTypesUseCase getWorkTypesUseCase) {
        this.getMonthlyAttendanceUseCase = getMonthlyAttendanceUseCase;
        this.getWorkTypesUseCase = getWorkTypesUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // 1. リクエストからコマンドを作成
            String idParam = request.getParameter("id");
            String yearParam = request.getParameter("year");
            String monthParam = request.getParameter("month");

            // ログイン中のユーザー情報をセッションから取得する方が望ましいが、ここではパラメータを維持
            EmployeeId employeeId = new EmployeeId(idParam);
            YearMonth yearMonth = YearMonth.of(Integer.parseInt(yearParam), Integer.parseInt(monthParam));
            
            GetMonthlyAttendanceUseCase.GetMonthlyAttendanceCommand command =
                    new GetMonthlyAttendanceUseCase.GetMonthlyAttendanceCommand(employeeId, yearMonth);

            // 2. ユースケースを実行
            MonthlyAttendanceDto attendanceData = getMonthlyAttendanceUseCase.getMonthlyAttendance(command);
            List<WorkType> workTypes = getWorkTypesUseCase.getWorkTypes();
            String workTypesJson = objectMapper.writeValueAsString(workTypes);

            // 3. 結果をリクエストスコープに設定
            request.setAttribute("attendanceData", attendanceData);
            request.setAttribute("workTypes", workTypes);
            request.setAttribute("workTypesJson", workTypesJson);
            request.setAttribute("staffId", idParam);
            
            // 4. ビューにフォワード
            return new View("/WEB-INF/view/kintai.jsp");

        } catch (Exception e) {
            // エラーハンドリングを強化することが望ましい
            e.printStackTrace();
            request.setAttribute("errorMessage", "勤怠情報の表示中にエラーが発生しました: " + e.getMessage());
            return new View("/WEB-INF/view/error.jsp");
        }
    }
}
