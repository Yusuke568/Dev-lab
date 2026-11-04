package com.example.leave.domain.port.out;

import com.example.leave.domain.model.LeaveGrantRule;
import java.util.List;

/**
 * 有給休暇関連の永続化操作を定義するポート。
 */
public interface PaidLeavePort {
    /** 有給残日数を増減する（減らすときは負の値。半休は 0.5 日）。 */
    void adjustDays(int staffId, double delta);
    List<LeaveGrantRule> findAllRules();
    void updateDays(int staffId, double days);
}
