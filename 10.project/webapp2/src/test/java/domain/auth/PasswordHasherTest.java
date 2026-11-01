package domain.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("パスワードハッシュ（PBKDF2）テスト")
public class PasswordHasherTest {

    private final PasswordHasher hasher = new PasswordHasher();

    @Test
    @DisplayName("pbkdf2_sha256$反復回数$ソルト$ハッシュ の形式で作成されること")
    void testHashFormat() {
        String stored = hasher.hash("secret".toCharArray());

        String[] parts = stored.split("\\$");
        assertEquals(4, parts.length);
        assertEquals("pbkdf2_sha256", parts[0]);
        assertEquals("210000", parts[1]);
        assertEquals(16, Base64.getDecoder().decode(parts[2]).length);
        assertEquals(32, Base64.getDecoder().decode(parts[3]).length);
    }

    @Test
    @DisplayName("正しいパスワードは照合に成功すること")
    void testVerifyCorrectPassword() {
        String stored = hasher.hash("correct horse".toCharArray());

        assertTrue(hasher.verify("correct horse".toCharArray(), stored));
    }

    @Test
    @DisplayName("違うパスワードは照合に失敗すること")
    void testVerifyWrongPassword() {
        String stored = hasher.hash("correct horse".toCharArray());

        assertFalse(hasher.verify("correct horsE".toCharArray(), stored));
        assertFalse(hasher.verify("".toCharArray(), stored));
    }

    @Test
    @DisplayName("同じパスワードでもソルトが毎回変わり、どちらも照合できること")
    void testSaltIsRandom() {
        String first = hasher.hash("secret".toCharArray());
        String second = hasher.hash("secret".toCharArray());

        assertNotEquals(first, second);
        assertTrue(hasher.verify("secret".toCharArray(), first));
        assertTrue(hasher.verify("secret".toCharArray(), second));
    }

    @Test
    @DisplayName("保存値の形式が不正な場合は例外にならず照合に失敗すること")
    void testVerifyMalformedStoredValue() {
        String valid = hasher.hash("secret".toCharArray());
        String[] parts = valid.split("\\$");
        String[] malformed = {
                null,
                "",
                "secret",                                                    // 平文のまま
                "pbkdf2_sha256$210000$" + parts[2],                          // 区切りが足りない
                valid + "$extra",                                            // 区切りが多い
                "pbkdf2_sha1$210000$" + parts[2] + "$" + parts[3],           // 未知のアルゴリズム
                "pbkdf2_sha256$abc$" + parts[2] + "$" + parts[3],            // 反復回数が数字でない
                "pbkdf2_sha256$0$" + parts[2] + "$" + parts[3],              // 反復回数が0
                "pbkdf2_sha256$999999999$" + parts[2] + "$" + parts[3],      // 反復回数が大きすぎる
                "pbkdf2_sha256$210000$!!!$" + parts[3],                      // ソルトが壊れたBase64
                "pbkdf2_sha256$210000$" + parts[2] + "$!!!",                 // ハッシュが壊れたBase64
                "pbkdf2_sha256$210000$$" + parts[3],                         // ソルトが空
        };

        for (String stored : malformed) {
            assertFalse(hasher.verify("secret".toCharArray(), stored), "should reject: " + stored);
        }
    }

    @Test
    @DisplayName("入力パスワードがnullの場合は照合に失敗すること")
    void testVerifyNullPassword() {
        String stored = hasher.hash("secret".toCharArray());

        assertFalse(hasher.verify(null, stored));
    }

    @Test
    @DisplayName("空のパスワードはハッシュ化できないこと")
    void testHashEmptyPassword() {
        assertThrows(IllegalArgumentException.class, () -> hasher.hash(new char[0]));
        assertThrows(IllegalArgumentException.class, () -> hasher.hash(null));
    }
}
