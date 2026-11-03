package com.example.permission.domain.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * 権限グループに割り当てる機能。
 * DB の permission_group_feature.feature_code にはこの列挙の名前を保存する。
 * マスタ管理はマスタごとに分け、権限グループごとに個別に割り当てられる。
 */
public enum Feature {
    ATTENDANCE("勤怠入力", null),
    OVERTIME_REQUEST("残業事前申請", null),
    LEAVE_REQUEST("休暇申請", null),
    APPROVAL("承認", null),
    SHAIN_ADMIN("社員管理", null),
    PAID_LEAVE_ADMIN("有給休暇管理", null),
    MASTER_PERMISSION_GROUP("権限グループ", Feature.MASTER_GROUP),
    MASTER_JOB_CLASS("役職", Feature.MASTER_GROUP),
    MASTER_DEPARTMENT("組織図", Feature.MASTER_GROUP),
    MASTER_APPROVAL_ROUTE("承認ルート設定", Feature.MASTER_GROUP),
    MASTER_ABSTRACT("適用マスタ", Feature.MASTER_GROUP),
    MASTER_LEAVE_RULE("有給付与ルール", Feature.MASTER_GROUP),
    MASTER_CORRECTION("補正CD", Feature.MASTER_GROUP);

    /** マスタ管理の機能をまとめる見出し */
    public static final String MASTER_GROUP = "マスタ管理";

    /**
     * 以前の「マスタ管理（全マスタ）」の機能コード。DB に残っていれば、すべてのマスタの機能として扱う
     * （マイグレーション 20261107 を流す前でも、これまでマスタ管理を使えた人が使えなくならないように）。
     */
    public static final String LEGACY_MASTER_ADMIN = "MASTER_ADMIN";

    private final String label;
    private final String group;

    Feature(String label, String group) {
        this.label = label;
        this.group = group;
    }

    /** 画面に出す名前。マスタは「マスタ管理（権限グループ）」のように見出しを付ける。 */
    public String getLabel() {
        return group == null ? label : group + "（" + label + "）";
    }

    /** 見出しを除いた短い名前（権限グループ画面の列見出し用）。 */
    public String getShortLabel() {
        return label;
    }

    /** まとめる見出し。なければ null。 */
    public String getGroup() {
        return group;
    }

    public boolean isMaster() {
        return MASTER_GROUP.equals(group);
    }

    public String getCode() {
        return name();
    }

    /** マスタ管理の機能すべて。 */
    public static Set<Feature> masterFeatures() {
        Set<Feature> set = EnumSet.noneOf(Feature.class);
        for (Feature feature : values()) {
            if (feature.isMaster()) {
                set.add(feature);
            }
        }
        return Collections.unmodifiableSet(set);
    }
}
