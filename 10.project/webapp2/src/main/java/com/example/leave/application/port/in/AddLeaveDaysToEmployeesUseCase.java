package com.example.leave.application.port.in;

import com.example.shain.domain.model.ShainId;
import java.util.List;

/**
 * 選択した社員に有給を一括付与するユースケース。
 */
public interface AddLeaveDaysToEmployeesUseCase {
    void addLeaveDaysToEmployees(List<ShainId> shainIds, int days);
}
