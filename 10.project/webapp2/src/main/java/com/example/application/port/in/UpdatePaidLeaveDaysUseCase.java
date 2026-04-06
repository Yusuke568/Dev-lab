package com.example.application.port.in;

/**
 * 社員の有給日数を更新するためのユースケース。
 */
public interface UpdatePaidLeaveDaysUseCase {

    /**
     * @param shainId 社員ID
     * @param days 新しい有給日数
     */
    void updatePaidLeaveDays(int shainId, int days);
}
