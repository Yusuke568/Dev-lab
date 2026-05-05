package com.example.kintai.configuration;

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
import com.example.adapter.out.persistence.TransactionManagerImpl;
import com.example.adapter.out.persistence.WorkTypePersistenceAdapter;
import com.example.adapter.out.persistence.KintaiPersistenceAdapter;
import com.example.application.port.in.*;
import com.example.application.port.out.ClassmasterPort;
import com.example.application.port.out.TransactionManager;
import com.example.application.port.out.WorkTypePort;
import com.example.application.port.out.KintaiUpdatePort;
import com.example.application.service.*;
import com.example.adapter.in.web.Action;
import com.example.shain.adapter.in.web.*;
import com.example.leave.adapter.in.web.*;
import com.example.auth.adapter.in.web.*;
import com.example.controller.action.*;
import com.example.controller.api.KintaiUpdateApiAction;
import com.example.kintai.application.port.in.GetMonthlyAttendanceUseCase;
import com.example.kintai.application.service.GetMonthlyAttendanceService;
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
    private final KintaiUpdatePort kintaiUpdatePort;
    private final PaidLeavePort paidLeavePort;

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
    private final UpdateKintaiUseCase updateKintaiUseCase;
    
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
        this.kintaiUpdatePort = new KintaiPersistenceAdapter();
        this.paidLeavePort = new PaidLeavePersistenceAdapter();
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
        this.updateKintaiUseCase = new UpdateKintaiService(this.kintaiUpdatePort, this.paidLeavePort, this.transactionManager, this.workTypePort);

        // --- Actionを初期化 ---
        initializeActions();
    }

    private void initializeActions() {
        // Auth Actions
        actionMap.put("Login", new com.example.auth.adapter.in.web.LoginAction());
        actionMap.put("LoginExecute", new com.example.auth.adapter.in.web.LoginExecuteAction(this.authenticateUseCase, this.getShainByIdUseCase));
        actionMap.put("Logout", new com.example.auth.adapter.in.web.LogoutAction());

        // KintaiDisplayAction
        actionMap.put("KintaiDisplay", new KintaiDisplayAction(this.getMonthlyAttendanceUseCase, this.getWorkTypesUseCase));

        // Shain Actions (New Context)
        actionMap.put("ShainList", new com.example.shain.adapter.in.web.ShainListAction(this.getShainListUseCase));
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

        // API Actions
        actionMap.put("KintaiUpdateApi", new KintaiUpdateApiAction(this.updateKintaiUseCase));
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
