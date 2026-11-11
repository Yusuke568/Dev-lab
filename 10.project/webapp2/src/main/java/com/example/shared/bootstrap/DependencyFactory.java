package com.example.shared.bootstrap;

import com.example.permission.domain.model.Feature;

import com.example.auth.application.port.in.AuthenticateUseCase;
import com.example.auth.application.service.AuthenticateService;
import com.example.auth.adapter.out.persistence.LoginPersistenceAdapter;
import com.example.leave.application.port.in.AddLeaveDaysToEmployeesUseCase;
import com.example.leave.application.service.PaidLeaveService;
import com.example.leave.adapter.out.persistence.PaidLeavePersistenceAdapter;
import com.example.leave.application.port.in.GrantLeaveByYearsOfServiceUseCase;
import com.example.leave.application.port.in.UpdatePaidLeaveDaysUseCase;
import com.example.leave.domain.port.out.PaidLeavePort;
import com.example.kintai.adapter.out.persistence.AttendancePersistenceAdapter;
import com.example.adapter.out.persistence.ClassmasterPersistenceAdapter;
import com.example.kintai.adapter.out.persistence.EmployeePersistenceAdapter;
import com.example.shain.adapter.out.persistence.ShainPersistenceAdapter;
import com.example.shared.persistence.TransactionManagerImpl;
import com.example.adapter.out.persistence.WorkTypePersistenceAdapter;
import com.example.application.port.in.*;
import com.example.application.port.out.ClassmasterPort;
import com.example.shared.transaction.TransactionManager;
import com.example.application.port.out.WorkTypePort;
import com.example.application.service.*;
import com.example.shared.web.Action;
import com.example.controller.action.*;
import com.example.kintai.adapter.in.web.ApprovalExecuteAction;
import com.example.kintai.adapter.in.web.ApprovalHistoryAction;
import com.example.kintai.adapter.in.web.ApprovalListAction;
import com.example.kintai.adapter.in.web.CorrectionMasterListAction;
import com.example.kintai.adapter.in.web.CorrectionMasterRegisterAction;
import com.example.kintai.adapter.in.web.KintaiUpdateApiAction;
import com.example.kintai.adapter.in.web.OvertimeRequestExecuteAction;
import com.example.kintai.adapter.in.web.OvertimeRequestMenuAction;
import com.example.kintai.adapter.in.web.OvertimeRequestSubmitAction;
import com.example.kintai.adapter.in.web.WorkReportExportAction;
import com.example.kintai.adapter.in.web.WeeklyWorkReportSaveAction;
import com.example.kintai.adapter.out.export.WorkReportXlsmExportAdapter;
import com.example.kintai.adapter.out.persistence.ApprovalHistoryPersistenceAdapter;
import com.example.kintai.adapter.out.persistence.CorrectionMasterPersistenceAdapter;
import com.example.kintai.adapter.out.persistence.OvertimeRequestPersistenceAdapter;
import com.example.kintai.adapter.out.persistence.WeeklyWorkReportPersistenceAdapter;
import com.example.kintai.application.port.in.ApproveAttendanceUseCase;
import com.example.kintai.application.port.in.DecideOvertimeRequestUseCase;
import com.example.kintai.application.port.in.ExportWorkReportUseCase;
import com.example.kintai.application.port.in.GetWeeklyWorkReportsUseCase;
import com.example.kintai.application.port.in.SaveWeeklyWorkReportsUseCase;
import com.example.kintai.application.port.in.GetApprovalHistoryUseCase;
import com.example.kintai.application.port.in.GetCorrectionMastersUseCase;
import com.example.kintai.application.port.in.GetMonthlyAttendanceUseCase;
import com.example.kintai.application.port.in.GetMyOvertimeRequestsUseCase;
import com.example.kintai.application.port.in.GetPendingApprovalsUseCase;
import com.example.kintai.application.port.in.GetPendingOvertimeRequestsUseCase;
import com.example.kintai.application.port.in.RegisterCorrectionMasterUseCase;
import com.example.kintai.application.port.in.SubmitOvertimeRequestUseCase;
import com.example.kintai.application.port.in.UpdateAttendanceUseCase;
import com.example.kintai.application.service.AttendanceApprovalService;
import com.example.kintai.application.service.CorrectionMasterService;
import com.example.kintai.application.service.ExportWorkReportService;
import com.example.kintai.application.service.GetMonthlyAttendanceService;
import com.example.kintai.application.service.OvertimeRequestService;
import com.example.kintai.application.service.UpdateAttendanceService;
import com.example.kintai.application.service.WeeklyWorkReportService;
import com.example.kintai.domain.port.out.WeeklyWorkReportPort;
import com.example.kintai.domain.port.out.WorkReportExportPort;
import com.example.shain.application.port.in.*;
import com.example.shain.application.service.ShainService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * アプリケーション全体の依存関係を構築・管理するファクトリクラス。
 * DIコンテナの役割を担います。
 */
