package com.example.leave.application.port.in;

import com.example.shain.domain.model.ShainId;

/**
 * 有給日数を更新するユースケース。
 */
public interface UpdatePaidLeaveDaysUseCase {
    /** 有給残日数を指定した日数にする（0.5 日単位）。 */
    void updatePaidLeaveDays(ShainId shainId, double days);
}
