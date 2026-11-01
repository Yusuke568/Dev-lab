package com.example.master.domain.port.out;

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
 * マスタ管理画面で扱うマスタの永続化操作を定義するポート。
 * 各 save は、id が null なら追加、あれば更新する。
 */
public interface MasterPort {

    // --- 役職 ---
    List<JobClass> findJobClasses();
    void saveJobClass(Integer id, String name, Integer permissionGroupId);

    // --- 権限グループ ---
    List<PermissionGroup> findPermissionGroups();
    /** @return 保存した権限グループの ID */
    int savePermissionGroup(Integer id, String name);
    void replaceGroupFeatures(int groupId, Set<String> featureCodes);

    // --- 組織図 ---
    /** 全部署（並び順は sort_order, id）。承認者名・所属人数も設定して返す。 */
    List<Department> findDepartments();
    void saveDepartment(Integer id, String name, Integer parentId, Integer approverStaffId, int sortOrder);
    List<StaffOption> findStaffOptions();
    void assignDepartment(int staffId, Integer departmentId);
    Integer findDepartmentIdOfStaff(int staffId);

    // --- 適用マスタ ---
    List<WorkType> findAbstracts();
    boolean existsAbstract(int id);
    void insertAbstract(int id, String name, boolean isWork, boolean isPaid, boolean isLegalHoliday, boolean isPrescribedHoliday);
    void updateAbstract(int id, String name, boolean isWork, boolean isPaid, boolean isLegalHoliday, boolean isPrescribedHoliday);

    // --- 有給付与ルール（勤続年数ごとに1件） ---
    List<LeaveGrantRule> findLeaveGrantRules();
    void saveLeaveGrantRule(int yearsOfService, int grantDays);
    void deleteLeaveGrantRule(int yearsOfService);

    // --- 承認ルート設定 ---
    List<ApprovalRouteSetting> findApprovalRouteSettings();
    void saveApprovalRouteSetting(RequestType requestType, int maxLevels);
}