public class DependencyFactory {

    // --- Adapters & Ports ---
    private final EmployeePersistenceAdapter employeePersistenceAdapter;
    private final AttendancePersistenceAdapter attendancePersistenceAdapter;
    private final com.example.kintai.application.port.in.AttendanceAccessUseCase attendanceAccessUseCase;
    private final com.example.notice.application.port.in.NoticeUseCase noticeUseCase =
            new com.example.notice.application.service.NoticeService(
                    new com.example.notice.adapter.out.persistence.NoticePersistenceAdapter(), java.time.Clock.systemDefaultZone());
    private final com.example.shain.domain.port.out.ShainPort shainPort;
    private final ClassmasterPort classmasterPort;
    private final TransactionManager transactionManager;
    private final WorkTypePort workTypePort;
    private final com.example.kintai.application.service.WeeklyOvertimeService weeklyOvertimeService;
    private final PaidLeavePort paidLeavePort;
    private final ApprovalHistoryPersistenceAdapter approvalHistoryPersistenceAdapter;
    private final CorrectionMasterPersistenceAdapter correctionMasterPersistenceAdapter;
    private final OvertimeRequestPersistenceAdapter overtimeRequestPersistenceAdapter;
    private final WorkReportExportPort workReportExportPort;
    private final com.example.kintai.domain.port.out.WorkReportImportPort workReportImportPort;
    private final WeeklyWorkReportPort weeklyWorkReportPort;

    // --- Use Cases ---
    private final AuthenticateUseCase authenticateUseCase;
    private final GetMonthlyAttendanceUseCase getMonthlyAttendanceUseCase;
    private final com.example.shain.application.port.in.GetShainListUseCase getShainListUseCase;
    private final com.example.shain.application.port.in.GetShainByIdUseCase getShainByIdUseCase;
    private final GetAllClassmastersUseCase getAllClassmastersUseCase;
    private final com.example.shain.application.port.in.UpdateShainUseCase updateShainUseCase;
    private final com.example.shain.application.port.in.GetNextShainIdUseCase getNextShainIdUseCase;
    private final RegisterShainUseCase registerShainUseCase;
    private final com.example.auth.application.port.in.ChangeOwnPasswordUseCase changeOwnPasswordUseCase;
    private final com.example.auth.application.port.in.ResetPasswordUseCase resetPasswordUseCase;
    private final com.example.shain.application.port.in.DeleteShainUseCase deleteShainUseCase;
    private final UpdatePaidLeaveDaysUseCase updatePaidLeaveDaysUseCase;
    private final GrantLeaveByYearsOfServiceUseCase grantLeaveByYearsOfServiceUseCase;
    private final AddLeaveDaysToEmployeesUseCase addLeaveDaysToEmployeesUseCase;
    private final GetWorkTypesUseCase getWorkTypesUseCase;
    private final UpdateAttendanceUseCase updateAttendanceUseCase;
    private final GetPendingApprovalsUseCase getPendingApprovalsUseCase;
    private final ApproveAttendanceUseCase approveAttendanceUseCase;
    private final GetApprovalHistoryUseCase getApprovalHistoryUseCase;
    private final GetCorrectionMastersUseCase getCorrectionMastersUseCase;
    private final RegisterCorrectionMasterUseCase registerCorrectionMasterUseCase;
    private final SubmitOvertimeRequestUseCase submitOvertimeRequestUseCase;
    private final GetMyOvertimeRequestsUseCase getMyOvertimeRequestsUseCase;
    private final GetPendingOvertimeRequestsUseCase getPendingOvertimeRequestsUseCase;
    private final DecideOvertimeRequestUseCase decideOvertimeRequestUseCase;
    private final ExportWorkReportUseCase exportWorkReportUseCase;
    private final GetWeeklyWorkReportsUseCase getWeeklyWorkReportsUseCase;
    private final SaveWeeklyWorkReportsUseCase saveWeeklyWorkReportsUseCase;
    private final com.example.kintai.application.port.in.ImportWorkReportUseCase importWorkReportUseCase;
    private final com.example.approval.application.service.ApprovalWorkflowService approvalWorkflowService;
    private final com.example.master.application.port.in.MasterMaintenanceUseCase masterMaintenanceUseCase;
    private final com.example.permission.application.port.in.GetUserFeaturesUseCase getUserFeaturesUseCase;
    private final com.example.leave.application.port.in.LeaveRequestUseCase leaveRequestUseCase;

