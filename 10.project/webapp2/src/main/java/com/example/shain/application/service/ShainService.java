package com.example.shain.application.service;

import com.example.auth.domain.model.PasswordValidationException;
import com.example.auth.domain.port.out.LoginPort;
import com.example.auth.domain.service.InitialPasswordResolver;
import com.example.auth.domain.service.PasswordHasher;
import com.example.shain.application.port.in.*;
import com.example.shain.domain.model.InvalidShainIdException;
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
    public ShainId updateShain(UpdateShainCommand command) {
        ShainId currentId = new ShainId(command.getId());
        String requested = command.getNewId() == null ? null : command.getNewId().strip();
        boolean changing = requested != null && !requested.isEmpty() && !requested.equals(currentId.getValue());

        return transactionManager.executeInTransaction(() -> {
            Shain existingShain = shainPort.findById(currentId)
                    .orElseThrow(() -> new IllegalArgumentException("Shain not found: " + currentId));
            ShainId id = currentId;
            if (changing) {
                // 画面でのチェックのあとに別の人が同じIDを使った場合に備え、トランザクションの中でもう一度調べる
                Optional<String> error = checkNewId(currentId.getValue(), requested);
                if (error.isPresent()) {
                    throw new InvalidShainIdException(error.get());
                }
                id = new ShainId(requested);
                shainPort.changeId(currentId, id);
            }
            shainPort.update(new Shain(
                    id,
                    command.getName(),
                    command.getNamekana(),
                    command.getEntryYear(),
                    command.getGender(),
                    command.getJobClass(),
                    existingShain.getPaidLeaveDays(),
                    existingShain.getYearsOfService()
            ));
            return id;
        });
    }

    /** 社員IDに使える形式：1〜9桁の数字（先頭は0以外）。ログインのユーザー名にもなる */
    private static final java.util.regex.Pattern ID_FORMAT = java.util.regex.Pattern.compile("[1-9][0-9]{0,8}");

    @Override
    public Optional<String> checkNewId(String currentId, String newId) {
        String value = newId == null ? "" : newId.strip();
        if (value.isEmpty()) {
            return Optional.of("社員IDを入力してください。");
        }
        if (value.equals(currentId)) {
            return Optional.empty();
        }
        if (!ID_FORMAT.matcher(value).matches()) {
            return Optional.of("社員IDは1〜9桁の数字で入力してください（先頭に0は付けられません）。");
        }
        if (shainPort.findById(new ShainId(value)).isPresent()) {
            return Optional.of("社員ID " + value + " はすでにほかの社員が使っています。");
        }
        if (loginPort.exists(value)) {
            return Optional.of("社員ID " + value + " はすでにログインアカウントとして使われています。");
        }
        return Optional.empty();
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
