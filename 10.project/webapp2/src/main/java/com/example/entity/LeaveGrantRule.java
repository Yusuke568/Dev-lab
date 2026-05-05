package com.example.entity;

import java.io.Serializable;

/**
 * 有給休暇付与ルールを表すエンティティ（レガシーDAO用）。
 */
public class LeaveGrantRule implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int yearsOfService;
    private int grantDays;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getYearsOfService() {
        return yearsOfService;
    }

    public void setYearsOfService(int yearsOfService) {
        this.yearsOfService = yearsOfService;
    }

    public int getGrantDays() {
        return grantDays;
    }

    public void setGrantDays(int grantDays) {
        this.grantDays = grantDays;
    }
}
