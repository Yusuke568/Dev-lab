package com.example.master.domain.model;

/**
 * 承認者や所属の選択肢に出す社員。
 */
public class StaffOption {

    private final int id;
    private final String name;
    private final Integer departmentId;
    private final String departmentName;
    private final Integer classId;
    private final String className;

    public StaffOption(int id, String name, Integer departmentId, String departmentName) {
        this(id, name, departmentId, departmentName, null, null);
    }

    public StaffOption(int id, String name, Integer departmentId, String departmentName, Integer classId, String className) {
        this.id = id;
        this.name = name;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.classId = classId;
        this.className = className;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public Integer getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    /** 職種（class_master）。承認者を選ぶときのグループに使う */
    public Integer getClassId() { return classId; }
    public String getClassName() { return className; }
}
