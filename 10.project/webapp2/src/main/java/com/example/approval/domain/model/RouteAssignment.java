package com.example.approval.domain.model;

/**
 * 承認ルートの割り当て先（社員または部署）。
 * 社員への割り当てが部署への割り当てより優先される。部署への割り当ては配下の部署にも効く。
 */
public class RouteAssignment {

    public static final String STAFF = "STAFF";
    public static final String DEPARTMENT = "DEPARTMENT";

    private final String targetType;
    private final int targetId;
    /** 画面表示用（社員名・部署名）。読み込み時だけ入る */
    private final String targetName;

    public RouteAssignment(String targetType, int targetId, String targetName) {
        if (!STAFF.equals(targetType) && !DEPARTMENT.equals(targetType)) {
            throw new IllegalArgumentException("割り当て先の種類が正しくありません: " + targetType);
        }
        this.targetType = targetType;
        this.targetId = targetId;
        this.targetName = targetName;
    }

    public String getTargetType() { return targetType; }
    public int getTargetId() { return targetId; }
    public String getTargetName() { return targetName; }
    public boolean isStaff() { return STAFF.equals(targetType); }

    /** 画面表示用（例: 「社員 126 山田」「部署 開発部」） */
    public String getLabel() {
        String name = targetName != null ? targetName : String.valueOf(targetId);
        return isStaff() ? "社員 " + targetId + " " + (targetName != null ? targetName : "") : "部署 " + name;
    }
}
