package com.example.permission.domain.model;

/**
 * 権限グループに割り当てる機能。
 * DB の permission_group_feature.feature_code にはこの列挙の名前を保存する。
 */
public enum Feature {
    ATTENDANCE("勤怠入力"),
    OVERTIME_REQUEST("残業事前申請"),
    LEAVE_REQUEST("休暇申請"),
    APPROVAL("承認"),
    SHAIN_ADMIN("社員管理"),
    PAID_LEAVE_ADMIN("有給休暇管理"),
    MASTER_ADMIN("マスタ管理");

    private final String label;

    Feature(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String getCode() {
        return name();
    }
}
