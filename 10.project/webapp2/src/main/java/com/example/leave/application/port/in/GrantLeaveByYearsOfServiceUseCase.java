package com.example.leave.application.port.in;

import java.util.Map;

/**
 * 勤続年数に応じて有給を付与するユースケース。
 */
public interface GrantLeaveByYearsOfServiceUseCase {

    /**
     * 全社員に、勤続年数に合う付与ルールの日数を足す。
     * @return 付与した社員の数
     */
    int grantLeaveByYearsOfService();

    /** 一括付与したら社員ごとに何日足すか（社員ID → 日数。付与しない社員は入らない）。画面の確認用 */
    Map<Integer, Integer> previewGrantDays();
}
