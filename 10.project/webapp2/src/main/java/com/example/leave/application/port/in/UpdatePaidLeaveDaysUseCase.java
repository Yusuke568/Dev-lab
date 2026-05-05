package com.example.leave.application.port.in;

import com.example.shain.domain.model.ShainId;

/**
 * 有給日数を更新するユースケース。
 */
public interface UpdatePaidLeaveDaysUseCase {
    void updatePaidLeaveDays(ShainId shainId, int days);
}
