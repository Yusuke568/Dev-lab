package com.example.application.service;

import com.example.application.port.in.GrantLeaveByYearsOfServiceUseCase;
import com.example.application.port.out.LeaveGrantRulePort;
import com.example.application.port.out.ShainPort;
import com.example.application.port.out.TransactionManager;
import com.example.entity.LeaveGrantRule;
import com.example.entity.Shain;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 勤続年数に応じた有給休暇の一括付与ユースケースの実装。
 */
public class GrantLeaveByYearsOfServiceService implements GrantLeaveByYearsOfServiceUseCase {

    private final ShainPort shainPort;
    private final LeaveGrantRulePort leaveGrantRulePort;
    private final TransactionManager transactionManager;

    public GrantLeaveByYearsOfServiceService(ShainPort shainPort, LeaveGrantRulePort leaveGrantRulePort, TransactionManager transactionManager) {
        this.shainPort = shainPort;
        this.leaveGrantRulePort = leaveGrantRulePort;
        this.transactionManager = transactionManager;
    }

    @Override
    public void grantLeaveByYearsOfService() {
        // TODO: この実装は非効率です (N+1アップデート)。
        // 将来的にバッチアップデートを使用するようにリファクタリングするべきです。

        transactionManager.executeInTransaction(() -> {
            List<LeaveGrantRule> rules = leaveGrantRulePort.findAll();
            Map<Integer, Integer> grantMap = rules.stream()
                    .collect(Collectors.toMap(LeaveGrantRule::getYearsOfService, LeaveGrantRule::getGrantDays));

            List<Shain> allShain = shainPort.findAll();
            int currentYear = LocalDate.now().getYear();

            for (Shain shain : allShain) {
                // 勤続年数を計算
                int yearsOfService = currentYear - shain.getEntryyear();
                shain.setYearsOfService(yearsOfService); // 必要に応じて更新

                // 適用されるべきルールを見つける (勤続年数以下で最大のルール)
                int grantDays = 0;
                for (int i = yearsOfService; i >= 0; i--) {
                    if (grantMap.containsKey(i)) {
                        grantDays = grantMap.get(i);
                        break; // 最も近いルールが見つかったのでループを抜ける
                    }
                }

                // 有給を付与（ここではルールの日数をそのまま設定する）
                // 注: 「付与」のロジックは要件次第（加算 or 設定）。ここでは設定とする。
                shain.setPaidLeaveDays(grantDays);
                shainPort.update(shain);
            }
        });
    }
}
