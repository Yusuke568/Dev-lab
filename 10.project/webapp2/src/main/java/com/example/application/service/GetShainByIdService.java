package com.example.application.service;

import com.example.application.port.in.GetShainByIdUseCase;
import com.example.application.port.out.ShainPort;
import com.example.entity.Shain;

import java.util.Optional;

/**
 * 社員IDによる社員情報取得ユースケースの実装。
 */
public class GetShainByIdService implements GetShainByIdUseCase {

    private final ShainPort shainPort;

    public GetShainByIdService(ShainPort shainPort) {
        this.shainPort = shainPort;
    }

    @Override
    public Optional<Shain> getShainById(int id) {
        return shainPort.findById(id);
    }
}
