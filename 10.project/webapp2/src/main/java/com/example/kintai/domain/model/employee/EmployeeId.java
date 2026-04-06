package com.example.kintai.domain.model.employee;

import java.io.Serializable;
import java.util.Objects;

/**
 * 社員ID（値オブジェクト）
 *
 * このオブジェクトは不変（Immutable）です。
 */
public final class EmployeeId implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String value;

    public EmployeeId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("EmployeeId cannot be null or blank.");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployeeId that = (EmployeeId) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "EmployeeId{" +
                "value='" + value + '\'' +
                '}';
    }
}
