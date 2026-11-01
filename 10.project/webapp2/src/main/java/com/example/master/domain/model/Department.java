package com.example.master.domain.model;

/**
 * 組織図の部署。parentId をたどると上位部署になる。
 * approverStaffId はその部署の承認者（未設定なら null）。
 */
public class Department {

    private final int id;
    private final String name;
    private final Integer parentId;
    private final Integer approverStaffId;
    private final int sortOrder;

    // 画面表示用（一覧の取得時だけ設定する）
    private String approverName;
    private int depth;
    private int memberCount;

    public Department(int id, String name, Integer parentId, Integer approverStaffId, int sortOrder) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.approverStaffId = approverStaffId;
        this.sortOrder = sortOrder;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public Integer getParentId() { return parentId; }
    public Integer getApproverStaffId() { return approverStaffId; }
    public int getSortOrder() { return sortOrder; }

    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }
    public int getDepth() { return depth; }
    public void setDepth(int depth) { this.depth = depth; }
    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    /** 一覧でツリーを字下げ表示するための接頭辞。 */
    public String getIndent() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("　　");
        }
        return depth > 0 ? sb.append("└ ").toString() : "";
    }
}
