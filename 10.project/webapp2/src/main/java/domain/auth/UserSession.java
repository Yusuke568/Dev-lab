package domain.auth;

import java.io.Serializable;
import java.util.Objects;

/**
 * ログインユーザーセッション情報
 */
public class UserSession implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String employeeId;
    private final String name;
    private final UserRole role;

    public UserSession(String employeeId, String name, UserRole role) {
        this.employeeId = Objects.requireNonNull(employeeId, "社員IDは必須です。");
        this.name = Objects.requireNonNull(name, "氏名は必須です。");
        this.role = Objects.requireNonNull(role, "ロールは必須です。");
    }

    public boolean canManageOthers() {
        return this.role == UserRole.SUPERVISOR || this.role == UserRole.ADMIN;
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    public boolean canEdit(String targetEmployeeId) {
        if (isAdmin()) return true;
        return this.employeeId.equals(targetEmployeeId);
    }

    public String getEmployeeId() { return employeeId; }
    public String getName() { return name; }
    public UserRole getRole() { return role; }
}
