package com.example.kintai.configuration;

import com.example.application.port.in.AddLeaveDaysToEmployeesUseCase;
import com.example.application.service.AddLeaveDaysToEmployeesService;
import com.example.adapter.out.persistence.LeaveGrantRulePersistenceAdapter;
import com.example.application.port.in.GrantLeaveByYearsOfServiceUseCase;
import com.example.application.port.out.LeaveGrantRulePort;
import com.example.application.service.GrantLeaveByYearsOfServiceService;
import com.example.kintai.adapter.out.persistence.AttendancePersistenceAdapter;
import com.example.adapter.out.persistence.ClassmasterPersistenceAdapter;
import com.example.kintai.adapter.out.persistence.EmployeePersistenceAdapter;
import com.example.adapter.out.persistence.ShainPersistenceAdapter;
import com.example.adapter.out.persistence.TransactionManagerImpl;
import com.example.adapter.out.persistence.WorkTypePersistenceAdapter;
import com.example.adapter.out.persistence.KintaiPersistenceAdapter;
import com.example.adapter.out.persistence.PaidLeavePersistenceAdapter;
import com.example.adapter.out.persistence.LoginPersistenceAdapter;
import com.example.application.port.in.*;
import com.example.application.port.out.ClassmasterPort;
import com.example.application.port.out.ShainPort;
import com.example.application.port.out.TransactionManager;
import com.example.application.port.out.WorkTypePort;
import com.example.application.port.out.KintaiUpdatePort;
import com.example.application.port.out.PaidLeavePort;
import com.example.application.port.out.LoginPort;
import com.example.application.service.*;
import com.example.controller.Action;
import com.example.controller.action.*;
import com.example.controller.api.KintaiUpdateApiAction;
import com.example.controller.api.SearchShainApiAction;
import com.example.kintai.application.port.in.GetMonthlyAttendanceUseCase;
import com.example.kintai.application.service.GetMonthlyAttendanceService;

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
    private final ShainPort shainPort;
    private final ClassmasterPort classmasterPort;
    private final LeaveGrantRulePort leaveGrantRulePort;
    private final TransactionManager transactionManager;
    private final WorkTypePort workTypePort;
    private final KintaiUpdatePort kintaiUpdatePort;
    private final PaidLeavePort paidLeavePort;

    // --- Use Cases ---
    private final AuthenticateUseCase authenticateUseCase;
    private final GetMonthlyAttendanceUseCase getMonthlyAttendanceUseCase;
    private final GetShainListUseCase getShainListUseCase;
    private final GetShainByIdUseCase getShainByIdUseCase;
    private final GetAllClassmastersUseCase getAllClassmastersUseCase;
    private final UpdateShainUseCase updateShainUseCase;
    private final GetNextShainIdUseCase getNextShainIdUseCase;
    private final InsertShainUseCase insertShainUseCase;
    private final DeleteShainUseCase deleteShainUseCase;
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
        this.leaveGrantRulePort = new LeaveGrantRulePersistenceAdapter();
        this.transactionManager = new TransactionManagerImpl();
        this.workTypePort = new WorkTypePersistenceAdapter();
        this.kintaiUpdatePort = new KintaiPersistenceAdapter();
        this.paidLeavePort = new PaidLeavePersistenceAdapter();
        LoginPort loginPort = new LoginPersistenceAdapter();

        // --- Service (Use Cases) を生成し、Adapter(Port)をコンストラクタで注入 ---
        this.authenticateUseCase = new AuthenticateService(loginPort);
        this.getMonthlyAttendanceUseCase = new GetMonthlyAttendanceService(
                this.attendancePersistenceAdapter,
                this.employeePersistenceAdapter
        );
        this.getShainListUseCase = new GetShainListService(this.shainPort);
        this.getShainByIdUseCase = new GetShainByIdService(this.shainPort);
        this.getAllClassmastersUseCase = new GetAllClassmastersService(this.classmasterPort);
        this.updateShainUseCase = new UpdateShainService(this.shainPort);
        this.getNextShainIdUseCase = new GetNextShainIdService(this.shainPort);
        this.insertShainUseCase = new InsertShainService(this.shainPort);
        this.deleteShainUseCase = new DeleteShainService(this.shainPort);
        this.updatePaidLeaveDaysUseCase = new UpdatePaidLeaveDaysService(this.shainPort);
        this.grantLeaveByYearsOfServiceUseCase = new GrantLeaveByYearsOfServiceService(this.shainPort, this.leaveGrantRulePort, this.transactionManager);
        this.addLeaveDaysToEmployeesUseCase = new AddLeaveDaysToEmployeesService(this.shainPort, this.transactionManager);
        this.getWorkTypesUseCase = new GetWorkTypesService(this.workTypePort);
        this.updateKintaiUseCase = new UpdateKintaiService(this.kintaiUpdatePort, this.paidLeavePort, this.transactionManager, this.workTypePort);

        // --- Actionを初期化 ---
        initializeActions();
    }

    private void initializeActions() {
        // Login Actions
        actionMap.put("Login", new LoginAction());
        actionMap.put("LoginExecute", new LoginExecuteAction(this.authenticateUseCase, this.getShainByIdUseCase));

        // KintaiDisplayAction
        actionMap.put("KintaiDisplay", new KintaiDisplayAction(this.getMonthlyAttendanceUseCase, this.getWorkTypesUseCase));

        // Shain Actions
        actionMap.put("ShainList", new ShainListAction(this.getShainListUseCase));
        actionMap.put("ShainUpdateForm", new ShainUpdateFormAction(this.getShainByIdUseCase, this.getAllClassmastersUseCase));
        actionMap.put("ShainUpdateExecute", new ShainUpdateExecuteAction(this.updateShainUseCase));
        actionMap.put("ShainInsertForm", new ShainInsertFormAction(this.getAllClassmastersUseCase, this.getNextShainIdUseCase));
        actionMap.put("ShainInsertExecute", new ShainInsertExecuteAction(this.insertShainUseCase));
        actionMap.put("ShainDeleteForm", new ShainDeleteFormAction(this.getShainByIdUseCase));
        actionMap.put("ShainDeleteExecute", new ShainDeleteExecuteAction(this.deleteShainUseCase));

        // Menu Action
        actionMap.put("Menu", new MenuAction(this.getMonthlyAttendanceUseCase));
        actionMap.put("Logout", new LogoutAction());

        // Paid Leave Admin Actions
        actionMap.put("PaidLeaveAdmin", new PaidLeaveAdminAction(this.getShainListUseCase));
        actionMap.put("UpdateLeave", new UpdateLeaveAction(this.updatePaidLeaveDaysUseCase));
        actionMap.put("GrantLeaveByYear", new GrantLeaveByYearAction(this.grantLeaveByYearsOfServiceUseCase));
        actionMap.put("GrantLeaveSelected", new GrantLeaveSelectedAction(this.addLeaveDaysToEmployeesUseCase));

        // API Actions
        actionMap.put("KintaiUpdateApi", new KintaiUpdateApiAction(this.updateKintaiUseCase));
        actionMap.put("SearchShainApi", new SearchShainApiAction(this.getShainListUseCase));

        // 他のActionも同様に追加していく
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
