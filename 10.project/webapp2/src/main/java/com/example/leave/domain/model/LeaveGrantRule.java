package com.example.leave.domain.model;

/**
 * 有給休暇付与ルールを表すドメインモデル。
 */
public class LeaveGrantRule {
    private final int yearsOfService;
    private final int grantDays;

    public LeaveGrantRule(int yearsOfService, int grantDays) {
        this.yearsOfService = yearsOfService;
        this.grantDays = grantDays;
    }

    public int getYearsOfService() {
        return yearsOfService;
    }

    public int getGrantDays() {
        return grantDays;
    }
}
