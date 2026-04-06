package com.example.application.service;

import com.example.application.port.in.UpdatePaidLeaveDaysUseCase;
import com.example.application.port.out.ShainPort;
import com.example.entity.Shain;

import java.util.Optional;

/**
 * 社員の有給日数を更新するユースケースの実装。
 */
public class UpdatePaidLeaveDaysService implements UpdatePaidLeaveDaysUseCase {

    private final ShainPort shainPort;

    public UpdatePaidLeaveDaysService(ShainPort shainPort) {
        this.shainPort = shainPort;
    }

    @Override
    public void updatePaidLeaveDays(int shainId, int days) {
        Optional<Shain> shainOptional = shainPort.findById(shainId);
        if (shainOptional.isPresent()) {
            Shain shain = shainOptional.get();
            shain.setPaidLeaveDays(days);
            shainPort.update(shain);
        } else {
            // or throw a custom exception
            throw new IllegalArgumentException("Shain not found with id: " + shainId);
        }
    }
}
