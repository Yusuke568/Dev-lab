package com.example.leave.application.service;

import com.example.leave.application.port.in.*;
import com.example.leave.domain.model.LeaveGrantRule;
import com.example.leave.domain.port.out.PaidLeavePort;
import com.example.shain.domain.port.out.ShainPort;
import com.example.shain.domain.model.Shain;
import com.example.shain.domain.model.ShainId;
import com.example.application.port.out.TransactionManager;

import java.util.List;

/**
 * 有給休暇管理のユースケース実装クラス。
 */
public class PaidLeaveService implements 
    UpdatePaidLeaveDaysUseCase, 
    GrantLeaveByYearsOfServiceUseCase, 
    AddLeaveDaysToEmployeesUseCase {

    private final PaidLeavePort paidLeavePort;
    private final ShainPort shainPort;
    private final TransactionManager transactionManager;

    public PaidLeaveService(PaidLeavePort paidLeavePort, ShainPort shainPort, TransactionManager transactionManager) {
        this.paidLeavePort = paidLeavePort;
        this.shainPort = shainPort;
        this.transactionManager = transactionManager;
    }

    @Override
    public void updatePaidLeaveDays(ShainId shainId, int days) {
        paidLeavePort.updateDays(shainId.getIntValue(), days);
    }

    @Override
    public void grantLeaveByYearsOfService() {
        transactionManager.executeInTransaction(() -> {
            List<Shain> shains = shainPort.findAll();
            List<LeaveGrantRule> rules = paidLeavePort.findAllRules();

            for (Shain shain : shains) {
                for (LeaveGrantRule rule : rules) {
                    if (shain.getYearsOfService() == rule.getYearsOfService()) {
                        int newDays = shain.getPaidLeaveDays() + rule.getGrantDays();
                        paidLeavePort.updateDays(shain.getId().getIntValue(), newDays);
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void addLeaveDaysToEmployees(List<ShainId> shainIds, int days) {
        transactionManager.executeInTransaction(() -> {
            for (ShainId id : shainIds) {
                shainPort.findById(id).ifPresent(shain -> {
                    int newDays = shain.getPaidLeaveDays() + days;
                    paidLeavePort.updateDays(id.getIntValue(), newDays);
                });
            }
        });
    }
}
