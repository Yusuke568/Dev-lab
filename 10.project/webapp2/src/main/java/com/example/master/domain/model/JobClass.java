package com.example.master.domain.model;

/**
 * 役職（class_master）。役職ごとに権限グループを1つ割り当てる。
 */
public class JobClass {

    private final int id;
    private final String name;
    private final Integer permissionGroupId;
    private final String permissionGroupName;

    public JobClass(int id, String name, Integer permissionGroupId, String permissionGroupName) {
        this.id = id;
        this.name = name;
        this.permissionGroupId = permissionGroupId;
        this.permissionGroupName = permissionGroupName;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public Integer getPermissionGroupId() { return permissionGroupId; }
    public String getPermissionGroupName() { return permissionGroupName; }
}
