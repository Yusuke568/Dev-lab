package com.example.kintai.domain.model.attendance;

/**
 * 承認者の配下（組織図上で承認者になっている部署とその下位部署）に所属する社員。
 */
public class Subordinate {

    private final String staffId;
    private final String name;
    private final String departmentName;

    public Subordinate(String staffId, String name, String departmentName) {
        this.staffId = staffId;
        this.name = name;
        this.departmentName = departmentName;
    }

    public String getStaffId() { return staffId; }
    public String getName() { return name; }
    public String getDepartmentName() { return departmentName; }
}