    // --- Actions ---
    private final Map<String, Action> actionMap = new ConcurrentHashMap<>();
    /** アクション名 → 必要な機能（どれか1つあれば使える） */
    private final Map<String, java.util.Set<Feature>> featureMap = new ConcurrentHashMap<>();
    private static final java.util.Set<String> PUBLIC_ACTIONS = java.util.Set.of("Login", "LoginExecute", "Logout");

    public DependencyFactory() {
        // --- 依存関係の最下層 (Adapters) からインスタンスを生成 ---
        this.employeePersistenceAdapter = new EmployeePersistenceAdapter();
        this.attendancePersistenceAdapter = new AttendancePersistenceAdapter();
        this.attendanceAccessUseCase = new com.example.kintai.application.service.AttendanceAccessService(this.attendancePersistenceAdapter);
        this.shainPort = new ShainPersistenceAdapter();
        this.classmasterPort = new ClassmasterPersistenceAdapter();
        this.transactionManager = new TransactionManagerImpl();
        this.workTypePort = new WorkTypePersistenceAdapter();
        this.weeklyOvertimeService = new com.example.kintai.application.service.WeeklyOvertimeService(
                this.attendancePersistenceAdapter, this.attendancePersistenceAdapter, this.workTypePort);
        this.paidLeavePort = new PaidLeavePersistenceAdapter();
        this.approvalHistoryPersistenceAdapter = new ApprovalHistoryPersistenceAdapter();
        this.correctionMasterPersistenceAdapter = new CorrectionMasterPersistenceAdapter();
        this.overtimeRequestPersistenceAdapter = new OvertimeRequestPersistenceAdapter();
        this.workReportExportPort = new WorkReportXlsmExportAdapter();
        this.workReportImportPort = new com.example.kintai.adapter.out.export.WorkReportXlsmImportAdapter();
        this.weeklyWorkReportPort = new WeeklyWorkReportPersistenceAdapter();
        com.example.auth.domain.port.out.LoginPort loginPort = new LoginPersistenceAdapter();
        com.example.master.adapter.out.persistence.MasterPersistenceAdapter masterPersistenceAdapter =
                new com.example.master.adapter.out.persistence.MasterPersistenceAdapter();

        // --- 権限・組織図・承認ルート ---
        this.approvalWorkflowService = new com.example.approval.application.service.ApprovalWorkflowService(
                new com.example.approval.adapter.out.persistence.ApprovalStepPersistenceAdapter(),
                masterPersistenceAdapter);
        this.masterMaintenanceUseCase = new com.example.master.application.service.MasterMaintenanceService(masterPersistenceAdapter);
        this.getUserFeaturesUseCase = new com.example.permission.application.service.GetUserFeaturesService(
                new com.example.permission.adapter.out.persistence.PermissionPersistenceAdapter());
        this.leaveRequestUseCase = new com.example.leave.application.service.LeaveRequestService(
                new com.example.leave.adapter.out.persistence.LeaveRequestPersistenceAdapter(),
                this.approvalWorkflowService);

        // --- Service (Use Cases) を生成し、Adapter(Port)をコンストラクタで注入 ---
        com.example.auth.domain.service.PasswordHasher passwordHasher = new com.example.auth.domain.service.PasswordHasher();
        com.example.auth.domain.service.PasswordPolicy passwordPolicy = new com.example.auth.domain.service.PasswordPolicy();
        com.example.auth.domain.service.InitialPasswordResolver initialPasswordResolver =
                new com.example.auth.domain.service.InitialPasswordResolver(passwordPolicy,
                        new com.example.auth.domain.service.TemporaryPasswordGenerator());
        this.authenticateUseCase = new AuthenticateService(loginPort, passwordHasher);
        this.changeOwnPasswordUseCase = new com.example.auth.application.service.ChangeOwnPasswordService(
                loginPort, passwordHasher, passwordPolicy);
        this.resetPasswordUseCase = new com.example.auth.application.service.ResetPasswordService(
                loginPort, this.shainPort, passwordHasher, initialPasswordResolver);
        this.getMonthlyAttendanceUseCase = new GetMonthlyAttendanceService(
                this.attendancePersistenceAdapter,
                this.employeePersistenceAdapter
        );
        
        ShainService shainService = new ShainService(this.shainPort, loginPort, passwordHasher, initialPasswordResolver,
                this.transactionManager);
        this.getShainListUseCase = shainService;
        this.getShainByIdUseCase = shainService;
        this.registerShainUseCase = shainService;
        this.updateShainUseCase = shainService;
        this.deleteShainUseCase = shainService;
        this.getNextShainIdUseCase = shainService;

        PaidLeaveService paidLeaveService = new PaidLeaveService(this.paidLeavePort, this.shainPort, this.transactionManager);
        this.updatePaidLeaveDaysUseCase = paidLeaveService;
        this.grantLeaveByYearsOfServiceUseCase = paidLeaveService;
        this.addLeaveDaysToEmployeesUseCase = paidLeaveService;

        this.getAllClassmastersUseCase = new GetAllClassmastersService(this.classmasterPort);
        this.getWorkTypesUseCase = new GetWorkTypesService(this.workTypePort);
        WeeklyWorkReportService weeklyWorkReportService = new WeeklyWorkReportService(this.weeklyWorkReportPort);
        this.getWeeklyWorkReportsUseCase = weeklyWorkReportService;
        this.saveWeeklyWorkReportsUseCase = weeklyWorkReportService;
        // 作業報告書の置き場所（-Dkintai.workReport.archiveDir か 環境変数 KINTAI_WORK_REPORT_DIR。なければ置かない）
        this.exportWorkReportUseCase = new ExportWorkReportService(
                this.getMonthlyAttendanceUseCase,
                this.getWorkTypesUseCase,
                this.getWeeklyWorkReportsUseCase,
                this.workReportExportPort,
                com.example.kintai.adapter.out.export.FileSystemWorkReportArchiveAdapter.fromSystemSettings()
        ).withAttendanceCheck(this.attendancePersistenceAdapter)
                .withDepartmentName(this.attendancePersistenceAdapter);
        this.updateAttendanceUseCase = new UpdateAttendanceService(
                this.attendancePersistenceAdapter,
                this.attendancePersistenceAdapter,
                this.paidLeavePort,
                this.transactionManager,
                this.workTypePort,
                this.overtimeRequestPersistenceAdapter,
                this.weeklyOvertimeService
        );
        this.importWorkReportUseCase = new com.example.kintai.application.service.ImportWorkReportService(
                this.workReportImportPort,
                this.updateAttendanceUseCase,
                this.saveWeeklyWorkReportsUseCase,
                this.getWorkTypesUseCase
        );

        AttendanceApprovalService attendanceApprovalService = new AttendanceApprovalService(
                this.attendancePersistenceAdapter,
                this.attendancePersistenceAdapter,
                this.attendancePersistenceAdapter,
                this.approvalHistoryPersistenceAdapter,
                this.approvalHistoryPersistenceAdapter
        );
        this.getPendingApprovalsUseCase = attendanceApprovalService;
        this.approveAttendanceUseCase = attendanceApprovalService;
        this.getApprovalHistoryUseCase = attendanceApprovalService;

        CorrectionMasterService correctionMasterService = new CorrectionMasterService(this.correctionMasterPersistenceAdapter);
        this.getCorrectionMastersUseCase = correctionMasterService;
        this.registerCorrectionMasterUseCase = correctionMasterService;

        OvertimeRequestService overtimeRequestService = new OvertimeRequestService(this.overtimeRequestPersistenceAdapter, this.approvalWorkflowService,
                this.attendancePersistenceAdapter);
        this.submitOvertimeRequestUseCase = overtimeRequestService;
        this.getMyOvertimeRequestsUseCase = overtimeRequestService;
        this.getPendingOvertimeRequestsUseCase = overtimeRequestService;
        this.decideOvertimeRequestUseCase = overtimeRequestService;

        // --- Actionを初期化 ---
        initializeActions();
    }

