package com.example.entity;

import java.io.Serializable;

/**
 * 適用マスタ（勤務形態）の情報を表すエンティティクラス。
 */
public class WorkType implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private boolean isWork;
    private boolean isPaid;
    private boolean isLegalHoliday;
    private boolean isPrescribedHoliday;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isWork() {
        return isWork;
    }

    public void setWork(boolean isWork) {
        this.isWork = isWork;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean isPaid) {
        this.isPaid = isPaid;
    }

    public boolean isLegalHoliday() {
        return isLegalHoliday;
    }

    public void setLegalHoliday(boolean isLegalHoliday) {
        this.isLegalHoliday = isLegalHoliday;
    }

    public boolean isPrescribedHoliday() {
        return isPrescribedHoliday;
    }

    public void setPrescribedHoliday(boolean isPrescribedHoliday) {
        this.isPrescribedHoliday = isPrescribedHoliday;
    }
}
