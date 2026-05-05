package com.example.shain.domain.model;

import java.util.Objects;

/**
 * 社員を表すドメインエンティティ。
 */
public class Shain {
    private final ShainId id;
    private String name;
    private String namekana;
    private int entryYear;
    private String gender;
    private String jobClass;
    private int paidLeaveDays;
    private int yearsOfService;

    public Shain(ShainId id, String name, String namekana, int entryYear, String gender, String jobClass, int paidLeaveDays, int yearsOfService) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.setName(name);
        this.setNamekana(namekana);
        this.entryYear = entryYear;
        this.gender = gender;
        this.jobClass = jobClass;
        this.paidLeaveDays = paidLeaveDays;
        this.yearsOfService = yearsOfService;
    }

    // ビジネスロジックの例：勤続年数に応じた有給付与など（将来的に追加）

    public ShainId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank.");
        }
        this.name = name;
    }

    public String getNamekana() {
        return namekana;
    }

    public void setNamekana(String namekana) {
        this.namekana = namekana;
    }

    public int getEntryYear() {
        return entryYear;
    }

    public String getGender() {
        return gender;
    }

    public String getJobClass() {
        return jobClass;
    }

    public int getPaidLeaveDays() {
        return paidLeaveDays;
    }

    public int getYearsOfService() {
        return yearsOfService;
    }

    public void setPaidLeaveDays(int paidLeaveDays) {
        this.paidLeaveDays = paidLeaveDays;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shain shain = (Shain) o;
        return Objects.equals(id, shain.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
