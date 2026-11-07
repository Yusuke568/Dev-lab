package com.example.leave.application.service;

import com.example.leave.application.port.in.*;
import com.example.leave.domain.model.LeaveGrantRule;
import com.example.leave.domain.port.out.PaidLeavePort;
import com.example.shain.domain.port.out.ShainPort;
import com.example.shain.domain.model.Shain;
import com.example.shain.domain.model.ShainId;
import com.example.shared.transaction.TransactionManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    public void updatePaidLeaveDays(ShainId shainId, double days) {
        if (days < 0 || days * 2 != Math.floor(days * 2)) {
            throw new IllegalArgumentException("有給残日数は 0 以上・0.5 日単位で入力してください。");
        }
        paidLeavePort.updateDays(shainId.getIntValue(), days);
    }

    @Override
    public int grantLeaveByYearsOfService() {
        return transactionManager.executeInTransaction(() -> {
            List<LeaveGrantRule> rules = paidLeavePort.findAllRules();
            int granted = 0;
            for (Shain shain : shainPort.findAll()) {
                Integer days = grantDaysFor(shain, rules);
                if (days != null && days > 0) {
                    paidLeavePort.updateDays(shain.getId().getIntValue(), shain.getPaidLeaveDays() + days);
                    granted++;
                }
            }
            return granted;
        });
    }

    @Override
    public Map<Integer, Integer> previewGrantDays() {
        List<LeaveGrantRule> rules = paidLeavePort.findAllRules();
        Map<Integer, Integer> result = new LinkedHashMap<>();
        for (Shain shain : shainPort.findAll()) {
            Integer days = grantDaysFor(shain, rules);
            if (days != null && days > 0) {
                result.put(shain.getId().getIntValue(), days);
            }
        }
        return result;
    }

    /** 入社年が未設定の社員には付与しない（勤続年数が分からないため） */
    private static Integer grantDaysFor(Shain shain, List<LeaveGrantRule> rules) {
        return shain.getEntryYear() > 0 ? grantDaysFor(shain.getYearsOfService(), rules) : null;
    }

    /**
     * 勤続年数に合うルールの付与日数。勤続年数以下で一番長いルールを使う
     * （ルールが 0〜6年までなら、7年以上の人には6年のルールを使う）。合うルールがなければ null
     */
    static Integer grantDaysFor(int yearsOfService, List<LeaveGrantRule> rules) {
        LeaveGrantRule best = null;
        for (LeaveGrantRule rule : rules) {
            if (rule.getYearsOfService() <= yearsOfService
                    && (best == null || rule.getYearsOfService() > best.getYearsOfService())) {
                best = rule;
            }
        }
        return best == null ? null : best.getGrantDays();
    }

    @Override
    public void addLeaveDaysToEmployees(List<ShainId> shainIds, int days) {
        transactionManager.executeInTransaction(() -> {
            for (ShainId id : shainIds) {
                shainPort.findById(id).ifPresent(shain -> {
                    double newDays = shain.getPaidLeaveDays() + days;
                    paidLeavePort.updateDays(id.getIntValue(), newDays);
                });
            }
        });
    }
}
