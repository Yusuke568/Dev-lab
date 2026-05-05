package com.example.entity;

import java.io.Serializable;

/**
 * 遉ｾ蜩｡諠・ｱ繧定｡ｨ縺吶お繝ｳ繝・ぅ繝・ぅ繧ｯ繝ｩ繧ｹ縲・
 */
public class Shain implements Serializable {
    private static final long serialVersionUID = 1L;

    // 遉ｾ蜩｡螻樊ｧ(繝励Ο繝代ユ繧｣)
    private int id;
    private String name;
    private String namekana;
    private int entryyear;
    private String gender;
    private String jobclass;
    private int paidLeaveDays;
    private int yearsOfService;

    public int getYearsOfService() {
        return yearsOfService;
    }

    public void setYearsOfService(int yearsOfService) {
        this.yearsOfService = yearsOfService;
    }

    public int getPaidLeaveDays() {
        return paidLeaveDays;
    }

    public void setPaidLeaveDays(int paidLeaveDays) {
        this.paidLeaveDays = paidLeaveDays;
    }

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

    public String getNamekana() {
        return namekana;
    }

    public void setNamekana(String namekana) {
        this.namekana = namekana;
    }

    public int getEntryyear() {
        return entryyear;
    }

    public void setEntryyear(int entryyear) {
        this.entryyear = entryyear;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getJobclass() {
        return jobclass;
    }

    public void setJobclass(String jobclass) {
        this.jobclass = jobclass;
    }
}
