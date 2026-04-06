package com.example.kintai.application.port.in;

import com.example.kintai.adapter.in.web.dto.MonthlyAttendanceDto;
import com.example.kintai.domain.model.employee.EmployeeId;

import java.time.YearMonth;

/**
 * 月次勤怠情報を取得するためのユースケース（内向きポート）。
 *
 * このインターフェースが、アプリケーションが提供する機能を定義します。
 */
public interface GetMonthlyAttendanceUseCase {

    /**
     * 月次勤怠情報を取得します。
     *
     * @param command ユースケースへの入力コマンド
     * @return 月次勤怠情報のDTO
     */
    MonthlyAttendanceDto getMonthlyAttendance(GetMonthlyAttendanceCommand command);

    /**
     * このユースケースを実行するために必要な情報をカプセル化するコマンドオブジェクト。
     */
    class GetMonthlyAttendanceCommand {
        private final EmployeeId employeeId;
        private final YearMonth yearMonth;

        public GetMonthlyAttendanceCommand(EmployeeId employeeId, YearMonth yearMonth) {
            this.employeeId = employeeId;
            this.yearMonth = yearMonth;
        }

        public EmployeeId getEmployeeId() {
            return employeeId;
        }

        public YearMonth getYearMonth() {
            return yearMonth;
        }
    }
}
