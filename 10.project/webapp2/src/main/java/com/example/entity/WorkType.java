package com.example.entity;

import java.io.Serializable;

/**
 * 驕ｩ逕ｨ繝槭せ繧ｿ・亥共蜍吝ｽ｢諷具ｼ峨・諠・ｱ繧定｡ｨ縺吶お繝ｳ繝・ぅ繝・ぅ繧ｯ繝ｩ繧ｹ縲・
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
