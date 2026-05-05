package com.example.shain.domain.model;

import java.util.Objects;

/**
 * 社員IDを表す値オブジェクト。
 */
public class ShainId {
    private final String value;

    public ShainId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ShainId cannot be blank.");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public int getIntValue() {
        return Integer.parseInt(value);
    }

    public static ShainId of(int value) {
        return new ShainId(String.valueOf(value));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShainId shainId = (ShainId) o;
        return Objects.equals(value, shainId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
