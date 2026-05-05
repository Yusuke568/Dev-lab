package com.example.shain.application.service;

import com.example.shain.application.port.in.*;
import com.example.shain.domain.model.Shain;
import com.example.shain.domain.model.ShainId;
import com.example.shain.domain.port.out.ShainPort;

import java.util.List;
import java.util.Optional;

/**
 * 社員管理ドメインのユースケース実装クラス。
 */
public class ShainService implements 
    GetShainListUseCase, 
    GetShainByIdUseCase, 
    RegisterShainUseCase, 
    UpdateShainUseCase, 
    DeleteShainUseCase, 
    GetNextShainIdUseCase {

    private final ShainPort shainPort;

    public ShainService(ShainPort shainPort) {
        this.shainPort = shainPort;
    }

    @Override
    public List<Shain> getShainList() {
        return shainPort.findAll();
    }

    @Override
    public Optional<Shain> getShainById(ShainId id) {
        return shainPort.findById(id);
    }

    @Override
    public void registerShain(RegisterShainCommand command) {
        ShainId id = shainPort.nextId();
        Shain shain = new Shain(
                id,
                command.getName(),
                command.getNamekana(),
                command.getEntryYear(),
                command.getGender(),
                command.getJobClass(),
                0, // 有給初期値
                0  // 勤続年数初期値
        );
        shainPort.create(shain);
    }

    @Override
    public void updateShain(UpdateShainCommand command) {
        ShainId id = new ShainId(command.getId());
        Shain existingShain = shainPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shain not found: " + id));

        Shain updatedShain = new Shain(
                id,
                command.getName(),
                command.getNamekana(),
                command.getEntryYear(),
                command.getGender(),
                command.getJobClass(),
                existingShain.getPaidLeaveDays(),
                existingShain.getYearsOfService()
        );
        shainPort.update(updatedShain);
    }

    @Override
    public void deleteShain(ShainId id) {
        shainPort.deleteById(id);
    }

    @Override
    public ShainId getNextId() {
        return shainPort.nextId();
    }
}
