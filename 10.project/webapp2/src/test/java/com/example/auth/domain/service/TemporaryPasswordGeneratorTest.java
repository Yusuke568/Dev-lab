package com.example.auth.domain.service;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemporaryPasswordGeneratorTest {

    private final TemporaryPasswordGenerator generator = new TemporaryPasswordGenerator();

    @Test
    void 十二文字で_紛らわしい文字を使わず_大文字小文字数字を含む() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            String password = generator.generate();
            assertEquals(12, password.length());
            assertTrue(password.chars().allMatch(c -> TemporaryPasswordGenerator.ALL.indexOf(c) >= 0), password);
            assertTrue(password.chars().noneMatch(c -> "0O1lI".indexOf(c) >= 0), password);
            assertTrue(password.chars().anyMatch(Character::isUpperCase), password);
            assertTrue(password.chars().anyMatch(Character::isLowerCase), password);
            assertTrue(password.chars().anyMatch(Character::isDigit), password);
            seen.add(password);
        }
        assertEquals(1000, seen.size(), "毎回違うパスワードを作る");
    }
}
