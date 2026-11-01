package com.example.auth.domain.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * PBKDF2WithHmacSHA256 によるパスワードのハッシュ作成と照合。
 * 保存形式: pbkdf2_sha256$&lt;反復回数&gt;$&lt;Base64のソルト&gt;$&lt;Base64のハッシュ&gt;
 */
public class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "pbkdf2_sha256";
    private static final int ITERATIONS = 210000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BYTES = 32;
    /** 照合時に受け付ける反復回数の上限（不正な値で処理が重くなるのを防ぐ） */
    private static final int MAX_ITERATIONS = 10_000_000;

    private final SecureRandom random = new SecureRandom();

    /**
     * パスワードのハッシュを保存形式の文字列で作成します。
     */
    public String hash(char[] password) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("password must not be empty");
        }
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        byte[] hash = pbkdf2(password, salt, ITERATIONS, HASH_BYTES);
        Base64.Encoder encoder = Base64.getEncoder();
        return PREFIX + "$" + ITERATIONS + "$" + encoder.encodeToString(salt) + "$" + encoder.encodeToString(hash);
    }

    /**
     * パスワードが保存されたハッシュと一致するか確認します。
     * 保存値の形式が不正な場合は false を返します。
     */
    public boolean verify(char[] password, String stored) {
        if (password == null || stored == null) {
            return false;
        }
        String[] parts = stored.split("\\$", -1);
        if (parts.length != 4 || !PREFIX.equals(parts[0])) {
            return false;
        }
        int iterations;
        byte[] salt;
        byte[] expected;
        try {
            iterations = Integer.parseInt(parts[1]);
            Base64.Decoder decoder = Base64.getDecoder();
            salt = decoder.decode(parts[2]);
            expected = decoder.decode(parts[3]);
        } catch (IllegalArgumentException e) {
            return false;
        }
        if (iterations < 1 || iterations > MAX_ITERATIONS || salt.length == 0 || expected.length == 0) {
            return false;
        }
        byte[] actual = pbkdf2(password, salt, iterations, expected.length);
        return MessageDigest.isEqual(expected, actual);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyBytes) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyBytes * 8);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to hash password", e);
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * ユーザー登録用に、入力したパスワードのハッシュを標準出力に表示します。
     * パスワードはコマンド履歴に残らないよう、引数ではなく対話入力で受け取ります。
     */
    public static void main(String[] args) throws IOException {
        char[] password;
        Console console = System.console();
        if (console != null) {
            password = console.readPassword("Password: ");
            char[] confirm = console.readPassword("Password (again): ");
            if (password == null || confirm == null || !Arrays.equals(password, confirm)) {
                System.err.println("Passwords do not match.");
                System.exit(1);
                return;
            }
            Arrays.fill(confirm, '\0');
        } else {
            // コンソールが無い場合（パイプ入力など）は標準入力の1行目を使う
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            String line = reader.readLine();
            password = line == null ? new char[0] : line.toCharArray();
        }
        if (password == null || password.length == 0) {
            System.err.println("Password must not be empty.");
            System.exit(1);
            return;
        }
        System.out.println(new PasswordHasher().hash(password));
        Arrays.fill(password, '\0');
    }
}
