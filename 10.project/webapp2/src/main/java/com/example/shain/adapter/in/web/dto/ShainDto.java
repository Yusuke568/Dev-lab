package com.example.shain.adapter.in.web.dto;

import com.example.shain.domain.model.Shain;

/**
 * JSP表示用の社員データ転送オブジェクト。
 * 既存のJSPが期待するプロパティ名（entryyear, jobclass等）を保持します。
 */
public class ShainDto {
    private final int id;
    private final String name;
    private final String namekana;
    private final int entryyear;
    private final String gender;
    private final String jobclass;
    private final int paidLeaveDays;
    private final int yearsOfService;

    public ShainDto(Shain domain) {
        this.id = domain.getId().getIntValue();
        this.name = domain.getName();
        this.namekana = domain.getNamekana();
        this.entryyear = domain.getEntryYear();
        this.gender = domain.getGender();
        this.jobclass = domain.getJobClass();
        this.paidLeaveDays = domain.getPaidLeaveDays();
        this.yearsOfService = domain.getYearsOfService();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getNamekana() { return namekana; }
    public int getEntryyear() { return entryyear; }
    public String getGender() { return gender; }
    public String getJobclass() { return jobclass; }
    public int getPaidLeaveDays() { return paidLeaveDays; }
    public int getYearsOfService() { return yearsOfService; }
}
