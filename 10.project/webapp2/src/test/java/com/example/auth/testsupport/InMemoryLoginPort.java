package com.example.auth.testsupport;

import com.example.auth.domain.model.LoginAccount;
import com.example.auth.domain.port.out.LoginPort;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** login_table の代わりのメモリ上の実装。 */
public class InMemoryLoginPort implements LoginPort, FakeTransactionManager.Snapshottable {

    private Map<String, LoginAccount> accounts = new HashMap<>();
    private Map<String, LoginAccount> snapshot;
    /** true にすると insertAccount が失敗する（DB エラーの代わり） */
    public boolean failOnInsert;

    public void put(String username, String passwordHash, boolean mustChangePassword) {
        accounts.put(username, new LoginAccount(username, passwordHash, true, mustChangePassword));
    }

    public LoginAccount get(String username) {
        return accounts.get(username);
    }

    public int size() {
        return accounts.size();
    }

    @Override
    public Optional<String> findPasswordHash(String username) {
        return findAccount(username).filter(LoginAccount::isEnabled).map(LoginAccount::getPasswordHash);
    }

    @Override
    public Optional<LoginAccount> findAccount(String username) {
        return Optional.ofNullable(accounts.get(username));
    }

    @Override
    public boolean exists(String username) {
        return accounts.containsKey(username);
    }

    @Override
    public void insertAccount(String username, String passwordHash, boolean mustChangePassword) {
        if (failOnInsert) {
            throw new RuntimeException("Failed to create login account");
        }
        if (accounts.containsKey(username)) {
            throw new RuntimeException("duplicate key");
        }
        put(username, passwordHash, mustChangePassword);
    }

    @Override
    public void updatePassword(String username, String passwordHash, boolean mustChangePassword) {
        if (!accounts.containsKey(username)) {
            throw new RuntimeException("Login account not found");
        }
        put(username, passwordHash, mustChangePassword);
    }

    @Override
    public void takeSnapshot() {
        snapshot = new HashMap<>(accounts);
    }

    @Override
    public void restoreSnapshot() {
        accounts = snapshot;
    }
}
