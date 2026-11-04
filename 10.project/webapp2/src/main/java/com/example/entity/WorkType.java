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
    /** 有給消費日数（有給 1、半休 0.5、それ以外 0） */
    private double paidLeaveDays;

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

    public double getPaidLeaveDays() {
        return paidLeaveDays;
    }

    public void setPaidLeaveDays(double paidLeaveDays) {
        this.paidLeaveDays = paidLeaveDays;
    }

    /**
     * DB の行から有給消費日数を読む。列がまだない（マイグレーション 20261109 の前）なら、
     * 有給扱いの区分は 1 日、それ以外は 0 日とする（これまでの数え方）。
     */
    public static double readPaidLeaveDays(java.sql.ResultSet rs, boolean isPaid) {
        try {
            return rs.getDouble("paid_leave_days");
        } catch (java.sql.SQLException e) {
            return isPaid ? 1 : 0;
        }
    }
}
