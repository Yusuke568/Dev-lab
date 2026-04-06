package com.example.application.service;

import com.example.application.port.in.DeleteShainUseCase;
import com.example.application.port.out.ShainPort;

/**
 * 社員情報削除ユースケースの実装。
 */
public class DeleteShainService implements DeleteShainUseCase {

    private final ShainPort shainPort;

    public DeleteShainService(ShainPort shainPort) {
        this.shainPort = shainPort;
    }

    @Override
    public void deleteShainById(int id) {
        // 注: 本来はここでトランザクションを開始・終了するべき。
        // 現在はアダプタ側で一時的にトランザクションを管理している。
        shainPort.deleteById(id);
    }
}
