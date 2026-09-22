package com.example.shared.bootstrap;

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
import com.example.kintai.adapter.out.persistence.ApprovalHistoryPersistenceAdapter;
import com.example.kintai.adapter.out.persistence.CorrectionMasterPersistenceAdapter;
import com.example.kintai.adapter.out.persistence.OvertimeRequestPersistenceAdapter;
import com.example.kintai.application.port.in.ApproveAttendanceUseCase;
import com.example.kintai.application.port.in.DecideOvertimeRequestUseCase;
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
import com.example.kintai.application.service.GetMonthlyAttendanceService;
import com.example.kintai.application.service.OvertimeRequestService;
import com.example.kintai.application.service.UpdateAttendanceService;
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
    private final com.example.shain.domain.port.out.ShainPort shainPort;
    private final ClassmasterPort classmasterPort;
    private final TransactionManager transactionManager;
    private final WorkTypePort workTypePort;
    private final PaidLeavePort paidLeavePort;
    private final ApprovalHistoryPersistenceAdapter approvalHistoryPersistenceAdapter;
    private final CorrectionMasterPersistenceAdapter correctionMasterPersistenceAdapter;
    private final OvertimeRequestPersistenceAdapter overtimeRequestPersistenceAdapter;

    // --- Use Cases ---
    private final AuthenticateUseCase authenticateUseCase;
    private final GetMonthlyAttendanceUseCase getMonthlyAttendanceUseCase;
    private final com.example.shain.application.port.in.GetShainListUseCase getShainListUseCase;
    private final com.example.shain.application.port.in.GetShainByIdUseCase getShainByIdUseCase;
    private final GetAllClassmastersUseCase getAllClassmastersUseCase;
    private final com.example.shain.application.port.in.UpdateShainUseCase updateShainUseCase;
    private final com.example.shain.application.port.in.GetNextShainIdUseCase getNextShainIdUseCase;
    private final RegisterShainUseCase registerShainUseCase;
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

    // --- Actions ---
    private final Map<String, Action> actionMap = new ConcurrentHashMap<>();

    public DependencyFactory() {
        // --- 依存関係の最下層 (Adapters) からインスタンスを生成 ---
        this.employeePersistenceAdapter = new EmployeePersistenceAdapter();
        this.attendancePersistenceAdapter = new AttendancePersistenceAdapter();
        this.shainPort = new ShainPersistenceAdapter();
        this.classmasterPort = new ClassmasterPersistenceAdapter();
        this.transactionManager = new TransactionManagerImpl();
        this.workTypePort = new WorkTypePersistenceAdapter();
        this.paidLeavePort = new PaidLeavePersistenceAdapter();
        this.approvalHistoryPersistenceAdapter = new ApprovalHistoryPersistenceAdapter();
        this.correctionMasterPersistenceAdapter = new CorrectionMasterPersistenceAdapter();
        this.overtimeRequestPersistenceAdapter = new OvertimeRequestPersistenceAdapter();
        com.example.auth.domain.port.out.LoginPort loginPort = new LoginPersistenceAdapter();

        // --- Service (Use Cases) を生成し、Adapter(Port)をコンストラクタで注入 ---
        this.authenticateUseCase = new AuthenticateService(loginPort);
        this.getMonthlyAttendanceUseCase = new GetMonthlyAttendanceService(
                this.attendancePersistenceAdapter,
                this.employeePersistenceAdapter
        );
        
        ShainService shainService = new ShainService(this.shainPort);
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
        this.updateAttendanceUseCase = new UpdateAttendanceService(
                this.attendancePersistenceAdapter,
                this.attendancePersistenceAdapter,
                this.paidLeavePort,
                this.transactionManager,
                this.workTypePort,
                this.overtimeRequestPersistenceAdapter
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

        OvertimeRequestService overtimeRequestService = new OvertimeRequestService(this.overtimeRequestPersistenceAdapter);
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
        actionMap.put("LoginExecute", new com.example.auth.adapter.in.web.LoginExecuteAction(this.authenticateUseCase, this.getShainByIdUseCase));
        actionMap.put("Logout", new com.example.auth.adapter.in.web.LogoutAction());

        // KintaiDisplayAction
        actionMap.put("KintaiDisplay", new KintaiDisplayAction(this.getMonthlyAttendanceUseCase, this.getWorkTypesUseCase, this.getCorrectionMastersUseCase, this.getMyOvertimeRequestsUseCase));

        // Shain Actions (New Context)
        actionMap.put("ShainList", new com.example.shain.adapter.in.web.ShainListAction(this.getShainListUseCase, this.getAllClassmastersUseCase));
        actionMap.put("ShainUpdateForm", new com.example.shain.adapter.in.web.ShainUpdateFormAction(this.getShainByIdUseCase, this.getAllClassmastersUseCase));
        actionMap.put("ShainUpdateExecute", new com.example.shain.adapter.in.web.ShainUpdateExecuteAction(this.updateShainUseCase));
        actionMap.put("ShainInsertForm", new com.example.shain.adapter.in.web.ShainInsertFormAction(this.getAllClassmastersUseCase, this.getNextShainIdUseCase));
        actionMap.put("ShainInsertExecute", new com.example.shain.adapter.in.web.ShainInsertExecuteAction(this.registerShainUseCase));
        actionMap.put("ShainDeleteForm", new com.example.shain.adapter.in.web.ShainDeleteFormAction(this.getShainByIdUseCase));
        actionMap.put("ShainDeleteExecute", new com.example.shain.adapter.in.web.ShainDeleteExecuteAction(this.deleteShainUseCase));

        // Menu Action
        actionMap.put("Menu", new MenuAction(this.getMonthlyAttendanceUseCase));

        // Paid Leave Admin Actions
        actionMap.put("PaidLeaveAdmin", new com.example.shain.adapter.in.web.PaidLeaveAdminAction(this.getShainListUseCase));
        actionMap.put("UpdateLeave", new com.example.leave.adapter.in.web.UpdateLeaveAction(this.updatePaidLeaveDaysUseCase));
        actionMap.put("GrantLeaveByYear", new com.example.leave.adapter.in.web.GrantLeaveByYearAction(this.grantLeaveByYearsOfServiceUseCase));
        actionMap.put("GrantLeaveSelected", new com.example.leave.adapter.in.web.GrantLeaveSelectedAction(this.addLeaveDaysToEmployeesUseCase));

        // Approval Actions
        actionMap.put("ApprovalList", new ApprovalListAction(this.getPendingApprovalsUseCase, this.getPendingOvertimeRequestsUseCase));
        actionMap.put("ApprovalExecute", new ApprovalExecuteAction(this.approveAttendanceUseCase));
        actionMap.put("ApprovalHistory", new ApprovalHistoryAction(this.getApprovalHistoryUseCase));

        // Correction Master Actions
        actionMap.put("CorrectionMasterList", new CorrectionMasterListAction(this.getCorrectionMastersUseCase));
        actionMap.put("CorrectionMasterRegister", new CorrectionMasterRegisterAction(this.registerCorrectionMasterUseCase));

        // Overtime Request Actions
        actionMap.put("OvertimeRequestMenu", new OvertimeRequestMenuAction(this.getMyOvertimeRequestsUseCase, this.getMonthlyAttendanceUseCase));
        actionMap.put("OvertimeRequestSubmit", new OvertimeRequestSubmitAction(this.submitOvertimeRequestUseCase));
        actionMap.put("OvertimeRequestExecute", new OvertimeRequestExecuteAction(this.decideOvertimeRequestUseCase));

        // API Actions
        actionMap.put("KintaiUpdateApi", new KintaiUpdateApiAction(this.updateAttendanceUseCase));
        actionMap.put("SearchShainApi", new com.example.shain.adapter.in.web.SearchShainApiAction(this.getShainListUseCase));
    }

    /**
     * アクション名に対応するActionインスタンスを取得します。
     * @param actionName アクション名 (例: "KintaiDisplay")
     * @return Actionインスタンス
     */
    public Action getAction(String actionName) {
        Action action = actionMap.get(actionName);
        if (action == null) {
            throw new IllegalArgumentException("No Action configured for name: " + actionName);
        }
        return action;
    }
}
