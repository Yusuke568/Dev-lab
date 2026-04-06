package com.example.application.port.in;

/**
 * 選択された複数の社員に有給休暇日数を加算するユースケース。
 */
public interface AddLeaveDaysToEmployeesUseCase {

    /**
     * @param employeeIds 対象の社員IDの配列
     * @param daysToAdd 加算する日数
     */
    void addLeaveDaysToEmployees(String[] employeeIds, int daysToAdd);
}
