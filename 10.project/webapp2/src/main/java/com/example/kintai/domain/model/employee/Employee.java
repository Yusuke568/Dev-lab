package com.example.kintai.domain.model.employee;

import java.util.Objects;

/**
 * 社員�E�エンチE��チE���E�E
 *
 * ビジネスロジチE��を持つことができますが、ここではシンプルなチE�Eタ保持に留めます、E
 */
public class Employee {
    private final EmployeeId id;
    private String name;
    /** 有給休暇の残日数（読み込んでいなければ null） */
    private Integer paidLeaveDays;

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

    public Integer getPaidLeaveDays() {
        return paidLeaveDays;
    }

    public void setPaidLeaveDays(Integer paidLeaveDays) {
        this.paidLeaveDays = paidLeaveDays;
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
