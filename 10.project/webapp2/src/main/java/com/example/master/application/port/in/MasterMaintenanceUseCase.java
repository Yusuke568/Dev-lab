package com.example.master.application.port.in;

import com.example.approval.domain.model.ApprovalRoute;
import com.example.approval.domain.model.RequestType;
import com.example.approval.domain.model.RouteAssignment;
import com.example.entity.WorkType;
import com.example.leave.domain.model.LeaveGrantRule;
import com.example.master.domain.model.ApprovalRouteSetting;
import com.example.master.domain.model.Department;
import com.example.master.domain.model.JobClass;
import com.example.master.domain.model.PermissionGroup;
import com.example.master.domain.model.StaffOption;

import java.util.List;
import java.util.Map;
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
    /** subApproverIds は副承認者（主承認者と同じ段で、どちらか1人が承認すればよい）。null なら変更しない */
    void saveDepartment(Integer id, String name, Integer parentId, Integer approverStaffId, List<Integer> subApproverIds, int sortOrder);
    List<StaffOption> getStaffOptions();
    void assignDepartment(int staffId, Integer departmentId);
    Integer getDepartmentIdOfStaff(int staffId);
    /** 部署ID → 副承認者の社員ID */
    Map<Integer, List<Integer>> getSubApprovers();
    /** 社員の兼任部署（主所属は含まない） */
    List<Integer> getConcurrentDepartmentIds(int staffId);
    /** 社員ID → 兼任部署 */
    Map<Integer, List<Integer>> getAllConcurrentDepartments();
    /** 兼任部署を置き換える（主所属と同じ部署は除く） */
    void assignConcurrentDepartments(int staffId, List<Integer> departmentIds);

    /** 承認ルートマスタ */
    List<ApprovalRoute> getApprovalRoutes();
    /**
     * 承認ルートを保存する。levels は段ごとの承認者（空の段は詰める）。
     * 同じ申請種別で、同じ社員・部署をほかのルートにも割り当てていればエラー。
     */
    void saveApprovalRoute(Integer id, String name, RequestType requestType, String note,
                           List<List<Integer>> levels, List<RouteAssignment> assignments);

    /** extraLevels は条件つきで足す段（残業事前申請に使えるルートだけ） */
    void saveApprovalRoute(Integer id, String name, RequestType requestType, String note,
                           List<List<Integer>> levels, List<com.example.approval.domain.model.ExtraLevel> extraLevels,
                           List<RouteAssignment> assignments);
    void deleteApprovalRoute(int id);
    /**
     * 社員個別の承認ルートを決める（承認設定画面の社員一覧から）。
     * overtimeRouteId・leaveRouteId は残業・休暇に使うルート（null なら割り当てなし＝部署の割り当てか組織図で決まる）。
     * 選んだルートがその申請種別に使えなければエラー。
     */
    void assignStaffRoutes(int staffId, Integer overtimeRouteId, Integer leaveRouteId);

    /**
     * 部署の承認ルートを決める（その部署と下の部署の社員に使われる）。
     * null なら割り当てなし（上位部署の割り当てか組織図で決まる）。
     */
    void assignDepartmentRoutes(int departmentId, Integer overtimeRouteId, Integer leaveRouteId);

    List<WorkType> getAbstracts();
    /**
     * isNew が true なら追加（ID の重複は不可）、false なら更新。
     * paidLeaveDays は有給消費日数（0・0.5・1）。勤怠を保存すると、この日数だけ有給残日数が減る。
     */
    void saveAbstract(boolean isNew, int id, String name, boolean isWork, boolean isPaid,
                      boolean isLegalHoliday, boolean isPrescribedHoliday, double paidLeaveDays);

    List<LeaveGrantRule> getLeaveGrantRules();
    void saveLeaveGrantRule(int yearsOfService, int grantDays);
    void deleteLeaveGrantRule(int yearsOfService);

    List<ApprovalRouteSetting> getApprovalRouteSettings();
    void saveApprovalRouteSetting(RequestType requestType, int maxLevels);

    List<com.example.master.domain.model.CompanyHoliday> getCompanyHolidays(int year);
    /**
     * 開始日〜終了日（終了日が null なら開始日だけ）を会社休日にする。
     * @return 登録した日数
     */
    int addCompanyHolidays(java.time.LocalDate from, java.time.LocalDate to, String name);
    void deleteCompanyHoliday(java.time.LocalDate date);
}
