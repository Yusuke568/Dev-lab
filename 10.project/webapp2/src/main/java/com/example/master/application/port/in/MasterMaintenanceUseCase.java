package com.example.master.application.port.in;

import com.example.approval.domain.model.RequestType;
import com.example.entity.WorkType;
import com.example.leave.domain.model.LeaveGrantRule;
import com.example.master.domain.model.ApprovalRouteSetting;
import com.example.master.domain.model.Department;
import com.example.master.domain.model.JobClass;
import com.example.master.domain.model.PermissionGroup;
import com.example.master.domain.model.StaffOption;

import java.util.List;
import java.util.Set;

/**
 * マスタ管理画面のユースケース。入力に問題があれば IllegalArgumentException を投げる。
 * 各 save は、id が null なら追加、あれば更新する。
 */
public interface MasterMaintenanceUseCase {

    List<JobClass> getJobClasses();
    void saveJobClass(Integer id, String name, Integer permissionGroupId);

    List<PermissionGroup> getPermissionGroups();
    void savePermissionGroup(Integer id, String name, Set<String> featureCodes);

    /** 部署を、親の直後に子が来るツリー順に並べて返す（depth 設定済み）。 */
    List<Department> getDepartmentTree();
    void saveDepartment(Integer id, String name, Integer parentId, Integer approverStaffId, int sortOrder);
    List<StaffOption> getStaffOptions();
    void assignDepartment(int staffId, Integer departmentId);
    Integer getDepartmentIdOfStaff(int staffId);

    List<WorkType> getAbstracts();
    /** isNew が true なら追加（ID の重複は不可）、false なら更新。 */
    void saveAbstract(boolean isNew, int id, String name, boolean isWork, boolean isPaid,
                      boolean isLegalHoliday, boolean isPrescribedHoliday);

    List<LeaveGrantRule> getLeaveGrantRules();
    void saveLeaveGrantRule(int yearsOfService, int grantDays);
    void deleteLeaveGrantRule(int yearsOfService);

    List<ApprovalRouteSetting> getApprovalRouteSettings();
    void saveApprovalRouteSetting(RequestType requestType, int maxLevels);
}
