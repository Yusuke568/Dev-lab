package com.example.kintai.application.service;

import com.example.kintai.application.port.in.AttendanceAccessUseCase;
import com.example.kintai.domain.model.attendance.Subordinate;
import com.example.kintai.domain.port.out.SubordinatePort;
import com.example.permission.domain.model.Feature;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 勤怠表を参照・編集できる範囲の判定。
 */
public class AttendanceAccessService implements AttendanceAccessUseCase {

    private final SubordinatePort subordinatePort;

    public AttendanceAccessService(SubordinatePort subordinatePort) {
        this.subordinatePort = subordinatePort;
    }

    @Override
    public boolean canAccess(String loginUser, Set<String> features, String targetStaffId) {
        if (loginUser == null || targetStaffId == null) {
            return false;
        }
        if (loginUser.equals(targetStaffId)) {
            return true;
        }
        if (!targetStaffId.matches("\\d{1,9}") || !hasApproval(features)) {
            return false;
        }
        return subordinatePort.isManagedBy(targetStaffId, loginUser);
    }

    @Override
    public List<Subordinate> getSubordinates(String loginUser, Set<String> features) {
        if (loginUser == null || !hasApproval(features)) {
            return Collections.emptyList();
        }
        return subordinatePort.findManagedStaff(loginUser);
    }

    private static boolean hasApproval(Set<String> features) {
        return features != null && features.contains(Feature.APPROVAL.name());
    }
}
