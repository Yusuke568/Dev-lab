package com.example.leave.domain.port.out;

import com.example.leave.domain.model.LeaveGrantRule;
import java.util.List;

/**
 * 有給休暇関連の永続化操作を定義するポート。
 */
public interface PaidLeavePort {
    void incrementDays(int staffId);
    void decrementDays(int staffId);
    List<LeaveGrantRule> findAllRules();
    void updateDays(int staffId, int days);
}
