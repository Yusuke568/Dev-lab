package com.example.application.service;

import com.example.application.port.in.AddLeaveDaysToEmployeesUseCase;
import com.example.application.port.out.ShainPort;
import com.example.application.port.out.TransactionManager;
import com.example.entity.Shain;

import java.util.Optional;

/**
 * 選択された複数の社員に有給休暇日数を加算するユースケースの実装。
 */
public class AddLeaveDaysToEmployeesService implements AddLeaveDaysToEmployeesUseCase {

    private final ShainPort shainPort;
    private final TransactionManager transactionManager;

    public AddLeaveDaysToEmployeesService(ShainPort shainPort, TransactionManager transactionManager) {
        this.shainPort = shainPort;
        this.transactionManager = transactionManager;
    }

    @Override
    public void addLeaveDaysToEmployees(String[] employeeIds, int daysToAdd) {
        // TODO: この実装は非効率です (N+1 クエリ/アップデート)。
        // 将来的にバッチアップデートと単一クエリで全社員を取得するようにリファクタリングするべきです。
        
        if (employeeIds == null || employeeIds.length == 0 || daysToAdd <= 0) {
            return; // 何もしない
        }

        transactionManager.executeInTransaction(() -> {
            for (String idStr : employeeIds) {
                try {
                    int id = Integer.parseInt(idStr);
                    Optional<Shain> shainOptional = shainPort.findById(id);
                    if (shainOptional.isPresent()) {
                        Shain shain = shainOptional.get();
                        shain.setPaidLeaveDays(shain.getPaidLeaveDays() + daysToAdd);
                        shainPort.update(shain);
                    }
                } catch (NumberFormatException e) {
                    // エラーをログに記録するか処理する。ここでは無効なIDをスキップする。
                    e.printStackTrace();
                }
            }
        });
    }
}