    private void initializeActions() {
        // Auth Actions
        actionMap.put("Login", new com.example.auth.adapter.in.web.LoginAction());
        actionMap.put("LoginExecute", new com.example.auth.adapter.in.web.LoginExecuteAction(this.authenticateUseCase, this.getShainByIdUseCase, this.getUserFeaturesUseCase));
        actionMap.put("Logout", new com.example.auth.adapter.in.web.LogoutAction());
        actionMap.put("PasswordChange", new com.example.auth.adapter.in.web.PasswordChangeAction());
        actionMap.put("PasswordChangeExecute", new com.example.auth.adapter.in.web.PasswordChangeExecuteAction(this.changeOwnPasswordUseCase));
        actionMap.put("PasswordResetForm", new com.example.auth.adapter.in.web.PasswordResetFormAction(this.getShainByIdUseCase));
        actionMap.put("PasswordResetExecute", new com.example.auth.adapter.in.web.PasswordResetExecuteAction(this.resetPasswordUseCase, this.getShainByIdUseCase));

        // KintaiDisplayAction
        actionMap.put("KintaiDisplay", new KintaiDisplayAction(this.getMonthlyAttendanceUseCase, this.getWorkTypesUseCase, this.getCorrectionMastersUseCase, this.getMyOvertimeRequestsUseCase, this.getWeeklyWorkReportsUseCase, this.attendanceAccessUseCase,
                new com.example.kintai.application.service.WeeklyWorkSummaryService(this.attendancePersistenceAdapter),
                new com.example.kintai.application.service.HolidayService(this.attendancePersistenceAdapter),
                this.noticeUseCase).withWeeklyOvertime(this.weeklyOvertimeService));

        // Shain Actions (New Context)
        actionMap.put("ShainList", new com.example.shain.adapter.in.web.ShainListAction(this.getShainListUseCase, this.getAllClassmastersUseCase));
        com.example.shain.adapter.in.web.ShainUpdateFormAction shainUpdateFormAction =
                new com.example.shain.adapter.in.web.ShainUpdateFormAction(this.getShainByIdUseCase, this.getAllClassmastersUseCase, this.masterMaintenanceUseCase);
        actionMap.put("ShainUpdateForm", shainUpdateFormAction);
        actionMap.put("ShainUpdateExecute", new com.example.shain.adapter.in.web.ShainUpdateExecuteAction(this.updateShainUseCase, this.masterMaintenanceUseCase, shainUpdateFormAction));
        actionMap.put("ShainIdCheckApi", new com.example.shain.adapter.in.web.ShainIdCheckApiAction(this.updateShainUseCase));
        com.example.shain.adapter.in.web.ShainInsertFormAction shainInsertFormAction =
                new com.example.shain.adapter.in.web.ShainInsertFormAction(this.getAllClassmastersUseCase, this.getNextShainIdUseCase, this.masterMaintenanceUseCase);
        actionMap.put("ShainInsertForm", shainInsertFormAction);
        actionMap.put("ShainInsertExecute", new com.example.shain.adapter.in.web.ShainInsertExecuteAction(this.registerShainUseCase, shainInsertFormAction));
        actionMap.put("ShainDeleteForm", new com.example.shain.adapter.in.web.ShainDeleteFormAction(this.getShainByIdUseCase));
        actionMap.put("ShainDeleteExecute", new com.example.shain.adapter.in.web.ShainDeleteExecuteAction(this.deleteShainUseCase));

        // Menu Action
        actionMap.put("Menu", new MenuAction(this.getMonthlyAttendanceUseCase));

        // Paid Leave Admin Actions
        actionMap.put("PaidLeaveAdmin", new com.example.shain.adapter.in.web.PaidLeaveAdminAction(this.getShainListUseCase, this.grantLeaveByYearsOfServiceUseCase));
        actionMap.put("UpdateLeave", new com.example.leave.adapter.in.web.UpdateLeaveAction(this.updatePaidLeaveDaysUseCase));
        actionMap.put("GrantLeaveByYear", new com.example.leave.adapter.in.web.GrantLeaveByYearAction(this.grantLeaveByYearsOfServiceUseCase));
        actionMap.put("GrantLeaveSelected", new com.example.leave.adapter.in.web.GrantLeaveSelectedAction(this.addLeaveDaysToEmployeesUseCase));

        // Approval Actions
        actionMap.put("ApprovalList", new ApprovalListAction(this.getPendingApprovalsUseCase, this.getPendingOvertimeRequestsUseCase,
                this.leaveRequestUseCase, this.approvalWorkflowService).withOvertimeStatus(
                new com.example.kintai.application.service.OvertimeStatusService(
                        this.attendancePersistenceAdapter, this.overtimeRequestPersistenceAdapter, this.approvalWorkflowService)));
        actionMap.put("ApprovalExecute", new ApprovalExecuteAction(this.approveAttendanceUseCase));
        actionMap.put("ApprovalHistory", new ApprovalHistoryAction(this.getApprovalHistoryUseCase));
        actionMap.put("SubordinateAttendance", new com.example.kintai.adapter.in.web.SubordinateAttendanceAction(this.attendanceAccessUseCase)
                .withSummary(new com.example.kintai.application.service.SubordinateSummaryService(
                        this.attendancePersistenceAdapter, this.attendancePersistenceAdapter, this.workTypePort)));
        actionMap.put("SubordinateOvertimeForm", new com.example.kintai.adapter.in.web.SubordinateOvertimeFormAction(
                this.attendanceAccessUseCase, new com.example.kintai.application.service.OvertimeFormExportService(
                        this.overtimeRequestPersistenceAdapter, this.attendancePersistenceAdapter, this.attendancePersistenceAdapter,
                        new com.example.kintai.adapter.out.export.OvertimeFormXlsxExportAdapter())));
        actionMap.put("NoticeManage", new com.example.notice.adapter.in.web.NoticeManageAction(this.noticeUseCase));

        // Correction Master Actions
        actionMap.put("CorrectionMasterList", new CorrectionMasterListAction(this.getCorrectionMastersUseCase));
        actionMap.put("CorrectionMasterRegister", new CorrectionMasterRegisterAction(this.registerCorrectionMasterUseCase));

        // Overtime Request Actions
        actionMap.put("OvertimeRequestMenu", new OvertimeRequestMenuAction(this.getMyOvertimeRequestsUseCase, this.getMonthlyAttendanceUseCase,
                this.approvalWorkflowService));
        actionMap.put("WorkReportExport", new WorkReportExportAction(this.exportWorkReportUseCase));
        actionMap.put("WorkReportImport", new com.example.kintai.adapter.in.web.WorkReportImportAction(this.importWorkReportUseCase));
        actionMap.put("WorkReportBulkImport", new com.example.kintai.adapter.in.web.WorkReportBulkImportAction(
                this.importWorkReportUseCase, this.attendanceAccessUseCase));
        actionMap.put("WeeklyWorkReportSave", new WeeklyWorkReportSaveAction(this.saveWeeklyWorkReportsUseCase, this.attendanceAccessUseCase));
        actionMap.put("OvertimeRequestSubmit", new OvertimeRequestSubmitAction(this.submitOvertimeRequestUseCase));
        actionMap.put("OvertimeRequestWithdraw", new com.example.kintai.adapter.in.web.OvertimeRequestWithdrawAction(
                this.submitOvertimeRequestUseCase));
        actionMap.put("OvertimeRequestExecute", new OvertimeRequestExecuteAction(this.decideOvertimeRequestUseCase));

        // API Actions
        actionMap.put("KintaiUpdateApi", new KintaiUpdateApiAction(this.updateAttendanceUseCase, this.attendanceAccessUseCase));
        actionMap.put("SearchShainApi", new com.example.shain.adapter.in.web.SearchShainApiAction(this.getShainListUseCase));

        // Leave Request Actions
        actionMap.put("LeaveRequestMenu", new com.example.leave.adapter.in.web.LeaveRequestMenuAction(
                this.leaveRequestUseCase, this.getWorkTypesUseCase, this.approvalWorkflowService));
        actionMap.put("LeaveRequestSubmit", new com.example.leave.adapter.in.web.LeaveRequestSubmitAction(this.leaveRequestUseCase));
        actionMap.put("LeaveRequestExecute", new com.example.leave.adapter.in.web.LeaveRequestExecuteAction(this.leaveRequestUseCase));

        // Master Actions
        actionMap.put("MasterMenu", new com.example.master.adapter.in.web.MasterMenuAction());
        actionMap.put("MasterJobClass", new com.example.master.adapter.in.web.MasterJobClassAction(this.masterMaintenanceUseCase));
        actionMap.put("MasterPermissionGroup", new com.example.master.adapter.in.web.MasterPermissionGroupAction(this.masterMaintenanceUseCase));
        actionMap.put("MasterDepartment", new com.example.master.adapter.in.web.MasterDepartmentAction(this.masterMaintenanceUseCase));
        actionMap.put("MasterAbstract", new com.example.master.adapter.in.web.MasterAbstractAction(this.masterMaintenanceUseCase));
        actionMap.put("MasterLeaveRule", new com.example.master.adapter.in.web.MasterLeaveRuleAction(this.masterMaintenanceUseCase));
        actionMap.put("MasterApprovalRoute", new com.example.master.adapter.in.web.MasterApprovalRouteAction(this.masterMaintenanceUseCase));
        actionMap.put("MasterApprovalRouteDef", new com.example.master.adapter.in.web.MasterApprovalRouteDefAction(this.masterMaintenanceUseCase, this.approvalWorkflowService));
        actionMap.put("MasterApprovalSettings", new com.example.master.adapter.in.web.MasterApprovalSettingsAction(this.masterMaintenanceUseCase, this.approvalWorkflowService));
        actionMap.put("MasterCompanyHoliday", new com.example.master.adapter.in.web.MasterCompanyHolidayAction(this.masterMaintenanceUseCase));

        initializeFeatureMap();
    }

