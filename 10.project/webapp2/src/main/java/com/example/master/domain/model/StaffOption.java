package com.example.master.domain.model;

/**
 * 承認者や所属の選択肢に出す社員。
 */
public class StaffOption {

    private final int id;
    private final String name;
    private final Integer departmentId;
    private final String departmentName;

    public StaffOption(int id, String name, Integer departmentId, String departmentName) {
        this.id = id;
        this.name = name;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public Integer getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
}
