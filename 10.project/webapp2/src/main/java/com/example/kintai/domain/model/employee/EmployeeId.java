package com.example.kintai.domain.model.employee;

import java.io.Serializable;
import java.util.Objects;

/**
 * 遉ｾ蜩｡ID・亥､繧ｪ繝悶ず繧ｧ繧ｯ繝茨ｼ・
 *
 * 縺薙・繧ｪ繝悶ず繧ｧ繧ｯ繝医・荳榊､会ｼ・mmutable・峨〒縺吶・
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
