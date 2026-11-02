package com.example.shain.application.service;

import com.example.auth.domain.model.PasswordValidationException;
import com.example.auth.domain.port.out.LoginPort;
import com.example.auth.domain.service.InitialPasswordResolver;
import com.example.auth.domain.service.PasswordHasher;
import com.example.shain.application.port.in.*;
import com.example.shain.domain.model.Shain;
import com.example.shain.domain.model.ShainId;
import com.example.shain.domain.port.out.ShainPort;
import com.example.shared.transaction.TransactionManager;

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
    private final LoginPort loginPort;
    private final PasswordHasher passwordHasher;
    private final InitialPasswordResolver initialPasswordResolver;
    private final TransactionManager transactionManager;

    public ShainService(ShainPort shainPort, LoginPort loginPort, PasswordHasher passwordHasher,
                        InitialPasswordResolver initialPasswordResolver, TransactionManager transactionManager) {
        this.shainPort = shainPort;
        this.loginPort = loginPort;
        this.passwordHasher = passwordHasher;
        this.initialPasswordResolver = initialPasswordResolver;
        this.transactionManager = transactionManager;
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
    public RegisterShainResult registerShain(RegisterShainCommand command) {
        // パスワードのチェックとハッシュ化（時間がかかる）は、トランザクションを始める前に済ませる
        String plain = initialPasswordResolver.resolve(command.getInitialPassword());
        String hash = passwordHasher.hash(plain.toCharArray());

        ShainId id = transactionManager.executeInTransaction(() -> {
            ShainId newId = shainPort.nextId();
            String username = newId.getValue();
            if (loginPort.exists(username)) {
                throw new PasswordValidationException("ユーザー名 " + username + " のアカウントがすでにあります。");
            }
            Shain shain = new Shain(
                    newId,
                    command.getName(),
                    command.getNamekana(),
                    command.getEntryYear(),
                    command.getGender(),
                    command.getJobClass(),
                    0, // 有給初期値
                    0  // 勤続年数初期値
            );
            shainPort.create(shain);
            shainPort.assignDepartment(newId, command.getDepartmentId());
            // 初回ログイン時に本人に変更してもらう
            loginPort.insertAccount(username, hash, true);
            return newId;
        });
        boolean generated = command.getInitialPassword() == null || command.getInitialPassword().isGenerate();
        return new RegisterShainResult(id, generated ? plain : null);
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