    /**
     * アクション名に対応するActionインスタンスを取得します。
     * @param actionName アクション名 (例: "KintaiDisplay")
     * @return Actionインスタンス
     */
    /**
     * アクションごとに必要な機能。ここに無いアクションは、ログインしていれば誰でも使える。
     */
    private void initializeFeatureMap() {
        requireFeature(Feature.ATTENDANCE, "KintaiDisplay", "KintaiUpdateApi", "WorkReportExport", "WorkReportImport",
                "WeeklyWorkReportSave");
        requireFeature(Feature.OVERTIME_REQUEST, "OvertimeRequestMenu", "OvertimeRequestSubmit", "OvertimeRequestWithdraw");
        requireFeature(Feature.LEAVE_REQUEST, "LeaveRequestMenu", "LeaveRequestSubmit");
        requireFeature(Feature.APPROVAL, "ApprovalList", "ApprovalExecute", "ApprovalHistory", "OvertimeRequestExecute", "SubordinateAttendance", "SubordinateOvertimeForm", "NoticeManage",
                "LeaveRequestExecute");
        requireFeature(Feature.SHAIN_ADMIN, "ShainList", "ShainUpdateForm", "ShainUpdateExecute", "ShainInsertForm",
                "ShainInsertExecute", "ShainDeleteForm", "ShainDeleteExecute", "SearchShainApi", "ShainIdCheckApi",
                "PasswordResetForm", "PasswordResetExecute");
        requireFeature(Feature.WEEKLY_REPORT_ADMIN, "WorkReportBulkImport");
        requireFeature(Feature.PAID_LEAVE_ADMIN, "PaidLeaveAdmin", "UpdateLeave", "GrantLeaveByYear", "GrantLeaveSelected");
        // マスタはマスタごとに権限を分ける。マスタ管理のトップは、どれか1つのマスタを使えれば開ける
        requireFeature(Feature.MASTER_PERMISSION_GROUP, "MasterPermissionGroup");
        requireFeature(Feature.MASTER_JOB_CLASS, "MasterJobClass");
        requireFeature(Feature.MASTER_DEPARTMENT, "MasterDepartment");
        requireFeature(Feature.MASTER_APPROVAL_ROUTE, "MasterApprovalRoute", "MasterApprovalRouteDef");
        requireFeature(Feature.MASTER_ABSTRACT, "MasterAbstract");
        requireFeature(Feature.MASTER_LEAVE_RULE, "MasterLeaveRule");
        requireFeature(Feature.MASTER_CORRECTION, "CorrectionMasterList", "CorrectionMasterRegister");
        requireFeature(Feature.MASTER_COMPANY_HOLIDAY, "MasterCompanyHoliday");
        requireAnyFeature(Feature.masterFeatures(), "MasterMenu");
        // 承認設定：組織図か承認ルート設定のどちらかがあれば開ける（変更できる範囲は画面の中で分ける）
        requireAnyFeature(java.util.EnumSet.of(Feature.MASTER_DEPARTMENT, Feature.MASTER_APPROVAL_ROUTE), "MasterApprovalSettings");
    }

    private void requireFeature(Feature feature, String... actionNames) {
        requireAnyFeature(java.util.EnumSet.of(feature), actionNames);
    }

    private void requireAnyFeature(java.util.Set<Feature> features, String... actionNames) {
        for (String name : actionNames) {
            if (!actionMap.containsKey(name)) {
                throw new IllegalStateException("No Action configured for name: " + name);
            }
            featureMap.put(name, features);
        }
    }

    /** ログインしなくても使えるアクションか。 */
    public boolean isPublicAction(String actionName) {
        return PUBLIC_ACTIONS.contains(actionName);
    }

    /** アクションに必要な機能（どれか1つあればよい）。ログインしていれば誰でも使えるアクションは null。 */
    public java.util.Set<Feature> getRequiredFeatures(String actionName) {
        return featureMap.get(actionName);
    }

    public Action getAction(String actionName) {
        Action action = actionMap.get(actionName);
        if (action == null) {
            throw new IllegalArgumentException("No Action configured for name: " + actionName);
        }
        return action;
    }
}
