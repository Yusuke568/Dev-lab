package com.example.auth.domain.model;

/**
 * ログイン用アカウント（login_table の1行）。
 */
public class LoginAccount {

    private final String username;
    private final String passwordHash;
    private final boolean enabled;
    private final boolean mustChangePassword;

    public LoginAccount(String username, String passwordHash, boolean enabled, boolean mustChangePassword) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
        this.mustChangePassword = mustChangePassword;
    }

    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isEnabled() { return enabled; }
    public boolean isMustChangePassword() { return mustChangePassword; }
}
