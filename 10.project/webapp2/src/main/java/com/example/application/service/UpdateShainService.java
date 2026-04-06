package com.example.application.service;

import com.example.application.port.in.UpdateShainUseCase;
import com.example.application.port.out.ShainPort;
import com.example.entity.Shain;

/**
 * 社員情報更新ユースケースの実装。
 */
public class UpdateShainService implements UpdateShainUseCase {

    private final ShainPort shainPort;

    public UpdateShainService(ShainPort shainPort) {
        this.shainPort = shainPort;
    }

    @Override
    public void updateShain(Shain shain) {
        // 注: 本来はここでトランザクションを開始・終了するべき。
        // 現在はアダプタ側で一時的にトランザクションを管理している。
        shainPort.update(shain);
    }
}
