package com.example.master.domain.model;

import com.example.approval.domain.model.RequestType;

/**
 * 申請種別ごとの承認ルート設定（組織図を何段上までたどるか）。
 */
public class ApprovalRouteSetting {

    private final RequestType requestType;
    private final int maxLevels;

    public ApprovalRouteSetting(RequestType requestType, int maxLevels) {
        this.requestType = requestType;
        this.maxLevels = maxLevels;
    }

    public RequestType getRequestType() { return requestType; }
    public String getRequestTypeCode() { return requestType.name(); }
    public String getLabel() { return requestType.getLabel(); }
    public int getMaxLevels() { return maxLevels; }
}
