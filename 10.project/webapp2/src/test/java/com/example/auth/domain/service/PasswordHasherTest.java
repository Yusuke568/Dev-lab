package com.example.auth.domain.service;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    private final PasswordHasher hasher = new PasswordHasher();

    @Test
    void hashHasExpectedFormat() {
        String stored = hasher.hash("secret".toCharArray());

        String[] parts = stored.split("\\$");
        assertEquals(4, parts.length);
        assertEquals("pbkdf2_sha256", parts[0]);
        assertEquals("210000", parts[1]);
        assertEquals(16, Base64.getDecoder().decode(parts[2]).length);
        assertEquals(32, Base64.getDecoder().decode(parts[3]).length);
    }

    @Test
    void verifyAcceptsCorrectPassword() {
        String stored = hasher.hash("correct horse".toCharArray());

        assertTrue(hasher.verify("correct horse".toCharArray(), stored));
    }

    @Test
    void verifyRejectsWrongPassword() {
        String stored = hasher.hash("correct horse".toCharArray());

        assertFalse(hasher.verify("correct horsE".toCharArray(), stored));
        assertFalse(hasher.verify("".toCharArray(), stored));
    }

    @Test
    void samePasswordGetsDifferentSalt() {
        String first = hasher.hash("secret".toCharArray());
        String second = hasher.hash("secret".toCharArray());

        assertNotEquals(first, second);
        assertTrue(hasher.verify("secret".toCharArray(), first));
        assertTrue(hasher.verify("secret".toCharArray(), second));
    }

    @Test
    void verifyRejectsMalformedStoredValues() {
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
    void verifyRejectsNullPassword() {
        String stored = hasher.hash("secret".toCharArray());

        assertFalse(hasher.verify(null, stored));
    }

    @Test
    void hashRejectsEmptyPassword() {
        assertThrows(IllegalArgumentException.class, () -> hasher.hash(new char[0]));
        assertThrows(IllegalArgumentException.class, () -> hasher.hash(null));
    }
}
