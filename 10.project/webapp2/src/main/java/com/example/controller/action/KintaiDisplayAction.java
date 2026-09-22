package com.example.controller.action;

import com.example.kintai.adapter.in.web.dto.DailyAttendanceDto;
import com.example.kintai.adapter.in.web.dto.MonthlyAttendanceDto;
import com.example.kintai.application.port.in.GetCorrectionMastersUseCase;
import com.example.kintai.application.port.in.GetMonthlyAttendanceUseCase;
import com.example.kintai.application.port.in.GetMyOvertimeRequestsUseCase;
import com.example.application.port.in.GetWorkTypesUseCase;
import com.example.shared.web.Action;
import com.example.shared.web.View;
import com.example.kintai.domain.model.attendance.CorrectionMaster;
import com.example.kintai.domain.model.attendance.OvertimeRequest;
import com.example.kintai.domain.model.employee.EmployeeId;
import com.example.entity.WorkType;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 勤怠入力画面を表示するアクションクラス。（リファクタリング後！E
 * 責務：リクエストを解釈し、E��刁E��ユースケースを呼び出し、結果をビューに渡す、E
 */
public class KintaiDisplayAction implements Action {

    private final GetMonthlyAttendanceUseCase getMonthlyAttendanceUseCase;
    private final GetWorkTypesUseCase getWorkTypesUseCase;
    private final GetCorrectionMastersUseCase getCorrectionMastersUseCase;
    private final GetMyOvertimeRequestsUseCase getMyOvertimeRequestsUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * コンストラクタ、E
     * 依存性は外部のファクトリ�E�EIコンチE���E�から注入されます、E
     */
    public KintaiDisplayAction(GetMonthlyAttendanceUseCase getMonthlyAttendanceUseCase, GetWorkTypesUseCase getWorkTypesUseCase,
                                GetCorrectionMastersUseCase getCorrectionMastersUseCase, GetMyOvertimeRequestsUseCase getMyOvertimeRequestsUseCase) {
        this.getMonthlyAttendanceUseCase = getMonthlyAttendanceUseCase;
        this.getWorkTypesUseCase = getWorkTypesUseCase;
        this.getCorrectionMastersUseCase = getCorrectionMastersUseCase;
        this.getMyOvertimeRequestsUseCase = getMyOvertimeRequestsUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // 1. リクエストからコマンドを作�E
            String idParam = request.getParameter("id");
            String yearParam = request.getParameter("year");
            String monthParam = request.getParameter("month");

            // ログイン中のユーザー惁E��をセチE��ョンから取得する方が望ましいが、ここではパラメータを維持E
            EmployeeId employeeId = new EmployeeId(idParam);
            YearMonth yearMonth = YearMonth.of(Integer.parseInt(yearParam), Integer.parseInt(monthParam));
            
            GetMonthlyAttendanceUseCase.GetMonthlyAttendanceCommand command =
                    new GetMonthlyAttendanceUseCase.GetMonthlyAttendanceCommand(employeeId, yearMonth);

            // 2. ユースケースを実衁E
            MonthlyAttendanceDto attendanceData = getMonthlyAttendanceUseCase.getMonthlyAttendance(command);
            List<WorkType> workTypes = getWorkTypesUseCase.getWorkTypes();
            String workTypesJson = objectMapper.writeValueAsString(workTypes);
            List<CorrectionMaster> correctionMasters = getCorrectionMastersUseCase.getCorrectionMasters();

            // 残業事前申請の状況（ステータス通知用）：申請済みの対象日、申請中・却下の件数
            List<OvertimeRequest> myRequests = getMyOvertimeRequestsUseCase.getMyRequests(idParam);
            Set<LocalDate> requestedDates = myRequests.stream()
                    .map(OvertimeRequest::getTargetDate)
                    .collect(Collectors.toSet());
            List<LocalDate> datesNeedingRequest = attendanceData.getDailyRecords().stream()
                    .filter(d -> d.getOvertimeMinutes() > 0 && !requestedDates.contains(d.getDate()))
                    .map(DailyAttendanceDto::getDate)
                    .collect(Collectors.toList());
            long pendingRequestCount = myRequests.stream().filter(OvertimeRequest::isPending).count();
            long rejectedRequestCount = myRequests.stream().filter(OvertimeRequest::isRejected).count();

            // 3. 結果をリクエストスコープに設宁E
            request.setAttribute("attendanceData", attendanceData);
            request.setAttribute("workTypes", workTypes);
            request.setAttribute("workTypesJson", workTypesJson);
            request.setAttribute("correctionMasters", correctionMasters);
            request.setAttribute("datesNeedingRequest", datesNeedingRequest);
            request.setAttribute("pendingRequestCount", pendingRequestCount);
            request.setAttribute("rejectedRequestCount", rejectedRequestCount);
            request.setAttribute("staffId", idParam);
            
            // 4. ビューにフォワーチE
            return new View("/WEB-INF/view/kintai.jsp");

        } catch (Exception e) {
            // エラーハンドリングを強化することが望ましい
            e.printStackTrace();
            request.setAttribute("errorMessage", "勤怠惁E��の表示中にエラーが発生しました: " + e.getMessage());
            return new View("/WEB-INF/view/error.jsp");
        }
    }
}
