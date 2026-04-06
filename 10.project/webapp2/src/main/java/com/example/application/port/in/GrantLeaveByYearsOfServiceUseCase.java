package com.example.application.port.in;

/**
 * 勤続年数に応じて全社員に有給休暇を一括付与するユースケース。
 */
public interface GrantLeaveByYearsOfServiceUseCase {

    /**
     * 勤続年数に応じた有給休暇の付与を実行します。
     */
    void grantLeaveByYearsOfService();
}
