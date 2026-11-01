package com.example.approval.domain.model;

/**
 * 承認ルートを使う申請の種別。DB の request_type にはこの列挙の名前を保存する。
 */
public enum RequestType {
    OVERTIME("残業事前申請"),
    LEAVE("休暇申請");

    private final String label;

    RequestType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String getCode() {
        return name();
    }
}
