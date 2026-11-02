package com.example.auth.domain.model;

/**
 * 管理者が設定する初期パスワードの指定。
 * 「自動で作る」ならランダムな仮パスワードを作り、そうでなければ入力されたパスワードを使う。
 */
public class InitialPassword {

    private final boolean generate;
    private final String password;
    private final String confirm;

    private InitialPassword(boolean generate, String password, String confirm) {
        this.generate = generate;
        this.password = password;
        this.confirm = confirm;
    }

    /** ランダムな仮パスワードを作る。 */
    public static InitialPassword generated() {
        return new InitialPassword(true, null, null);
    }

    /** 管理者が入力したパスワードを使う。 */
    public static InitialPassword specified(String password, String confirm) {
        return new InitialPassword(false, password, confirm);
    }

    public boolean isGenerate() { return generate; }
    public String getPassword() { return password; }
    public String getConfirm() { return confirm; }
}
