package com.example.approval.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 承認ルートマスタの1件。
 * levels は段（1段目から）ごとの承認者の社員ID。同じ段に複数いれば、誰か1人の承認で次の段へ進む。
 * requestType が null なら残業事前申請・休暇申請の両方に使う。
 */
public class ApprovalRoute {

    private final Integer id;
    private final String name;
    private final RequestType requestType;
    private final String note;
    private final List<List<Integer>> levels;
    private final List<RouteAssignment> assignments;

    public ApprovalRoute(Integer id, String name, RequestType requestType, String note,
                         List<List<Integer>> levels, List<RouteAssignment> assignments) {
        this.id = id;
        this.name = name;
        this.requestType = requestType;
        this.note = note;
        this.levels = levels != null ? levels : new ArrayList<>();
        this.assignments = assignments != null ? assignments : new ArrayList<>();
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public RequestType getRequestType() { return requestType; }
    public String getNote() { return note; }
    public List<List<Integer>> getLevels() { return Collections.unmodifiableList(levels); }
    public List<RouteAssignment> getAssignments() { return Collections.unmodifiableList(assignments); }

    /** この申請種別に使えるルートか（種別の指定がないルートはどちらにも使える） */
    public boolean appliesTo(RequestType type) {
        return requestType == null || requestType == type;
    }

    /** 画面表示用の種別名 */
    public String getRequestTypeLabel() {
        return requestType == null ? "残業・休暇の両方" : requestType.getLabel();
    }

    public boolean isAssignedTo(String targetType, int targetId) {
        return assignments.stream().anyMatch(a -> a.getTargetType().equals(targetType) && a.getTargetId() == targetId);
    }
}
