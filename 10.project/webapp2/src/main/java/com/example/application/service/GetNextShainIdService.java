package com.example.application.service;

import com.example.application.port.in.GetNextShainIdUseCase;
import com.example.application.port.out.ShainPort;

/**
 * 次の社員ID取得ユースケースの実装。
 */
public class GetNextShainIdService implements GetNextShainIdUseCase {

    private final ShainPort shainPort;

    public GetNextShainIdService(ShainPort shainPort) {
        this.shainPort = shainPort;
    }

    @Override
    public int getNextShainId() {
        // 古いビジネスロジックを維持: 全社員数 + 100
        long currentCount = shainPort.count();
        return (int) currentCount + 100;
    }
}
