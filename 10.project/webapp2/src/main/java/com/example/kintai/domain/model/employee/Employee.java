package com.example.kintai.domain.model.employee;

import java.util.Objects;

/**
 * 社員（エンティティ）
 *
 * ビジネスロジックを持つことができますが、ここではシンプルなデータ保持に留めます。
 */
public class Employee {
    private final EmployeeId id;
    private String name;

    public Employee(EmployeeId id, String name) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.setName(name);
    }

    public EmployeeId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Employee name cannot be null or blank.");
        }
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return id.equals(employee.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
